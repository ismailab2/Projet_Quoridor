package fr.bordeaux.ai;

import fr.bordeaux.core.*;
import java.io.*;
import java.util.*;
import java.util.function.Predicate;
import org.tensorflow.*;
import org.tensorflow.framework.losses.BinaryCrossentropy;
import org.tensorflow.framework.optimizers.GradientDescent;
import org.tensorflow.framework.optimizers.Optimizer;
import org.tensorflow.ndarray.*;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.op.*;
import org.tensorflow.op.core.*;
import org.tensorflow.types.*;

/**
 * Heuristic evaluator using a TensorFlow-based Logistic Regression model. Trained via random game
 * simulations during initialization.
 */
public class NeuralNetworkEvaluator implements HeuristicEvaluator, AutoCloseable {

  /** The number of input features for the neural network. */
  private static final int NUM_FEATURES = 4;

  /** The default number of games to simulate for training. */
  private static final int DEFAULT_TRAIN_GAMES = 1000;

  /** The learning rate for the gradient descent optimizer. */
  private static final float LEARNING_RATE = 0.01f;

  /** The default number of training epochs. */
  private static final int DEFAULT_EPOCHS = 150;

  /** Number of training games to simulate. */
  private final int numTrainingGames;

  /** Number of training epochs. */
  private final int epochs;

  /** The TensorFlow computation graph. */
  private Graph graph;

  /** The TensorFlow session for executing operations. */
  private Session session;

  /** Placeholder for input features during training and inference. */
  private Placeholder<TFloat32> inputPlaceholder;

  /** Placeholder for target labels during training. */
  private Placeholder<TFloat32> labelPlaceholder;

  /** The prediction operation (sigmoid output). */
  private Operand<TFloat32> prediction;

  /** The training operation (gradient descent step). */
  private Op trainOp;

  /** Placeholder for feeding weights during model loading. */
  private Placeholder<TFloat32> wPlaceholder;

  /** Placeholder for feeding bias during model loading. */
  private Placeholder<TFloat32> bPlaceholder;

  /** Operation to assign values to the weights variable. */
  private Op weightsAssign;

  /** Operation to assign values to the bias variable. */
  private Op biasAssign;

  /**
   * Initializes the graph and trains the model using the default number of training games ({@value
   * DEFAULT_TRAIN_GAMES}) and epochs ({@value DEFAULT_EPOCHS}).
   */
  public NeuralNetworkEvaluator() {
    this(DEFAULT_TRAIN_GAMES, DEFAULT_EPOCHS);
  }

  /**
   * Initializes the graph and trains the model for the given number of epochs, using the default
   * number of training games ({@value DEFAULT_TRAIN_GAMES}).
   *
   * @param epochs number of gradient-descent passes over the training data
   */
  public NeuralNetworkEvaluator(final int epochs) {
    this(DEFAULT_TRAIN_GAMES, epochs);
  }

  /**
   * Initializes the graph and trains the model with full control over both hyperparameters.
   *
   * @param numTrainingGames number of random games to simulate for training data
   * @param epochs number of gradient-descent passes over the training data
   */
  public NeuralNetworkEvaluator(final int numTrainingGames, final int epochs) {
    this.numTrainingGames = numTrainingGames;
    this.epochs = epochs;
    initGraph();
    trainModel();
  }

  /**
   * Initializes the evaluator by loading a pre-trained model from the specified file.
   *
   * @param modelPath the path to the pre-trained model file
   * @throws IOException if an error occurs while reading the model file
   */
  public NeuralNetworkEvaluator(final String modelPath) throws IOException {
    this.numTrainingGames = 0;
    this.epochs = 0;
    initGraph(); // Create the graph structure
    loadModel(modelPath); // Replace random weights with the loaded ones
  }

