package fr.bordeaux.ai;

import fr.bordeaux.core.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
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

/** Heuristic evaluator using a TensorFlow-based Logistic Regression model. */
@SuppressWarnings({"PMD.CouplingBetweenObjects", "PMD.TooManyMethods"})
/* High coupling is inherent to TensorFlow graph construction and integrated training simulations. */
public class NeuralNetworkEvaluator implements HeuristicEvaluator, AutoCloseable {

  /** PMD string for suppressing Law of Demeter violations. */
  private static final String SUPPRESS_LOD = "PMD.LawOfDemeter";

  /** The number of input features for the neural network. */
  private static final int NUM_FEATURES = 4;

  /** The default number of games to simulate for training. */
  private static final int TRAIN_GAMES = 1000;

  /** The learning rate for the gradient descent optimizer. */
  private static final float LEARNING_RATE = 0.01f;

  /** The default number of training epochs. */
  private static final int DEFAULT_EPOCHS = 150;

  /** The probability of choosing a random move (epsilon-greedy). */
  private static final double EXPLORATION_PROB = 0.1;

  /** Number of training games to simulate. */
  private final int numGames;

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

  /** Default constructor. Uses 1000 games and 150 epochs. */
  public NeuralNetworkEvaluator() {
    this(TRAIN_GAMES, DEFAULT_EPOCHS);
  }

  /**
   * Initializes training with custom epochs.
   *
   * @param epochs Number of training iterations.
   */
  public NeuralNetworkEvaluator(final int epochs) {
    this(TRAIN_GAMES, epochs);
  }

  /**
   * Full constructor for training from scratch.
   *
   * @param numGames Number of simulations to run.
   * @param epochs Gradient descent passes.
   */
  public NeuralNetworkEvaluator(final int numGames, final int epochs) {
    this.numGames = numGames;
    this.epochs = epochs;
    initGraph();
    trainModel();
  }

  /**
   * Constructor for inference using a saved model.
   *
   * @param path Path to the .bin model file.
   * @throws IOException If file is missing or corrupt.
   */
  public NeuralNetworkEvaluator(final String path) throws IOException {
    this.numGames = 0;
    this.epochs = 0;
    initGraph(); // Create the graph structure
    loadModel(path); // Replace random weights with the loaded ones
  }

