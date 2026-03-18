package fr.bordeaux.ai;

import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Move;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;
import fr.bordeaux.core.RuleChecker;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Represents an AI player using the Monte Carlo Tree Search algorithm. */
@SuppressWarnings({"PMD.TooManyMethods", "PMD.LawOfDemeter"})
public class MctsPlayer extends AiPlayer {

  /** Reflection time allowed for the AI in milliseconds. */
  private final long reflectionTime;

  /** Neural Network evaluator for selection (null for UCT). */
  private final NeuralNetworkEvaluator evaluator;

  /** Constant direction offsets for orthogonal movement. */
  private static final int[][] DIRECTIONS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

  /** Diagonal direction offsets for jumps only. */
  private static final int[][] DIAG_DIRS = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

  /** Maximum number of turns allowed in a single simulation playout. */
  private static final int MAX_TURNS = 200;

  /** UCT constant for exploration vs exploitation balance. */
  private static final double UCT_CONSTANT = 1.41;

  /** Constructor with custom reflection time. */
  public MctsPlayer(final String name, final Position positionStart, final long reflectionTime) {
    super(name, positionStart);
    this.reflectionTime = reflectionTime;
    this.evaluator = null;
  }

  /** Default constructor with 2000ms reflection time. */
  public MctsPlayer(final String name, final Position positionStart) {
    this(name, positionStart, 2000);
  }

  /** Constructor with custom reflection time and neural network evaluator. */
  public MctsPlayer(
      final String name,
      final Position positionStart,
      final long reflectionTime,
      final NeuralNetworkEvaluator evaluator) {
    super(name, positionStart);
    this.reflectionTime = reflectionTime;
    this.evaluator = evaluator;
  }

  @Override
  public Player copy() {
    return new MctsPlayer(
        this.getName(),
        this.getPosition(),
        this.getStartingPosition(),
        this.getRemainingWalls(),
        this.getScore(),
        this.reflectionTime);
  }

  private MctsPlayer(
      final String name,
      final Position current,
      final Position start,
      final int walls,
      final int score,
      final long reflectionTime) {
    super(name, current, start, walls, score);
    this.reflectionTime = reflectionTime;
    this.evaluator = null;
  }

  /** Inner class representing a node within the MCTS search tree. */
  public static class MctsNode {

    /** The game state associated with this node. */
    private final GameState state;

    /** The move that led to this node. */
    private final Move move;

    /** The parent node in the tree. */
    private final MctsNode parent;

    /** List of child nodes. */
    private final List<MctsNode> children = new ArrayList<>();

    /** List of legal moves that haven't been explored yet. */
    private final List<Move> untriedMoves;

    /** Total number of wins backpropagated to this node. */
    private int wins;

    /** Total number of times this node has been visited. */
    private int visits;

    /** Constructor for the root node. */
    public MctsNode(final GameState state) {
      this(state, null, null);
    }

    /** Constructor for child nodes. */
    public MctsNode(final GameState state, final Move move, final MctsNode parent) {
      this.state = state;
      this.move = move;
      this.parent = parent;
      this.untriedMoves = new ArrayList<>(state.generatePseudoLegalMoves());
      Collections.shuffle(this.untriedMoves);
    }

    public boolean hasUntriedLegalMoves() {
      while (!untriedMoves.isEmpty()) {
        if (RuleChecker.isMoveLegal(state, untriedMoves.getLast())) {
          return true;
        }
        untriedMoves.removeLast();
      }
      return false;
    }

    /** Performs MCTS iterations until the reflection time limit is reached. */
    public Move getBestMoveAfterSearch(
        final long limit, final int myIdx, final NeuralNetworkEvaluator evaluator) {
      final long start = System.currentTimeMillis();
      while (System.currentTimeMillis() - start < limit) {
        MctsNode current = this;
        while (!current.state.isGameOver() && !current.hasUntriedLegalMoves()) {
          if (current.children.isEmpty()) break;
          current = bestUCT(current, myIdx, evaluator);
        }
        if (!current.state.isGameOver() && current.hasUntriedLegalMoves()) {
          current = current.expand();
        }
        if (current != null) {
          backpropagate(current, current.simulate(), myIdx);
        }
      }
      return this.extractBestMove();
    }

