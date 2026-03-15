package fr.bordeaux.ai;

import fr.bordeaux.core.*;
import org.tensorflow.*;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.op.*;
import org.tensorflow.op.core.*;
import org.tensorflow.op.train.*;
import org.tensorflow.types.*;
import org.tensorflow.ndarray.*;
import java.util.*;
import java.util.function.Predicate;

/**
 * Heuristic evaluator using a TensorFlow-based Logistic Regression model.
 * Trained via random game simulations during initialization.
 */
public class NeuralNetworkEvaluator implements HeuristicEvaluator, AutoCloseable {

  private static final int NUM_FEATURES = 4;
  private static final int NUM_TRAINING_GAMES = 500;
  private static final float LEARNING_RATE = 0.01f;
  private static final int EPOCHS = 50;

  private Graph graph;
  private Session session;

  private Placeholder<TFloat32> inputPlaceholder;
  private Placeholder<TFloat32> labelPlaceholder;
  private Operand<TFloat32> prediction;
  private Op trainOp;

  /**
   * Initializes the graph and trains the model on simulated data.
   */
  public NeuralNetworkEvaluator() {
    System.out.println("Initializing TF Evaluator...");
    initGraph();
    trainModel();
  }

  /**
   * Builds the computational graph using manual gradient descent.
   */
  private void initGraph() {
    graph = new Graph();
    Ops tf = Ops.create(graph);

    // Inputs: [Batch, Features] and [Batch, Label]
    inputPlaceholder = tf.placeholder(TFloat32.class, Placeholder.shape(Shape.of(-1, NUM_FEATURES)));
    labelPlaceholder = tf.placeholder(TFloat32.class, Placeholder.shape(Shape.of(-1, 1)));

    // Parameters
    Variable<TFloat32> weights = tf.variable(Shape.of(NUM_FEATURES, 1), TFloat32.class);
    Variable<TFloat32> bias = tf.variable(Shape.of(1), TFloat32.class);

    // Variable initialization
    Assign<TFloat32> initWeights = tf.assign(weights, tf.zeros(tf.constant(Shape.of(NUM_FEATURES, 1)), TFloat32.class));
    Assign<TFloat32> initBias = tf.assign(bias, tf.zeros(tf.constant(Shape.of(1)), TFloat32.class));

    // Sigmoid(XW + b)
    prediction = tf.math.sigmoid(tf.math.add(tf.linalg.matMul(inputPlaceholder, weights), bias));

    // Mean Squared Error Loss
    Operand<TFloat32> loss = tf.math.mean(tf.math.square(tf.math.sub(prediction, labelPlaceholder)), tf.constant(0));

    // Manual backpropagation
    Gradients gradients = tf.gradients(loss, Arrays.asList(weights, bias));
    Op updateW = tf.train.applyGradientDescent(weights, tf.constant(LEARNING_RATE), gradients.dy(0));
    Op updateB = tf.train.applyGradientDescent(bias, tf.constant(LEARNING_RATE), gradients.dy(1));
    trainOp = tf.withControlDependencies(Arrays.asList(updateW, updateB)).noOp();

    session = new Session(graph);
    session.runner().addTarget(initWeights).addTarget(initBias).run();
  }

  /**
   * Performs training over specified epochs.
   */
  private void trainModel() {
    List<TrainingData> dataList = generateTrainingData();
    if (dataList.isEmpty()) return;

    try (TFloat32 inputTensor = prepareInputTensor(dataList);
         TFloat32 labelTensor = prepareLabelTensor(dataList)) {

      for (int epoch = 0; epoch < EPOCHS; epoch++) {
        session.runner()
                .feed(inputPlaceholder, inputTensor)
                .feed(labelPlaceholder, labelTensor)
                .addTarget(trainOp)
                .run();

        if ((epoch + 1) % 10 == 0) {
          System.out.println("  Epoch " + (epoch + 1) + " completed.");
        }
      }
    }
    System.out.println("Model training complete.");
  }

