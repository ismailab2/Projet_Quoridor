package fr.bordeaux.ai;

import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Move;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Represents an Ai player with MCTS. */
public class MCTSPlayer extends AiPlayer {

  /** Reflection time in milliseconds. */
  private final long reflectionTime;

  /** Constructor. */
  public MCTSPlayer(final String name, final Position positionStart, final long reflectionTime) {
    super(name, positionStart);
    this.reflectionTime = reflectionTime;
  }

  /** Default constructor. */
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

  /** Inner class representing a node in the MCTS tree. */
  public static class MctsNode {
    /** The state of the game. */
    private final GameState state;

    /** The move performed. */
    private final Move move;

    /** The parent node. */
    private final MctsNode parent;

    /** The children nodes. */
    private final List<MctsNode> children = new ArrayList<>();

    /** The moves not tried. */
    private final List<Move> untriedMoves;

    /** Total wins. */
    private int wins;

    /** Total visits. */
    private int visits;

    /** Constructor for root. */
    public MctsNode(final GameState state) {
      this(state, null, null);
    }

    /** Constructor for child. */
    public MctsNode(final GameState state, final Move move, final MctsNode parent) {
      this.state = state;
      this.move = move;
      this.parent = parent;
      this.untriedMoves = new ArrayList<>(state.generateLegalMoves());
      Collections.shuffle(this.untriedMoves);
    }

    /** Search for best move */
    public Move getBestMoveAfterSearch(
        final long reflectionTime, final int myIdx, final MCTSPlayer aiPlayer) {
      final long start = System.currentTimeMillis();
      while (System.currentTimeMillis() - start < reflectionTime) {
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

    private Move extractBestMove() {
      Move result = null;
      if (!children.isEmpty()) {
        MctsNode bestMctsNode = children.getFirst();
        for (final MctsNode n : children) {
          if (n.visits > bestMctsNode.visits) {
            bestMctsNode = n;
          }
        }
        result = bestMctsNode.move;
      }
      return result;
    }

    /** Expands the node by creating a new child. */
    public MctsNode expand() {
      final Move nextMove = untriedMoves.removeLast();
      final GameState nextState = state.copy();
      nextState.applyMove(nextMove);
      final MctsNode newMctsNode = new MctsNode(nextState, nextMove, this);
      children.add(newMctsNode);
      return newMctsNode;
    }

    /** Simulates a random game from this node. */
    public int simulate() {
      final GameState temp = state.copy();
      while (!temp.isGameOver()) {
        final List<Move> moves = temp.generateLegalMoves();
        if (moves.isEmpty()) {
          break;
        }
        temp.applyMove(moves.get((int) (Math.random() * moves.size())));
      }
      int winner = -1;
      final List<Player> players = temp.getPlayers();
      for (int i = 0; i < players.size(); i++) {
        if (temp.hasReachedGoal(players.get(i))) {
          winner = i;
          break;
        }
      }
      return winner;
    }

    /** Get index. */
    public int getPlayerIndex() {
      return state.getCurrentIndexPlayer();
    }

    /** Get children. */
    public List<MctsNode> getChildren() {
      return children;
    }

    /** Get parent. */
    public MctsNode getParent() {
      return parent;
    }

    /** Get visits. */
    public int getVisits() {
      return visits;
    }

    /** Get wins. */
    public int getWins() {
      return wins;
    }

    /** Add visit. */
    public void incrementVisits() {
      this.visits++;
    }

    /** Add win. */
    public void incrementWins() {
      this.wins++;
    }
  }

  /** Get next move to play for the AI. */
  @Override
  public Move getNextMove(final GameState state) {
    Move finalMove = null;
    final int myIdx = state.getPlayers().indexOf(this);
    final List<Move> legalMoves = state.generateLegalMoves();

    for (final Move m : legalMoves) {
      final GameState check = state.copy();
      check.applyMove(m);
      if (check.hasReachedGoal(check.getPlayers().get(myIdx))) {
        finalMove = m;
        break;
      }
    }

    if (finalMove == null) {
      finalMove =
          new MctsNode(state.copy()).getBestMoveAfterSearch(this.reflectionTime, myIdx, this);
    }
    return finalMove;
  }

  /** Calculate best score among children. */
  protected MctsNode bestUCT(final MctsNode mctsNode, final int myIdx) {
    final boolean isMyTurn = mctsNode.getPlayerIndex() == myIdx;
    double bestScore = isMyTurn ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
    MctsNode bestChild = null;

    for (final MctsNode child : mctsNode.getChildren()) {
      final double score = calculateScore(mctsNode.getVisits(), child, isMyTurn);
      if (isMyTurn) {
        if (score > bestScore) {
          bestScore = score;
          bestChild = child;
        }
      } else if (score < bestScore) {
        bestScore = score;
        bestChild = child;
      }
    }
    return bestChild;
  }

  private double calculateScore(final int pVisits, final MctsNode child, final boolean isMyTurn) {
    final double constantC = 1.41;
    final double score;
    if (child.getVisits() == 0) {
      score = isMyTurn ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
    } else {
      final double exploit = (double) child.getWins() / child.getVisits();
      final double explore = constantC * Math.sqrt(Math.log(pVisits) / child.getVisits());
      score = isMyTurn ? (exploit + explore) : (exploit - explore);
    }
    return score;
  }

  /** Backpropagates results. */
  protected void backpropagate(final MctsNode mctsNode, final int winnerIdx, final int myIdx) {
    MctsNode current = mctsNode;
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