  /**
   * Defines the Logistic Regression graph: Z = Sigmoid(WX + B).
   *
   * <p>Sets up loss (BCE) and optimizer (SGD).
   */
  @SuppressWarnings(SUPPRESS_LOD)
  /* Impossible to satisfy LOD */
  private void initGraph() {
    // Initialize the computation graph container
    graph = new Graph();
    // Create an operations builder associated with the graph
    final Ops tfOps = Ops.create(graph);

    final MathOps math = tfOps.math;
    final LinalgOps linalg = tfOps.linalg;
    final RandomOps random = tfOps.random;

    // Define entry points for data: Input features [Batch Size, 4]
    inputPlaceholder =
        tfOps.placeholder(TFloat32.class, Placeholder.shape(Shape.of(-1, NUM_FEATURES)));
    // Define entry points for targets: Ground truth labels [Batch Size, 1]
    labelPlaceholder = tfOps.placeholder(TFloat32.class, Placeholder.shape(Shape.of(-1, 1)));

    // Generate initial weights using a truncated normal distribution to break
    // symmetry.
    // Weights range [-2.0, 2.0], mean 0, std 1.0.
    final Operand<TFloat32> randomWeights =
        random.truncatedNormal(tfOps.constant(Shape.of(NUM_FEATURES, 1)), TFloat32.class);
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
    final Operand<TFloat32> logits = math.add(linalg.matMul(inputPlaceholder, weights), bias);

    // Activation: Apply Sigmoid function to squish logits into probabilities [0, 1]
    prediction = math.sigmoid(logits);

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

  /** Core training loop. Prepares tensors and runs SGD Target. */
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
   * Scores a state between -1000 (Loss) and 1000 (Win).
   *
   * @param state The board state to analyze.
   * @return Evaluation score.
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
   * Normalizes features: distances [0,1] and walls [0,1].
   *
   * @param state Game state.
   * @return Feature vector.
   */
  @SuppressWarnings(SUPPRESS_LOD)
  /* Prevents cluttering GameState API with too many specific delegation methods. */
  private float[] getFeatures(final GameState state) {
    final Player curr = state.getCurrentPlayer();
    final Player opp = state.getPlayers().get(1 - state.getPlayers().indexOf(curr));
    final float boardSize = state.getBoard().getSize();
    return new float[] {
      computeDistanceToWin(state, curr) / (boardSize - 1.0f),
      computeDistanceToWin(state, opp) / (boardSize - 1.0f),
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
      final TrainingData sample = data.get(i);
      final double[] featArray = sample.getFeatures();

      for (int j = 0; j < NUM_FEATURES; j++) {
        matrix[i][j] = (float) featArray[j];
      }
    }
    return TFloat32.tensorOf(StdArrays.ndCopyOf(matrix));
  }

  /**
   * Prepares a 2D float tensor representing the batch labels for training.
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
   * Simulates random games to build a training dataset.
   *
   * @return a list of {@link TrainingData}.
   */
  private List<TrainingData> generateTrainingData() {
    final List<TrainingData> trainingData = new ArrayList<>();
    final Random random = new Random(42);

    for (int game = 0; game < numGames; game++) {
      simulateAndRecordGame(trainingData, random);
    }

    Collections.shuffle(trainingData);
    return trainingData;
  }

  /**
   * Simulates a single game and adds the resulting training data.
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
  @SuppressWarnings(SUPPRESS_LOD)
  /* Prevents GameState API bloat over strict LoD. */
  private void recordState(
      final GameState state,
      final Player player1,
      final Player player2,
      final List<double[]> p1History,
      final List<double[]> p2History) {
    final float[] features = getFeatures(state);
    final double[] featuresD = {features[0], features[1], features[2], features[3]};
    final Player curr = state.getCurrentPlayer();

    if (curr.equals(player1)) {
      p1History.add(featuresD);
    } else if (curr.equals(player2)) {
      p2History.add(featuresD);
    }
  }

  /** Selects a move using epsilon-greedy exploration. */
  @SuppressWarnings(SUPPRESS_LOD)
  /* LoD suppressed to avoid GameState bloat. */
  private Move selectExploratoryMove(final GameState state, final Random random) {
    final Move selectedMove;

    if (random.nextDouble() < EXPLORATION_PROB) {
      final List<Move> legalMoves = state.generateLegalMoves();
      final int randomIndex = random.nextInt(legalMoves.size());
      selectedMove = legalMoves.get(randomIndex);
    } else {
      selectedMove = state.getCurrentPlayer().getNextMove(state);
    }

    return selectedMove;
  }

  /** Processes the game result and adds training samples to the dataset. */
  private void recordGameOutcome(
      final GameState state,
      final Player player1,
      final Player player2,
      final List<double[]> p1History,
      final List<double[]> p2History,
      final List<TrainingData> trainingData) {
    if (state.hasReachedGoal(player1)) {
      addSamples(trainingData, p1History, 1.0);
      addSamples(trainingData, p2History, 0.0);
    } else if (state.hasReachedGoal(player2)) {
      addSamples(trainingData, p1History, 0.0);
      addSamples(trainingData, p2History, 1.0);
    }
  }

  /** Adds all states from a history list to the training dataset. */
  private void addSamples(
      final List<TrainingData> data, final List<double[]> history, final double outcome) {
    for (final double[] features : history) {
      data.add(new TrainingData(features, outcome));
    }
  }

  /**
   * BFS distance to the goal line. Returns 99 if blocked.
   *
   * @param state the current game state
   * @param player the player whose distance to goal is being calculated
   * @return the number of steps required to win, or 99 if no path is found
   */
  @SuppressWarnings(SUPPRESS_LOD)
  /* LoD suppressed to avoid GameState bloat. */
  private int computeDistanceToWin(final GameState state, final Player player) {
    final Board board = state.getBoard();
    final fr.bordeaux.graph.Graph gameGraph = board.getGraph();
    final int startNode =
        state.getBoard().id(player.getPosition().getX(), player.getPosition().getY());
    final Predicate<Integer> winCond = state.getWinCondition(player);
    final List<Integer> path = gameGraph.findPathBFS(startNode, winCond);
    return path.isEmpty() ? 99 : path.size() - 1;
  }

  /** Internal data structure representing a single training example. */
  private static final class TrainingData {
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
    private TrainingData(final double[] features, final double outcome) {
      this.features = features;
      this.outcome = outcome;
    }

    private double[] getFeatures() {
      return features;
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
        DataOutputStream dos = new DataOutputStream(Files.newOutputStream(Paths.get(filePath)))) {

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
    try (DataInputStream dis = new DataInputStream(Files.newInputStream(Paths.get(filePath)))) {
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
          Result ignored =
              session
                  .runner()
                  .feed(wPlaceholder, wTensor)
                  .feed(bPlaceholder, bTensor)
                  .addTarget(weightsAssign)
                  .addTarget(biasAssign)
                  .run()) {}
    }
  }
}
