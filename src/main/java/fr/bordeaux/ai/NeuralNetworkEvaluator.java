package fr.bordeaux.ai;

import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Board;
import fr.bordeaux.core.Position;
import fr.bordeaux.core.Move;
import fr.bordeaux.graph.Graph;
import java.util.*;
import java.util.function.Predicate;

/**
 * Evaluates game states using a trained neural network (logistic regression).
 * The model is trained on demand during initialization by simulating random games.
 */
public class NeuralNetworkEvaluator implements HeuristicEvaluator {

  private static final int NUM_TRAINING_GAMES = 500;
  private static final double LEARNING_RATE = 0.01;
  private static final int EPOCHS = 50;

  // ...existing code...
  private double[] weights;
  private double bias;

  public NeuralNetworkEvaluator() {
    System.out.println("Initializing NeuralNetworkEvaluator: training model...");
    trainModel();
    System.out.println("Model training complete!");
  }

  /**
   * Trains the model using data from simulated games.
   */
  private void trainModel() {
    weights = new double[4];
    bias = 0.0;

    // Generate training data
    List<TrainingData> trainingData = generateTrainingData();

    // Train model
    System.out.println("Training with " + trainingData.size() + " samples over " + EPOCHS + " epochs...");

    for (int epoch = 0; epoch < EPOCHS; epoch++) {
      double loss = 0.0;

      for (TrainingData data : trainingData) {
        // Forward pass
        double logit = bias;
        for (int i = 0; i < weights.length; i++) {
          logit += weights[i] * data.features[i];
        }
        double pred = 1.0 / (1.0 + Math.exp(-logit));

        // Compute loss
        double error = pred - data.outcome;
        loss += error * error;

        // Backward pass: update weights
        for (int i = 0; i < weights.length; i++) {
          weights[i] -= LEARNING_RATE * error * data.features[i];
        }
        bias -= LEARNING_RATE * error;
      }

      loss /= trainingData.size();
      if ((epoch + 1) % 10 == 0) {
        System.out.println("  Epoch " + (epoch + 1) + "/" + EPOCHS + ", Loss: " + String.format("%.6f", loss));
      }
    }

    System.out.println("Final weights: " + Arrays.toString(weights) + ", bias: " + bias);
  }

  /**
   * Generates training data by simulating random games.
   */
  private List<TrainingData> generateTrainingData() {
    List<TrainingData> trainingData = new ArrayList<>();
    System.out.println("Generating training data from " + NUM_TRAINING_GAMES + " simulated games...");

    for (int game = 0; game < NUM_TRAINING_GAMES; game++) {
      Board board = new Board(9);
      List<Player> players = new ArrayList<>();
      players.add(new RandomPlayer("P1", new Position(0, 4)));
      players.add(new RandomPlayer("P2", new Position(8, 4)));
      GameState state = new GameState(board, players);

      int turn = 0;
      while (!state.isGameOver() && turn < 200) {
        float distCurr = (float) computeDistanceToWin(state, state.getCurrentPlayer());
        float distOpp = (float) computeDistanceToWin(state, state.getPlayers().get(1 - state.getPlayers().indexOf(state.getCurrentPlayer())));
        float wallsCurr = (float) state.getCurrentPlayer().getRemainingWalls();
        float wallsOpp = (float) state.getPlayers().get(1 - state.getPlayers().indexOf(state.getCurrentPlayer())).getRemainingWalls();

        double[] features = {distCurr, distOpp, wallsCurr, wallsOpp};

        Player currPlayer = state.getCurrentPlayer();
        List<Move> moves = state.generateLegalMoves();
        if (!moves.isEmpty()) {
          Move move = moves.get(new Random().nextInt(moves.size()));
          state.applyMove(move);
          double outcome = state.hasReachedGoal(currPlayer) ? 1.0 : 0.0;
          trainingData.add(new TrainingData(features, outcome));
        }

        turn++;
      }

      if ((game + 1) % 100 == 0) {
        System.out.println("  Generated " + (game + 1) + " games...");
      }
    }

    System.out.println("Total training samples: " + trainingData.size());
    return trainingData;
  }

  @Override
  public int evaluate(GameState state) {
    List<Player> players = state.getPlayers();
    if (players.size() != 2) {
      throw new IllegalArgumentException("Only 2 players supported");
    }
    Player curr = state.getCurrentPlayer();
    Player opp = players.get(1 - players.indexOf(curr));

    float distCurr = (float) computeDistanceToWin(state, curr);
    float distOpp = (float) computeDistanceToWin(state, opp);
    float wallsCurr = (float) curr.getRemainingWalls();
    float wallsOpp = (float) opp.getRemainingWalls();

    // Features: [dist_curr, dist_opp, walls_curr, walls_opp]
    double[] features = {distCurr, distOpp, wallsCurr, wallsOpp};

    // Logistic regression
    double logit = bias;
    for (int i = 0; i < weights.length; i++) {
      logit += weights[i] * features[i];
    }
    double prob = 1.0 / (1.0 + Math.exp(-logit));

    // Scale to [-1000, 1000]
    return (int) ((2 * prob - 1) * 1000);
  }

  /**
   * Computes distance from player to win using BFS.
   */
  private int computeDistanceToWin(GameState state, Player player) {
    Graph graph = state.getBoard().getGraph();
    int start = state.getBoard().id(player.getPosition().getX(), player.getPosition().getY());

    Predicate<Integer> winCond = state.getWinCondition(player);
    List<Integer> path = graph.findPathBFS(start, winCond);
    return path.isEmpty() ? Integer.MAX_VALUE : path.size() - 1;
  }

  public void close() {
    // No resources to close
  }

  /**
   * Training data container.
   */
  private static class TrainingData {
    double[] features;
    double outcome;

    TrainingData(double[] features, double outcome) {
      this.features = features;
      this.outcome = outcome;
    }
  }
}