    private Move extractBestMove() {
      Move result = null;
      if (!children.isEmpty()) {
        MctsNode bestNode = children.getFirst();
        for (final MctsNode n : children) {
          if (n.visits > bestNode.visits) {
            bestNode = n;
          }
        }
        result = bestNode.move;
      }
      return result;
    }

    /** Expands the tree by creating a new child node from an untried move. */
    public MctsNode expand() {
      final Move nextMove = untriedMoves.removeLast();
      final GameState nextState = state.copy();
      final Player currentPlayer = nextState.getCurrentPlayer();
      nextState.applyMove(nextMove);
      if (nextState.hasReachedGoal(currentPlayer)) {
        nextState.setGameOver(true);
      }
      final MctsNode newNode = new MctsNode(nextState, nextMove, this);
      children.add(newNode);
      return newNode;
    }

    /** Simulates a random game. Optimized for high simulation counts. */
    public int simulate() {
      final GameState temp = state.copy();
      int winnerResult;

      if (temp.isGameOver()) {
        winnerResult = temp.getPreviousIndexPlayer();
      } else {
        final int totalPlayers = temp.getPlayers().size();
        final List<Player> allPlayers = temp.getPlayers();
        winnerResult = -1;
        int turns = 0;

        while (winnerResult == -1 && turns < 200) {
          winnerResult = simulateOneStep(temp, allPlayers, totalPlayers);
          turns++;
        }
      }

      return winnerResult;
    }

    /** Simulates one step of the game. */
    private int simulateOneStep(
        final GameState temp, final List<Player> allPlayers, final int totalPlayers) {
      final Player currentPlayer = temp.getCurrentPlayer();
      final Position currentPos = currentPlayer.getPosition();
      final int curX = currentPos.getX();
      final int curY = currentPos.getY();

      final List<Move> legalMoves = new ArrayList<>(12);
      int posIndex = 0;

      // 4 orthogonal steps and jumps (8 moves)
      for (final int[] dir : DIRECTIONS) {
        // Step move (1 cell in direction)
        Position stepPos = new Position(curX + dir[0], curY + dir[1]);
        final Move stepMove = Move.pawn(currentPos, stepPos);
        if (RuleChecker.isMoveLegal(temp, stepMove)) {
          legalMoves.add(stepMove);
        }
        posIndex++;

        // Jump move (2 cells in direction)
        Position jumpPos = new Position(curX + (dir[0] * 2), curY + (dir[1] * 2));
        final Move jumpMove = Move.pawn(currentPos, jumpPos);
        if (RuleChecker.isMoveLegal(temp, jumpMove)) {
          legalMoves.add(jumpMove);
        }
        posIndex++;
      }

      // 4 diagonal steps only (1 cell in diagonal direction)
      for (final int[] dir : DIAG_DIRS) {
        Position diagPos = new Position(curX + dir[0], curY + dir[1]);
        final Move diagonalStep = Move.pawn(currentPos, diagPos);
        if (RuleChecker.isMoveLegal(temp, diagonalStep)) {
          legalMoves.add(diagonalStep);
        }
      }

      if (legalMoves.isEmpty()) {
        return -1;
      }

      final Move selectedMove = legalMoves.get((int) (Math.random() * legalMoves.size()));
      final int lastIdx = temp.getCurrentIndexPlayer();
      temp.applyMoveWithoutClearingRedo(selectedMove);

      // Victory check
      if (temp.hasReachedGoal(allPlayers.get(lastIdx))) {
        return lastIdx;
      }

      return -1;
    }

    /** Returns the current index player (alias for getPlayerIndex). */
    public int getCurrentIndexPlayer() {
      return state.getCurrentIndexPlayer();
    }

    /** Returns the list of child nodes. */
    public List<MctsNode> getChildren() {
      return children;
    }

    /** Returns the parent node. */
    public MctsNode getParent() {
      return parent;
    }

    /** Increments the visit count. */
    public void incrementVisits() {
      this.visits++;
    }

    /** Increments the win count. */
    public void incrementWins() {
      this.wins++;
    }
  }

  @Override
  public Move getNextMove(final GameState state) {
    final int myIdx = state.getPlayers().indexOf(this);
    return new MctsNode(state.copy())
        .getBestMoveAfterSearch(this.reflectionTime, myIdx, this.evaluator);
  }