  /**
   * Initializes the TensorFlow computation graph. This method defines the model architecture
   * (Logistic Regression), the loss function (Binary Cross-Entropy), and the optimization strategy
   * (Gradient Descent).
   */
  private void initGraph() {
    // Initialize the computation graph container
    graph = new Graph();
    // Create an operations builder associated with the graph
    final Ops tfOps = Ops.create(graph);

    // Define entry points for data: Input features [Batch Size, 4]
    inputPlaceholder =
        tfOps.placeholder(TFloat32.class, Placeholder.shape(Shape.of(-1, NUM_FEATURES)));
    // Define entry points for targets: Ground truth labels [Batch Size, 1]
    labelPlaceholder = tfOps.placeholder(TFloat32.class, Placeholder.shape(Shape.of(-1, 1)));

    // Generate initial weights using a truncated normal distribution to break
    // symmetry.
    // Weights range [-2.0, 2.0], mean 0, std 1.0.
    final Operand<TFloat32> randomWeights =
        tfOps.random.truncatedNormal(tfOps.constant(Shape.of(NUM_FEATURES, 1)), TFloat32.class);
    // Create a trainable variable for weights (W)
    final Variable<TFloat32> weights = tfOps.withName("weights").variable(randomWeights);
    // Create a trainable variable for bias (b), initialized to zero
    final Variable<TFloat32> bias =
        tfOps.withName("bias").variable(tfOps.zeros(tfOps.constant(Shape.of(1)), TFloat32.class));

    // Define assignment operations for loading pre-trained weights
    wPlaceholder = tfOps.placeholder(TFloat32.class, Placeholder.shape(Shape.of(NUM_FEATURES, 1)));
    bPlaceholder = tfOps.placeholder(TFloat32.class, Placeholder.shape(Shape.of(1)));
    weightsAssign = tfOps.assign(weights, wPlaceholder);
    biasAssign = tfOps.assign(bias, bPlaceholder);

    // Linear Transformation: Compute logits as (Input * Weights) + Bias
    final Operand<TFloat32> logits =
        tfOps.math.add(tfOps.linalg.matMul(inputPlaceholder, weights), bias);

    // Activation: Apply Sigmoid function to squish logits into probabilities [0, 1]
    prediction = tfOps.math.sigmoid(logits);

    // Loss Function: Initialize Binary Cross-Entropy for classification
    final BinaryCrossentropy bce = new BinaryCrossentropy(true);
    // Calculate the mean loss between the predicted values and actual labels
    final Operand<TFloat32> meanLoss = bce.call(tfOps, labelPlaceholder, logits);

    // Optimizer: Initialize Gradient Descent with the specified learning rate
    final Optimizer gradDesc = new GradientDescent(graph, LEARNING_RATE);
    // Minimize the loss function by computing and applying gradients during
    // training
    trainOp = gradDesc.minimize(meanLoss);

    // Execute the initialization of variables within the graph
    session = new Session(graph);
    session.runner().addTarget(weights).addTarget(bias).run();
  }

  /**
   * Performs training over specified epochs. This method generates training data via simulation and
   * then optimizes the weights using Gradient Descent.
   */
  private void trainModel() {
    final List<TrainingData> dataList = generateTrainingData();
    if (dataList.isEmpty()) {
      return;
    }

    try (TFloat32 inputTensor = prepareInputTensor(dataList);
        TFloat32 labelTensor = prepareLabelTensor(dataList)) {

      // Run the training loop for the specified number of epochs
      for (int epoch = 0; epoch < epochs; epoch++) {
        session
            .runner()
            // Bind the input features tensor to the graph's input placeholder
            .feed(inputPlaceholder, inputTensor)
            // Bind the ground truth labels tensor to the graph's label placeholder
            .feed(labelPlaceholder, labelTensor)
            // Register the training operation as the target to execute
            // (runs one gradient descent step)
            .addTarget(trainOp)
            // Execute all registered operations in the graph
            .run();
      }
    }
  }

  /**
   * Evaluates the current game state using the trained Neural Network model.
   *
   * @param state the current game state to evaluate
   * @return an integer score in the range [-1000, 1000] mapping to the predicted probability
   */
  @Override
  public int evaluate(final GameState state) {
    final float[] features = getFeatures(state);

    try (TFloat32 input = TFloat32.tensorOf(StdArrays.ndCopyOf(new float[][] {features}));
        Result result = session.runner().feed(inputPlaceholder, input).fetch(prediction).run()) {
      try (TFloat32 output = (TFloat32) result.get(0)) {
        final float prob = output.getFloat(0, 0);
        return (int) ((2 * prob - 1) * 1000);
      }
    }
  }