  /**
   * Evaluates the current game state and returns a score in [-1000, 1000].
   */
  @Override
  public int evaluate(GameState state) {
    float[] features = getFeatures(state);

    try (TFloat32 input = TFloat32.tensorOf(StdArrays.ndCopyOf(new float[][]{features}))) {
      try (TFloat32 result = (TFloat32) session.runner()
              .feed(inputPlaceholder, input)
              .fetch(prediction)
              .run()
              .get(0)) {
        float prob = result.getFloat(0, 0);
        return (int) ((2 * prob - 1) * 1000);
      }
    }
  }

  /**
   * Extracts model features: [dist_curr, dist_opp, walls_curr, walls_opp].
   */
  private float[] getFeatures(GameState state) {
    Player curr = state.getCurrentPlayer();
    Player opp = state.getPlayers().get(1 - state.getPlayers().indexOf(curr));
    return new float[] {
            (float) computeDistanceToWin(state, curr),
            (float) computeDistanceToWin(state, opp),
            (float) curr.getRemainingWalls(),
            (float) opp.getRemainingWalls()
    };
  }

  private TFloat32 prepareInputTensor(List<TrainingData> data) {
    float[][] matrix = new float[data.size()][NUM_FEATURES];
    for (int i = 0; i < data.size(); i++) {
      for (int j = 0; j < NUM_FEATURES; j++) {
        matrix[i][j] = (float) data.get(i).features[j];
      }
    }
    return TFloat32.tensorOf(StdArrays.ndCopyOf(matrix));
  }

  private TFloat32 prepareLabelTensor(List<TrainingData> data) {
    float[][] matrix = new float[data.size()][1];
    for (int i = 0; i < data.size(); i++) {
      matrix[i][0] = (float) data.get(i).outcome;
    }
    return TFloat32.tensorOf(StdArrays.ndCopyOf(matrix));
  }

  @Override
  public void close() {
    if (session != null) session.close();
    if (graph != null) graph.close();
  }

  /**
   * Simulates random games to build a training dataset.
   */
  private List<TrainingData> generateTrainingData() {
    List<TrainingData> trainingData = new ArrayList<>();
    System.out.println("Simulating " + NUM_TRAINING_GAMES + " games...");

    for (int game = 0; game < NUM_TRAINING_GAMES; game++) {
      Board board = new Board(9);
      List<Player> players = Arrays.asList(
              new RandomPlayer("P1", new Position(0, 4)),
              new RandomPlayer("P2", new Position(8, 4))
      );
      GameState state = new GameState(board, players);

      int turn = 0;
      while (!state.isGameOver() && turn < 200) {
        float[] features = getFeatures(state);
        double[] featuresD = new double[]{features[0], features[1], features[2], features[3]};

        Player currPlayer = state.getCurrentPlayer();
        List<Move> moves = state.generateLegalMoves();
        if (!moves.isEmpty()) {
          state.applyMove(moves.get(new Random().nextInt(moves.size())));
          double outcome = state.hasReachedGoal(currPlayer) ? 1.0 : 0.0;
          trainingData.add(new TrainingData(featuresD, outcome));
        }
        turn++;
      }
      if ((game + 1) % 100 == 0) System.out.println("  " + (game + 1) + " games simulated.");
    }
    return trainingData;
  }

  /**
   * BFS to compute the shortest path to the winning row.
   */
  private int computeDistanceToWin(GameState state, Player player) {
    fr.bordeaux.graph.Graph g = state.getBoard().getGraph();
    int start = state.getBoard().id(player.getPosition().getX(), player.getPosition().getY());
    Predicate<Integer> winCond = state.getWinCondition(player);
    List<Integer> path = g.findPathBFS(start, winCond);
    return path.isEmpty() ? 99 : path.size() - 1;
  }

  private static class TrainingData {
    double[] features;
    double outcome;
    TrainingData(double[] features, double outcome) {
      this.features = features;
      this.outcome = outcome;
    }
  }
}