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
public class MCTSPlayer extends AiPlayer {

  /** Reflection time allowed for the AI in milliseconds. */
  private final long reflectionTime;

  /** Simulation counter for debugging and performance tracking. */
  private static int simNum;

  /** Constant direction offsets for orthogonal movement. */
  private static final int[][] DIRECTIONS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

  /** Maximum number of turns allowed in a single simulation playout. */
  private static final int MAX_TURNS = 200;

  /**
   * Constructor with custom reflection time.
   *
   * @param name Name of the player.
   * @param positionStart Starting position on the board.
   * @param reflectionTime Maximum time in milliseconds allowed for MCTS search.
   */
  public MCTSPlayer(final String name, final Position positionStart, final long reflectionTime) {
    super(name, positionStart);
    this.reflectionTime = reflectionTime;
  }

  /**
   * Default constructor with 2000ms reflection time.
   *
   * @param name Name of the player.
   * @param positionStart Starting position on the board.
   */
  public MCTSPlayer(final String name, final Position positionStart) {
    this(name, positionStart, 2000);
  }

  @Override
  public Player copy() {
    return new MCTSPlayer(
        this.getName(),
        this.getPosition(),
        this.getStartingPosition(),
        this.getRemainingWalls(),
        this.getScore(),
        this.reflectionTime);
  }