  /**
   * Extracts quantitative features from the game state for the Neural Network. Features:
   * [distance_current_player, distance_opponent, remaining_walls_current,
   * remaining_walls_opponent].
   *
   * @param state the current game state
   * @return an array of float features
   */
  private float[] getFeatures(final GameState state) {
    final Player curr = state.getCurrentPlayer();
    final Player opp = state.getPlayers().get(1 - state.getPlayers().indexOf(curr));
    final float boardSize = state.getBoard().getSize();
    return new float[] {
      (float) computeDistanceToWin(state, curr) / (boardSize - 1.0f),
      (float) computeDistanceToWin(state, opp) / (boardSize - 1.0f),
      curr.getRemainingWalls() / 10f,
      opp.getRemainingWalls() / 10f
    };
  }

  /**
   * Prepares a 2D float tensor representing the batch input features for training.
   *
   * @param data the list of {@link TrainingData} generated from simulations
   * @return a {@link TFloat32} tensor containing the input data
   */
  private TFloat32 prepareInputTensor(final List<TrainingData> data) {
    final float[][] matrix = new float[data.size()][NUM_FEATURES];
    for (int i = 0; i < data.size(); i++) {
      for (int j = 0; j < NUM_FEATURES; j++) {
        matrix[i][j] = (float) data.get(i).features[j];
      }
    }
    return TFloat32.tensorOf(StdArrays.ndCopyOf(matrix));
  }

  /**
   * Prepares a 2D float tensor representing the batch labels (outcomes) for training.
   *
   * @param data the list of {@link TrainingData} generated from simulations
   * @return a {@link TFloat32} tensor containing the target labels
   */
  private TFloat32 prepareLabelTensor(final List<TrainingData> data) {
    final float[][] matrix = new float[data.size()][1];
    for (int i = 0; i < data.size(); i++) {
      matrix[i][0] = (float) data.get(i).outcome;
    }
    return TFloat32.tensorOf(StdArrays.ndCopyOf(matrix));
  }

  /** Releases resources associated with the TensorFlow graph and session. */
  @Override
  public void close() {
    if (session != null) {
      session.close();
    }
    if (graph != null) {
      graph.close();
    }
  }

  /**
   * Simulates random games to build a training dataset. Players take random legal moves until the
   * game ends or a turn limit is reached.
   *
   * @return a list of {@link TrainingData} containing feature arrays and game outcomes
   */
  private List<TrainingData> generateTrainingData() {
    final List<TrainingData> trainingData = new ArrayList<>();
    final Random random = new Random(42);

    for (int game = 0; game < numTrainingGames; game++) {
      simulateAndRecordGame(trainingData, random);
      if ((game + 1) % 100 == 0) {
        // Log periodically
      }
    }

    Collections.shuffle(trainingData);
    return trainingData;
  }

  /**
   * Simulates a single game and adds the resulting training data if there is a decisive winner.
   *
   * @param trainingData the list to append training samples to
   * @param random the random number generator for exploration
   */
  private void simulateAndRecordGame(final List<TrainingData> trainingData, final Random random) {
    final Board board = new Board(9);
    final Player player1 =
        MinimaxPlayer.builder("P1", new Position(0, 4)).withDepth(1).withReflexion(1).build();
    final Player player2 =
        MinimaxPlayer.builder("P2", new Position(8, 4)).withDepth(1).withReflexion(1).build();

    final List<Player> players = Arrays.asList(player1, player2);
    final GameState state = new GameState(board, players);

    final List<double[]> p1History = new ArrayList<>();
    final List<double[]> p2History = new ArrayList<>();

    int turn = 0;
    while (turn < 250 && !state.isGameOver()) {
      recordState(state, player1, player2, p1History, p2History);
      final Move nextMove = selectExploratoryMove(state, random);
      final Player movingPlayer = state.getCurrentPlayer();
      state.applyMove(nextMove);

      if (state.hasReachedGoal(movingPlayer)) {
        state.setGameOver(true);
      }
      turn++;
    }

    recordGameOutcome(state, player1, player2, p1History, p2History, trainingData);
  }

  /** Records the features of the current state for the player who is about to move. */
  private void recordState(
      final GameState state,
      final Player p1,
      final Player p2,
      final List<double[]> p1History,
      final List<double[]> p2History) {
    final float[] features = getFeatures(state);
    final double[] featuresD = new double[] {features[0], features[1], features[2], features[3]};
    final Player curr = state.getCurrentPlayer();

    if (curr.equals(p1)) {
      p1History.add(featuresD);
    } else {
      p2History.add(featuresD);
    }
  }

