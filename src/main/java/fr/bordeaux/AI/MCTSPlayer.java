package fr.bordeaux.AI;

import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Move;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;
import java.util.*;

/** Represents an Ai player with MCTS */
public class MCTSPlayer extends AiPlayer {

  private final long reflectionTime; // Time in milliseconds

  public MCTSPlayer(String name, Position positionStart, long reflectionTime) {
    super(name, positionStart);
    this.reflectionTime = reflectionTime;
  }

  // Default constructor with 2 seconds if not specified
  public MCTSPlayer(String name, Position positionStart) {
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
      String name, Position current, Position start, int walls, int score, long reflectionTime) {
    super(name, current, start, walls, score);
    this.reflectionTime = reflectionTime;
  }

  public static class Node {
    GameState state; // Used for determining legal moves
    Move move; // Move that led to this node, potential move to return
    Node parent; // Used in backpropagation to update wins and visits
    List<Node> children = new ArrayList<>(); // children of each node
    List<Move> untriedMoves; // list of available moves from this node, used for optimization
    int wins = 0;
    int visits = 0;

    public Node(GameState state) {
      this(state, null, null);
    }

    public Node(GameState state, Move move, Node parent) {
      this.state = state;
      this.move = move;
      this.parent = parent;
      this.untriedMoves = new ArrayList<>(state.generateLegalMoves());
      Collections.shuffle(this.untriedMoves);
    }

    public Move getBestMove() {
      if (children.isEmpty()) {
        return null;
      }

      Node bestNode = children.getFirst();
      for (Node n : children) {
        if (n.visits > bestNode.visits) {
          bestNode = n;
        }
      }
      return bestNode.move;
    }
  }

  @Override
  public Move getNextMove(GameState state) {

    // Get this Ai player index in list of player of gameState
    int myIndex = state.getPlayers().indexOf(this);

    // Check if there is an obvious winning move
    List<Move> legalMoves = state.generateLegalMoves();
    for (Move m : legalMoves) {
      GameState check = state.copy();
      check.applyMove(m);
      Player meInCheck = check.getPlayers().get(myIndex);

      if (check.hasReachedGoal(meInCheck)) {
        return m;
      }
    }

    // Create the root of the search tree using a copy of the current game state
    Node root = new Node(state.copy());

    long startTime = System.currentTimeMillis();

    // Thinking loop: run as many simulations as possible within the time limit (2 seconds)
    while (System.currentTimeMillis() - startTime < this.reflectionTime) {

      // 1. selection & expansion: Find the most promising node or create a new one
      Node selected = select(root, myIndex);

      // 2. simulation: Play a random game until the end to see who wins
      // result is the index of the winner in the players list
      int winnerIndex = simulate(selected);

      // 3. backpropagation: Update the statistics (wins/visits) from the leaf to the root
      backpropagate(selected, winnerIndex, myIndex);
    }

    // After the time is up, choose the move that was explored the most (highest visit count)
    return root.getBestMove();
  }

  private Node select(Node node, int myIndex) {
    Node current = node;

    while (!current.state.isGameOver() && isFullyExpanded(current)) {
      current = bestUCT(current, myIndex);
    }

    // If game isn't over, and we have an incomplete node, we expand it
    if (!current.state.isGameOver()) {
      return expand(current);
    }
    return current;
  }

  private Node bestUCT(Node node, int myIndex) {
    double C = 1.41;
    Node bestChild = null;

    // Check if it's the opponent's turn
    boolean isMyTurn = node.state.getCurrentIndexPlayer() == myIndex;

    double bestScore = isMyTurn ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

    for (Node child : node.children) {
      if (child.visits == 0) return child;

      double exploitation = (double) child.wins / child.visits;
      double exploration = C * Math.sqrt(Math.log(node.visits) / child.visits); // UCB Formula

      double score = isMyTurn ? (exploitation + exploration) : (exploitation - exploration);

      if (isMyTurn) {
        if (score > bestScore) {
          bestScore = score;
          bestChild = child;
        }
      } else {
        if (score < bestScore) {
          bestScore = score;
          bestChild = child;
        }
      }
    }
    return bestChild;
  }

  private Node expand(Node node) {
    // Pop the last available move not already a child
    Move move = node.untriedMoves.removeLast();

    GameState nextState = node.state.copy();
    nextState.applyMove(move);
    Node newNode = new Node(nextState, move, node);
    node.children.add(newNode);
    return newNode;
  }

  // Check if there are still unexplored moves from node, false if there are.
  private boolean isFullyExpanded(Node node) {
    return node.untriedMoves.isEmpty();
  }

  private int simulate(Node node) {
    // Copy state to isolate simulation from the search tree
    GameState tempState = node.state.copy();

    while (!tempState.isGameOver()) {
      List<Move> legalMoves = tempState.generateLegalMoves();
      if (legalMoves.isEmpty()) break;

      // Select and apply a random legal move
      Move randomMove = legalMoves.get((int) (Math.random() * legalMoves.size()));
      tempState.applyMove(randomMove);

      // Check if the player who just moved reached their goal
      int playersCount = tempState.getPlayers().size();
      int lastPlayerIdx = (tempState.getCurrentIndexPlayer() - 1 + playersCount) % playersCount;

      if (tempState.hasReachedGoal(tempState.getPlayers().get(lastPlayerIdx))) {
        return lastPlayerIdx;
      }
    }

    // Identify and return the winner's index
    for (int i = 0; i < tempState.getPlayers().size(); i++) {
      if (tempState.hasReachedGoal(tempState.getPlayers().get(i))) {
        return i;
      }
    }
    return -1; // Return -1 for a draw or error
  }

  public void backpropagate(Node node, int winnerIndex, int myIndex) {
    Node current = node;

    while (current != null) {
      // Increment total visit count for this node
      current.visits++;

      // Update wins only if the simulation winner is this AI
      if (winnerIndex == myIndex) {
        current.wins++;
      }

      // Traverse up to the parent node
      current = current.parent;
    }
  }

  @Override
  public String toString() {
    return "AI " + super.toString();
  }

  @Override
  public Map<String, Object> getSettings() { // return all settings of MCTS
    Map<String, Object> settings = new HashMap<>();
    settings.put("mode", "mcts");
    return settings;
  }
}