  /**
   * Provides a hint for the best move for a given player in the given game state. Uses default
   * reflection time of 2000ms and no state evaluator.
   *
   * @param player The player for whom to find the best move.
   * @param state The current game state.
   * @return The best move for the player, or null if no move is available.
   */
  public static Move hint(final Player player, final GameState state) {
    return hint(player, state, 2000, false);
  }

  /**
   * Provides a hint for the best move for a given player in the given game state.
   *
   * @param player The player for whom to find the best move.
   * @param state The current game state.
   * @param reflectionTimeMs The reflection time in milliseconds.
   * @return The best move for the player, or null if no move is available.
   */
  public static Move hint(final Player player, final GameState state, final long reflectionTimeMs) {
    return hint(player, state, reflectionTimeMs, false);
  }

  /**
   * Provides a hint for the best move for a given player in the given game state.
   *
   * @param player The player for whom to find the best move.
   * @param state The current game state.
   * @param useNN Whether to use the neural network evaluator.
   * @return The best move for the player, or null if no move is available.
   */
  public static Move hint(final Player player, final GameState state, final boolean useNN) {
    return hint(player, state, 2000, useNN);
  }

  /**
   * @param player @param state @param reflectionTimeMs @param useNN
   */
  public static Move hint(
      final Player player,
      final GameState state,
      final long reflectionTimeMs,
      final boolean useNN) {
    final int playerIdx = state.getPlayers().indexOf(player);
    NeuralNetworkEvaluator evalToUse = null;
    if (useNN) {
      evalToUse = new NeuralNetworkEvaluator();
    }
    return new MctsNode(state.copy())
        .getBestMoveAfterSearch(reflectionTimeMs, playerIdx, evalToUse);
  }

  /** Selects the best child node using NN evaluation or UCT formula. */
  private static MctsNode bestUCT(
      final MctsNode node, final int myIdx, final NeuralNetworkEvaluator evaluator) {
    return evaluator != null
        ? selectBestChildUsingNN(node, evaluator)
        : selectBestChildUsingUCT(node, myIdx);
  }

  /** Selects the best child using neural network evaluation. */
  private static MctsNode selectBestChildUsingNN(
      final MctsNode node, final NeuralNetworkEvaluator evaluator) {
    MctsNode bestChild = null;
    double bestValue = Double.NEGATIVE_INFINITY;

    for (final MctsNode child : node.getChildren()) {
      // The evaluator returns the score for the player whose turn it is in child.state.
      // Since child.state is the opponent's turn relative to 'node', the selection
      // player's reward is the negative of the evaluator's output.
      final double value = -evaluator.evaluate(child.state);
      if (value > bestValue) {
        bestValue = value;
        bestChild = child;
      }
    }
    return bestChild;
  }

  /** Selects the best child using UCT formula. */
  private static MctsNode selectBestChildUsingUCT(final MctsNode node, final int myIdx) {
    MctsNode bestChild = null;
    double maxUct = Double.NEGATIVE_INFINITY;
    final int currentPlayerIdx = node.getCurrentIndexPlayer();
    final boolean isAiTurn = currentPlayerIdx == myIdx;

    for (final MctsNode child : node.getChildren()) {
      double winRate = (double) child.wins / child.visits;
      if (!isAiTurn) {
        winRate = 1.0 - winRate;
      }
      final double uctValue =
          winRate + UCT_CONSTANT * Math.sqrt(Math.log(node.visits) / child.visits);
      if (uctValue > maxUct) {
        maxUct = uctValue;
        bestChild = child;
      }
    }
    return bestChild;
  }

  /** Backpropagates the simulation result up to the root. */
  private static void backpropagate(final MctsNode node, final int winnerIdx, final int myIdx) {
    MctsNode current = node;
    while (current != null) {
      current.incrementVisits();
      if (winnerIdx == myIdx) {
        current.incrementWins();
      }
      current = current.getParent();
    }
  }

  @Override
  public String toString() {
    return "AI " + super.toString();
  }

  @Override
  public Map<String, Object> getSettings() {
    final Map<String, Object> settings = new ConcurrentHashMap<>();
    settings.put("mode", "mcts");
    return settings;
  }
}
