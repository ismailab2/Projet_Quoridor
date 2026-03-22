package fr.bordeaux.ai;

import fr.bordeaux.core.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import org.tensorflow.*;
import org.tensorflow.ndarray.*;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.op.*;
import org.tensorflow.op.core.*;
import org.tensorflow.types.*;

/** Heuristic evaluator using a TensorFlow-based Logistic Regression model. */
public class NeuralNetworkEvaluator implements HeuristicEvaluator, AutoCloseable {

  /** PMD string for suppressing Law of Demeter violations. */
  /* package */ static final String SUPPRESS_LOD = "PMD.LawOfDemeter";

  /** The default number of training epochs. */
  private static final int DEFAULT_EPOCHS = 750;

  /** The default number of games to simulate for training. */
  private static final int DEFAULT_GAMES = 5000;

  /** The number of input features for the neural network. */
  /* package */ static final int NUM_FEATURES = 4;

  /** The TensorFlow computation graph. */
  private Graph graph;

  /** The TensorFlow session for executing operations. */
  private Session session;

  /** Placeholder for input features during training and inference. */
  private Placeholder<TFloat32> inputPlaceholder;

  /** The prediction operation (sigmoid output). */
  private Operand<TFloat32> prediction;

  /** Placeholder for feeding weights during model loading. */
  /* package */ Placeholder<TFloat32> wPlaceholder;

  /** Placeholder for feeding bias during model loading. */
  /* package */ Placeholder<TFloat32> bPlaceholder;

  /** Operation to assign values to the weights variable. */
  /* package */ Op weightsAssign;

  /** Operation to assign values to the bias variable. */
  /* package */ Op biasAssign;

  /** Default constructor. Uses 5000 games and 500 epochs. */
  public NeuralNetworkEvaluator() {
    this(DEFAULT_GAMES, DEFAULT_EPOCHS);
  }

  /**
   * Full constructor for training from scratch.
   *
   * @param numGames Number of simulations to run.
   * @param epochs Gradient descent passes.
   */
  public NeuralNetworkEvaluator(final int numGames, final int epochs) {
    initGraph();
    final NeuralNetworkTrainer trainer = new NeuralNetworkTrainer();
    trainer.trainModel(this, numGames, epochs);
  }

  @SuppressWarnings(SUPPRESS_LOD)
  /** Initializes the TensorFlow graph for the neural network. */
  private void initGraph() {
    this.graph = new Graph();
    final Ops tfOps = Ops.create(graph);

    inputPlaceholder =
        tfOps.placeholder(TFloat32.class, Placeholder.shape(Shape.of(-1, NUM_FEATURES)));

    final Operand<TInt64> shapeConst = tfOps.constant(Shape.of(NUM_FEATURES, 1));
    final Variable<TFloat32> weights =
        tfOps
            .withName("weights")
            .variable(tfOps.random.truncatedNormal(shapeConst, TFloat32.class));

    final Operand<TInt64> biasConst = tfOps.constant(Shape.of(1));
    final Variable<TFloat32> bias =
        tfOps.withName("bias").variable(tfOps.zeros(biasConst, TFloat32.class));

    wPlaceholder = tfOps.placeholder(TFloat32.class, Placeholder.shape(Shape.of(NUM_FEATURES, 1)));
    bPlaceholder = tfOps.placeholder(TFloat32.class, Placeholder.shape(Shape.of(1)));
    weightsAssign = tfOps.assign(weights, wPlaceholder);
    biasAssign = tfOps.assign(bias, bPlaceholder);

    final Operand<TFloat32> matMul = tfOps.linalg.matMul(inputPlaceholder, weights);
    final Operand<TFloat32> add = tfOps.math.add(matMul, bias);
    prediction = tfOps.math.sigmoid(add);

    this.session = new Session(graph);

    session.runner().addTarget(weights).addTarget(bias).run();
  }

