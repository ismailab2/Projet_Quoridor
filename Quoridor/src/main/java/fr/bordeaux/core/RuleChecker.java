package fr.bordeaux.core;

import java.util.ArrayList;
import java.util.List;

/**
 * RuleChecker checks if moves (pawn moves or wall placements) are valid according to Quoridor
 * rules.
 */
public class RuleChecker {

  public static boolean isMoveLegal(GameState state, Move move) {
    if (move.isPawn()) {
      return pawnMoveLegal(state, move);
    } else {
      return wallMoveLegal(state, move);
    }
  }

  public static boolean canJump(GameState state, Move move) {
    // Jump gestion
    Player CurrentPlay = state.getCurrentPlayer();
    Board b = state.getBoard();
    Position from = CurrentPlay.getPosition();
    Position to = move.getTo();

    int fromId = b.id(from.getX(), from.getY());
    int toId = b.id(to.getX(), to.getY());

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
      // System.out.println(jump.getX() + " " + jump.getY());

      if (b.isInside(jump)) {
        int jumpId = b.id(jump.getX(), jump.getY());

        boolean canJumpStraight =
            b.getGraph().hasEdge(fromId, opId) && b.getGraph().hasEdge(opId, jumpId);

        if (canJumpStraight && to.equals(jump)) {
          System.out.println("jump straight");
          return true;
        }
      }

      for (Position diag : getDiagonalJumps(from, posOtherPlayer)) {
        if (!b.isInside(diag)) continue;

        int diagId = b.id(diag.getX(), diag.getY());

        boolean canSideStep =
            b.getGraph().hasEdge(fromId, opId) && b.getGraph().hasEdge(opId, diagId);

        if (canSideStep && to.equals(diag)) return true;
      }
    }
    return false;
  }

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

    if (b.getGraph().hasEdge(fromId, toId)) return true;

    return RuleChecker.canJump(state, move);
  }

  private static boolean isAdjacent(Position a, Position b) {
    int dx = Math.abs(a.getX() - b.getX());
    int dy = Math.abs(a.getY() - b.getY());
    // System.out.println("c'est adjacent "+ (dx+dy == 1));
    return dx + dy == 1;
  }

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

  // Check if a path still exists after applying a move, move must be a wall move.
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