  /**
   * Private constructor used for cloning or internal state management.
   *
   * @param name Name of the player.
   * @param current Current position of the player.
   * @param start Initial starting position.
   * @param walls Number of walls remaining.
   * @param score Current score or evaluation.
   * @param reflectionTime Maximum reflection time in milliseconds.
   */
  private MCTSPlayer(
      final String name,
      final Position current,
      final Position start,
      final int walls,
      final int score,
      final long reflectionTime) {
    super(name, current, start, walls, score);
    this.reflectionTime = reflectionTime;
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

    /**
     * Constructor for the root node of the MCTS tree.
     *
     * @param state The initial game state for this node.
     */
    public MctsNode(final GameState state) {
      this(state, null, null);
    }

    /**
     * Constructor for child nodes.
     *
     * @param state The game state resulting from the move.
     * @param move The move that led to this state.
     * @param parent The parent node in the tree.
     */
    public MctsNode(final GameState state, final Move move, final MctsNode parent) {
      this.state = state;
      this.move = move;
      this.parent = parent;
      this.untriedMoves = new ArrayList<>(state.generateLegalMoves());
      Collections.shuffle(this.untriedMoves);
    }

    /**
     * Performs MCTS iterations until the reflection time limit is reached.
     *
     * @param limit The time limit in milliseconds for the search.
     * @param myIdx The index of the current AI player.
     * @param aiPlayer The AI player instance performing the search.
     * @return The best move found after the simulation iterations.
     */
    public Move getBestMoveAfterSearch(
        final long limit, final int myIdx, final MCTSPlayer aiPlayer) {
      final long start = System.currentTimeMillis();
      while (System.currentTimeMillis() - start < limit) {
        MctsNode current = this;
        while (!current.state.isGameOver() && current.untriedMoves.isEmpty()) {
          current = aiPlayer.bestUCT(current, myIdx);
        }
        if (!current.state.isGameOver()) {
          current = current.expand();
        }
        aiPlayer.backpropagate(current, current.simulate(), myIdx);
      }
      return this.extractBestMove();
    }

    /**
     * Extracts the best move from the current node's children based on visit count.
     *
     * @return The move associated with the most visited child node, or null if no children exist.
     */
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

    /**
     * Expands the tree by creating a new child node from an untried move.
     *
     * @return The newly created child node.
     */
    public MctsNode expand() {
      final Move nextMove = untriedMoves.removeLast();
      final GameState nextState = state.copy();
      Player currentPlayer = nextState.getCurrentPlayer();
      nextState.applyMove(nextMove);
      if (nextState.hasReachedGoal(currentPlayer)) {
        nextState.setGameOver(true);
      }
      final MctsNode newNode = new MctsNode(nextState, nextMove, this);
      children.add(newNode);
      return newNode;
    }

    /**
     * Simulates a random game from the current state until a conclusion or turn limit.
     *
     * @return The index of the winning player, or -1 if no winner was determined.
     */
    public int simulate() {
      final GameState temp = state.copy();
      simNum++;
      if (temp.isGameOver()) {
        return temp.getPreviousIndexPlayer();
      }
      final int totalPlayers = temp.getPlayers().size();
      final List<Player> allPlayers = temp.getPlayers();
      int winnerResult = -1;
      int turns = 0;

      while (winnerResult == -1 && turns < MAX_TURNS) {
        final Player currentPlayer = temp.getCurrentPlayer();
        final Position currentPos = currentPlayer.getPosition();
        final int curX = currentPos.getX();
        final int curY = currentPos.getY();

        final List<Move> legalMoves = new ArrayList<>(8);

        // Check only relevant jumps and steps based on DIRECTIONS
        for (final int[] dir : DIRECTIONS) {
          final Position step = new Position(curX + dir[0], curY + dir[1]);
          final Move stepMove = Move.pawn(currentPos, step);
          if (RuleChecker.isMoveLegal(temp, stepMove)) {
            legalMoves.add(stepMove);
          }

          final Position jump = new Position(curX + (dir[0] * 2), curY + (dir[1] * 2));
          final Move jumpMove = Move.pawn(currentPos, jump);
          if (RuleChecker.isMoveLegal(temp, jumpMove)) {
            legalMoves.add(jumpMove);
          }
        }

        if (legalMoves.isEmpty()) {
          break;
        }

        temp.applyMoveWithoutClearingRedo(
            legalMoves.get((int) (Math.random() * legalMoves.size())));

        // Victory check
        final int lastIdx = (temp.getCurrentIndexPlayer() - 1 + totalPlayers) % totalPlayers;
        if (temp.hasReachedGoal(allPlayers.get(lastIdx))) {
          winnerResult = lastIdx;
        }
        turns++;
      }
      return winnerResult;
    }

    /**
     * Returns the player index for the current state.
     *
     * @return The index of the player whose turn it is in this node.
     */
    public int getPlayerIndex() {
      return state.getCurrentIndexPlayer();
    }

    /**
     * Returns the list of child nodes.
     *
     * @return A list containing all expanded child nodes.
     */
    public List<MctsNode> getChildren() {
      return children;
    }

    /**
     * Returns the parent node.
     *
     * @return The parent MctsNode, or null if this is the root.
     */
    public MctsNode getParent() {
      return parent;
    }

    /**
     * Returns the total number of times this node has been visited.
     *
     * @return The visit count.
     */
    public int getVisits() {
      return visits;
    }

    /**
     * Returns the total number of wins tracked through this node.
     *
     * @return The win count.
     */
    public int getWins() {
      return wins;
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
    simNum = 0;
    final int myIdx = state.getCurrentIndexPlayer();
    return new MctsNode(state.copy()).getBestMoveAfterSearch(this.reflectionTime, myIdx, this);
  }

  /**
   * Selects the best child node using the Upper Confidence Bound for Trees (UCT) formula.
   *
   * @param node The parent node from which to select the best child.
   * @param myIdx The index of the AI player to determine if it's an advantage or disadvantage.
   * @return The child node with the highest UCT value.
   */
  private MctsNode bestUCT(MctsNode node, int myIdx) {
    MctsNode bestChild = null;
    double maxUct = Double.NEGATIVE_INFINITY;
    boolean isAiTurn = node.state.getCurrentIndexPlayer() == myIdx;

    for (MctsNode child : node.children) {
      double winRate = (double) child.wins / child.visits;
      if (!isAiTurn) {
        winRate = 1.0 - winRate;
      }

      double uctValue = winRate + 1.41 * Math.sqrt(Math.log(node.visits) / child.visits);

      if (uctValue > maxUct) {
        maxUct = uctValue;
        bestChild = child;
      }
    }
    return bestChild;
  }

  /**
   * Backpropagates the simulation result from a leaf node up to the root.
   *
   * @param node The node where the simulation started.
   * @param winnerIdx The index of the player who won the simulation.
   * @param myIdx The index of the AI player to update win counts accordingly.
   */
  protected void backpropagate(final MctsNode node, final int winnerIdx, final int myIdx) {
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
