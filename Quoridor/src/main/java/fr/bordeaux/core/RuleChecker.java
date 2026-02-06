package fr.bordeaux.core;

/**
 * RuleChecker checks if moves (pawn moves or wall placements) are valid according to Quoridor
 * rules.
 */
public class RuleChecker {

  public static boolean isMoveLegal(GameState state, Move move) {
    return move.getType() == Move.Type.PAWN
        ? pawnMoveLegal(state, move.getTo())
        : wallMoveLegal(state, move.getWall());
  }

  private static boolean pawnMoveLegal(GameState state, Position to) {
    Player p = state.currentPlayer();
    Board b = state.getBoard();

    if (!b.isInside(to)) return false;
    if (b.getCell(to).getPlayer() != null) return false;

    Position from = p.getPosition();
    int dx = Math.abs(to.getX() - from.getX());
    int dy = Math.abs(to.getY() - from.getY());

    return dx + dy == 1;
  }

  private static boolean wallMoveLegal(GameState state, Wall wall) {
    Player p = state.currentPlayer();
    if (p.getRemainingWalls() <= 0) return false;

    for (Wall w : state.getBoard().getWalls()) {
      if (w.conflictsWith(wall) || w.crosses(wall)) return false;
    }
    return true;
  }
}