  /**
   * Factory method to implicitly load the default bundled model from the classpath.
   *
   * @return A NeuralNetworkEvaluator initialized with the default weights.
   * @throws IOException If the resource is missing.
   */
  public static NeuralNetworkEvaluator loadDefault() throws IOException {
    final InputStream inputstream =
        NeuralNetworkEvaluator.class.getResourceAsStream("/nnmodels/default_quoridor_model.bin");
    if (inputstream == null) {
      throw new FileNotFoundException(
          "Default model not found in classpath: /nnmodels/default_quoridor_model.bin");
    }
    return new NeuralNetworkEvaluator(inputstream);
  }

  /**
   * Constructor for inference using a saved model from an InputStream.
   *
   * @param inputstream InputStream containing the .bin model binary.
   * @throws IOException If stream is unreadable or corrupt.
   */
  public NeuralNetworkEvaluator(final InputStream inputstream) throws IOException {
    initGraph();
    NeuralNetworkTrainer.loadModel(this, inputstream);
  }

  /**
   * Constructor for inference using a saved model from a file path.
   *
   * @param path Path to the .bin model file.
   * @throws IOException If file is missing or corrupt.
   */
  public NeuralNetworkEvaluator(final String path) throws IOException {
    initGraph();
    NeuralNetworkTrainer.loadModel(this, path);
  }

  /**
   * Saves the model to a file.
   *
   * @param path Path to save the .bin model file.
   * @throws IOException If file is missing or corrupt.
   */
  public void saveModel(final String path) throws IOException {
    try (Result wRes = session.runner().fetch("weights").run();
        TFloat32 wTensor = (TFloat32) wRes.get(0);
        Result bRes = session.runner().fetch("bias").run();
        TFloat32 bTensor = (TFloat32) bRes.get(0);
        DataOutputStream dos = new DataOutputStream(Files.newOutputStream(Paths.get(path)))) {
      for (int i = 0; i < NUM_FEATURES; i++) {
        dos.writeFloat(wTensor.getFloat(i, 0));
      }
      dos.writeFloat(bTensor.getFloat(0));
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
  protected float[] getFeatures(final GameState state) {
    final Player curr = state.getCurrentPlayer();
    final Player opp = state.getPlayers().get(1 - state.getPlayers().indexOf(curr));
    final float boardSize = state.getBoard().getSize();
    final float maxDist = boardSize - 1.0f;

    // 1. Current Player Proximity: 1.0 means the player has reached the goal,
    // 0.0 means they are at the starting line.
    final float myProx = 1.0f - (computeDistanceToWin(state, curr) / maxDist);

    // 2. Opponent Distance: 1.0 means the opponent is far from their goal,
    // 0.0 means they are about to win. Higher is better for the current player.
    final float oppDist = computeDistanceToWin(state, opp) / maxDist;

    // 3. Remaining Walls: Number of walls the current player has left.
    final float currWalls = curr.getRemainingWalls() / 10.0f;

    // 4. Remaining Walls: Number of walls the opponent has left.
    final float oppWalls = opp.getRemainingWalls() / 10.0f;

    return new float[] {myProx, oppDist, currWalls, oppWalls};
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
   * BFS distance to the goal line. Returns 20 if blocked.
   *
   * @param state the current game state
   * @param player the player whose distance to goal is being calculated
   * @return the number of steps required to win, or 20 if no path is found
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
    return path.isEmpty() ? 20 : path.size() - 1;
  }

  /**
   * Returns the session used by the neural network.
   *
   * @return the session
   */
  protected Session getSession() {
    return session;
  }

  /**
   * Returns the graph used by the neural network.
   *
   * @return the graph
   */
  protected Graph getGraph() {
    return graph;
  }

  /**
   * Returns the input placeholder used by the neural network.
   *
   * @return the input placeholder
   */
  protected Placeholder<TFloat32> getInputPlaceholder() {
    return inputPlaceholder;
  }

  /**
   * Returns the prediction used by the neural network.
   *
   * @return the prediction
   */
  protected Operand<TFloat32> getPrediction() {
    return prediction;
  }
}
