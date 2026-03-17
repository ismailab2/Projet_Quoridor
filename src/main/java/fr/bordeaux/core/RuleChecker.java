package fr.bordeaux.core;

import java.util.ArrayList;
import java.util.List;

/**
 * RuleChecker checks if moves (pawn moves or wall placements) are valid according to Quoridor
 * rules.
 */
public class RuleChecker {

  /**
   * Private constructor to prevent instantiation of this utility class. Throws an AssertionError if
   * called internally.
   */
  private RuleChecker() {
    throw new AssertionError("Utility class should not be instantiated");
  }

  /**
   * Checks if a given move is legal in the current game state.
   *
   * @param state The current {@link GameState}.
   * @param move The {@link Move} to validate.
   * @return {@code true} if the move is legal, {@code false} otherwise.
   */
  public static boolean isMoveLegal(GameState state, Move move) {
    if (move.isPawn()) {
      return pawnMoveLegal(state, move);
    } else {
      return wallMoveLegal(state, move);
    }
  }

  /**
   * Specifically checks if a jumping move is possible. A jump occurs when an opponent's pawn is
   * adjacent and the cell behind it is free, or diagonal cells are reachable if the back is
   * blocked.
   *
   * @param state The current {@link GameState}.
   * @param move The {@link Move} containing the target jump position.
   * @return {@code true} if the jump is valid, {@code false} otherwise.
   */
  public static boolean canJump(GameState state, Move move) {
    // Jump gestion
    Player CurrentPlay = state.getCurrentPlayer();
    Board b = state.getBoard();
    Position from = CurrentPlay.getPosition();
    Position to = move.getTo();

    int fromId = b.id(from.getX(), from.getY());

    for (Player otherPlayer : state.getPlayers()) {
      if (otherPlayer == CurrentPlay) {
        continue;
      }

      Position posOtherPlayer = otherPlayer.getPosition();

      if (!isAdjacent(from, posOtherPlayer)) {
        continue;
      }

      int dx = posOtherPlayer.getX() - from.getX();
      int dy = posOtherPlayer.getY() - from.getY();

      int opId = b.id(posOtherPlayer.getX(), posOtherPlayer.getY());
      // System.out.println(dx + " " + dy);

      Position jump = new Position(posOtherPlayer.getX() + dx, posOtherPlayer.getY() + dy);
      boolean jumpPossible = false;

      if (b.isInside(jump)) {
        int jumpId = b.id(jump.getX(), jump.getY());
        if (b.getGraph().hasEdge(opId, jumpId) && b.getGraph().hasEdge(fromId, opId)) {
          if (to.equals(jump)) return true;
          jumpPossible = true;
        }
      }
      if (!jumpPossible) {
        for (Position diag : getDiagonalJumps(from, posOtherPlayer)) {
          if (!b.isInside(diag)) continue;

          int diagId = b.id(diag.getX(), diag.getY());

          boolean canSideStep =
              b.getGraph().hasEdge(fromId, opId) && b.getGraph().hasEdge(opId, diagId);

          if (canSideStep && to.equals(diag)) return true;
        }
      }
    }
    return false;
  }

  /**
   * Validates a pawn movement. Checks for boundaries, collisions with other players, and
   * connectivity through the board graph.
   *
   * @param state The current game state.
   * @param move The pawn move to check.
   * @return {@code true} if the pawn can move to the target position.
   */
  private static boolean pawnMoveLegal(GameState state, Move move) {
    Player CurrentPlay = state.getCurrentPlayer();
    Board b = state.getBoard();
    Position from = CurrentPlay.getPosition();
    Position to = move.getTo();
    // System.out.println("c'est from"+from);
    // System.out.println(to.getX() + " " + to.getY());

    if (!b.isInside(to)) return false;

    int fromId = b.id(from.getX(), from.getY());
    int toId = b.id(to.getX(), to.getY());

    for (Player p : state.getPlayers()) {
      if (p.getPosition().equals(to)) return false;
    }

    if (b.getGraph().hasEdge(fromId, toId)) return true;

    return RuleChecker.canJump(state, move);
  }

  /**
   * Determines if two positions are horizontally or vertically adjacent.
   *
   * @param a The first position.
   * @param b The second position.
   * @return {@code true} if the positions are neighbors.
   */
  private static boolean isAdjacent(Position a, Position b) {
    int dx = Math.abs(a.getX() - b.getX());
    int dy = Math.abs(a.getY() - b.getY());
    // System.out.println("c'est adjacent "+ (dx+dy == 1));
    return dx + dy == 1;
  }

  /**
   * Computes potential diagonal jump positions when a straight jump is blocked.
   *
   * @param from The current player's position.
   * @param op The opponent's position.
   * @return A list of candidate {@link Position}s for a diagonal jump.
   */
  private static List<Position> getDiagonalJumps(Position from, Position op) {
    List<Position> diags = new ArrayList<>();

    int dx = op.getX() - from.getX();
    int dy = op.getY() - from.getY();

    if (dx == 1) {
      diags.add(new Position(op.getX(), op.getY() + 1));
      diags.add(new Position(op.getX(), op.getY() - 1));
    } else if (dx == -1) { // adversaire en haut
      diags.add(new Position(op.getX(), op.getY() + 1));
      diags.add(new Position(op.getX(), op.getY() - 1));
    } else if (dy == 1) { // adversaire à droite
      diags.add(new Position(op.getX() + 1, op.getY()));
      diags.add(new Position(op.getX() - 1, op.getY()));
    } else if (dy == -1) {
      diags.add(new Position(op.getX() + 1, op.getY()));
      diags.add(new Position(op.getX() - 1, op.getY()));
    }

    return diags;
  }

  /**
   * Validates a wall placement move. Checks for remaining walls, collisions with existing walls,
   * and path connectivity.
   *
   * @param state The current game state.
   * @param move The wall placement move.
   * @return {@code true} if the wall placement is legal.
   */
  private static boolean wallMoveLegal(GameState state, Move move) {
    Player p = state.getCurrentPlayer();
    Board b = state.getBoard();

    // Has the player enough walls
    if (p.getRemainingWalls() <= 0) return false;

    // Is there a collision between two walls
    if (b.isWallCollision(move.getTo(), move.getOrientation())) return false;

    // Do all players still have a path to victory
    return doesPathExist(state, move);
  }

  /**
   * Temporarily applies a wall move to verify if all players can still reach their respective goals
   * using a BFS algorithm.
   *
   * @param state The current game state.
   * @param move The wall move to test.
   * @return {@code true} if a path still exists for every player.
   */
  private static boolean doesPathExist(GameState state, Move move) {
    Board b = state.getBoard();
    b.addWall(move.getTo(), move.getOrientation());

    boolean canWin = true;
    for (Player p : state.getPlayers()) {
      int startId = b.id(p.getPosition().getX(), p.getPosition().getY());
      if (b.getGraph().findPathBFS(startId, state.getWinCondition(p)).isEmpty()) {
        canWin = false;
        break;
      }
    }
    b.removeWall(move.getTo(), move.getOrientation());
    return canWin;
  }
}