  /** Selects a move using epsilon-greedy exploration. */
  private Move selectExploratoryMove(final GameState state, final Random random) {
    if (random.nextDouble() < 0.1) {
      final List<Move> legalMoves = state.generateLegalMoves();
      return legalMoves.get(random.nextInt(legalMoves.size()));
    }
    return state.getCurrentPlayer().getNextMove(state);
  }

  /** Processes the game result and adds training samples to the dataset. */
  private void recordGameOutcome(
      final GameState state,
      final Player p1,
      final Player p2,
      final List<double[]> p1History,
      final List<double[]> p2History,
      final List<TrainingData> trainingData) {
    if (state.hasReachedGoal(p1)) {
      addSamples(trainingData, p1History, 1.0);
      addSamples(trainingData, p2History, 0.0);
    } else if (state.hasReachedGoal(p2)) {
      addSamples(trainingData, p1History, 0.0);
      addSamples(trainingData, p2History, 1.0);
    }
  }

  /** Adds all states from a history list to the training dataset with a common outcome. */
  private void addSamples(
      final List<TrainingData> data, final List<double[]> history, final double outcome) {
    for (final double[] features : history) {
      data.add(new TrainingData(features, outcome));
    }
  }

  /**
   * Computes the distance (number of steps) a player must take to reach their winning row using a
   * Breadth-First Search (BFS) algorithm.
   *
   * @param state the current game state
   * @param player the player whose distance to goal is being calculated
   * @return the number of steps required to win, or 99 if no path is found
   */
  private int computeDistanceToWin(final GameState state, final Player player) {
    final fr.bordeaux.graph.Graph gameGraph = state.getBoard().getGraph();
    final int startNode =
        state.getBoard().id(player.getPosition().getX(), player.getPosition().getY());
    final Predicate<Integer> winCond = state.getWinCondition(player);
    final List<Integer> path = gameGraph.findPathBFS(startNode, winCond);
    return path.isEmpty() ? 99 : path.size() - 1;
  }

  /** Internal data structure representing a single training example. */
  private static class TrainingData {
    /** The feature array corresponding to a specific game state. */
    private final double[] features;

    /** The observed outcome of the simulated game (e.g. 1.0 for win, 0.0 for loss). */
    private final double outcome;

    /**
     * Constructs a new TrainingData instance.
     *
     * @param features an array of numerical features evaluating the position
     * @param outcome the float outcome representing win/loss
     */
    TrainingData(double[] features, double outcome) {
      this.features = features;
      this.outcome = outcome;
    }
  }

  /**
   * Saves the current model weights and bias to a file.
   *
   * @param filePath the path where the model should be saved
   * @throws IOException if an error occurs while writing the file
   */
  public void saveModel(final String filePath) throws IOException {
    try (Result wRes = session.runner().fetch("weights").run();
        TFloat32 wTensor = (TFloat32) wRes.get(0);
        Result bRes = session.runner().fetch("bias").run();
        TFloat32 bTensor = (TFloat32) bRes.get(0);
        DataOutputStream dos = new DataOutputStream(new FileOutputStream(filePath))) {

      for (int i = 0; i < NUM_FEATURES; i++) {
        dos.writeFloat(wTensor.getFloat(i, 0));
      }
      dos.writeFloat(bTensor.getFloat(0));
    }
  }

  /**
   * Loads model weights and bias from a file and injects them into the graph.
   *
   * @param filePath the path to the model file to load
   * @throws IOException if an error occurs while reading the file
   */
  public final void loadModel(final String filePath) throws IOException {
    try (DataInputStream dis = new DataInputStream(new FileInputStream(filePath))) {
      final float[] wArray = new float[NUM_FEATURES];
      for (int i = 0; i < NUM_FEATURES; i++) {
        wArray[i] = dis.readFloat();
      }
      final float biasVal = dis.readFloat();

      final float[][] matrix4x1 = new float[NUM_FEATURES][1];
      for (int i = 0; i < NUM_FEATURES; i++) {
        matrix4x1[i][0] = wArray[i];
      }

      try (TFloat32 wTensor = TFloat32.tensorOf(StdArrays.ndCopyOf(matrix4x1));
          TFloat32 bTensor = TFloat32.tensorOf(StdArrays.ndCopyOf(new float[] {biasVal}));
          Result result =
              session
                  .runner()
                  .feed(wPlaceholder, wTensor)
                  .feed(bPlaceholder, bTensor)
                  .addTarget(weightsAssign)
                  .addTarget(biasAssign)
                  .run()) {
        // Runner executed
      }
    }
  }
}
