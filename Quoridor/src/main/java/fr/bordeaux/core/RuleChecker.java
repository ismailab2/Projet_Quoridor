package fr.bordeaux.core;

/**
 * RuleChecker checks if moves (pawn moves or wall placements) are valid according to Quoridor
 * rules.
 */
public class RuleChecker {

  private RuleChecker() {}

  public static boolean isMoveLegal(GameState state, Move move) {
    if (move.isPawn()) {
      return pawnMoveLegal(state, move);
    } else {
      return wallMoveLegal(state, move);
    }
  }


  private static boolean pawnMoveLegal(GameState state, Move move) {
    Player p = state.currentPlayer();
    Board b = state.getBoard();
    Position to = move.getTo();

    //Check if position is inside the board
    if (!b.isInside(to)) return false;

    //Check if there is another player at the same place
    for (Player other : state.getPlayers()) {
      if (other.getPosition().equals(to)) return false;
      //TODO : On doit modifier ici pour les sauts par dessus un autre joueur
    }

    //Check is there is an edge between the two vertices.
    int currentId = b.id(p.getPosition().row(), p.getPosition().col());
    int targetId = b.id(to.row(), to.col());

    return b.getGraph().hasEdge(currentId, targetId);
  }

  private static boolean wallMoveLegal(GameState state, Move move) {
    Player p = state.currentPlayer();
    Board b = state.getBoard();

    //Has the player enough walls
    if (p.getRemainingWalls() <= 0) return false;

    //Is there a collision between two walls
    if (b.isWallCollision(move.getTo(), move.getOrientation())) return false;

    //Do all players still have a path to victory
    return doesPathExist(state, move);
  }

  //Check if a path still exists after applying a move, move must be a wall move.
  private static boolean doesPathExist(GameState state, Move move) {
    Board b = state.getBoard();
    b.addWall(move.getTo(), move.getOrientation());

    boolean canWin = true;
    for  (Player p : state.getPlayers()) {
      int startId =  b.id(p.getPosition().row(), p.getPosition().col());
      if (b.findPath(startId,state.getWinCondition(p)).isEmpty()) {
        canWin = false;
        break;
      }
    }
    b.removeWall(move.getTo(), move.getOrientation());
    return canWin;
  }
}
