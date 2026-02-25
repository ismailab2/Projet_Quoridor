package fr.bordeaux.AI;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Move;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;

import java.util.ArrayList;
import java.util.List;

/** Represents an Ai player with MCTS */
public class MCTSPlayer extends Player {

  @JsonCreator
  public MCTSPlayer(
      @JsonProperty("name") String name, @JsonProperty("positionStart") Position positionStart) {
    super(name, positionStart);
  }

  @Override
  public Player copy() {
    return new MCTSPlayer(
            this.getName(),
            this.getPosition(),
            this.getStartingPosition(),
            this.getRemainingWalls(),
            this.getScore()
    );
  }

  private MCTSPlayer(String name, Position current, Position start, int walls, int score) {
    super(name, current, start, walls, score);
  }

  public static class Node {
    GameState state;
    Move move; //Move that led to this node
    Node parent;
    List<Node> children = new ArrayList<>();
    int wins = 0;
    int visits = 0;

    public Node(GameState state) {
      this(state, null, null);
    }

    public Node(GameState state,  Move move, Node parent) {
      this.state = state;
      this.move = move;
      this.parent = parent;
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
    // Create the root of the search tree using a copy of the current game state
    Node root = new Node(state.copy());

    long startTime = System.currentTimeMillis();

    // Thinking loop: run as many simulations as possible within the time limit (2 seconds)
    while (System.currentTimeMillis() - startTime < 2000) {

      // 1. SELECTION & EXPANSION: Find the most promising node or create a new one
      Node selected = select(root);

      // 2. SIMULATION: Play a random game until the end to see who wins
      int result = simulate(selected);

      // 3. BACKPROPAGATION: Update the statistics (wins/visits) from the leaf to the root
      backpropagate(selected, result);
    }

    // After the time is up, choose the move that was explored the most (highest visit count)
    return root.getBestMove();
  }

  private Node select(Node node) {
    Node current = node;

    while(!current.state.isGameOver() && isFullyExpanded(current)) {
      current = bestUCT(current);
    }

    //If game isn't over, and we have an incomplete node, we expand it
    if (!current.state.isGameOver()) {
      return expand(current);
    }
    return current;
  }

  private Node bestUCT(Node node) {
    double bestScore = Double.NEGATIVE_INFINITY;
    Node bestChild = null;
    double C = 1.41; //Square root of 2

    for (Node child : node.children) {
      if (child.visits == 0) return child; //Priority to never visited nodes

      //Formula for determining the score of a child using Upper Confidence Bound
      double exploitation = (double) child.wins / child.visits;
      double exploration = C * Math.sqrt(Math.log(node.visits) / child.visits);
      double score = exploitation + exploration;

      if (score > bestScore) {
        bestScore = score;
        bestChild = child;
      }
    }
    return bestChild;
  }

  //Check if there are still unexplored moves from node, false if there are.
  private boolean isFullyExpanded(Node node) {
    List<Move> legalMoves = generate_legal_moves();
    return node.children.size() == legalMoves.size();
  }

  //Temporary method to compile
  private List<Move> generate_legal_moves() {
    return new ArrayList<>();
  }

  private Node expand(Node node) {
    List<Move> legalMoves = generate_legal_moves();

    for (Move move : legalMoves) {
      boolean alreadyExpanded = false;
      for (Node child : node.children) {
        if (child.move.equals(move)) {
          alreadyExpanded = true;
          break;
        }
      }
      if (!alreadyExpanded) {
        GameState nextState = node.state.copy();
        nextState.applyMove(move);
        Node newNode = new Node(nextState, move, node);
        node.children.add(newNode);
        return newNode;
      }
    }
    return node;
  }

  private int simulate(Node node) {
    return 0;
  }

  public void backpropagate(Node node, int result) {
    return;
  }

  @Override
  public String toString() {
    return "AI " + super.toString();
  }
}
