package fr.bordeaux.core;

/**
 * The GameEngine class manages the core logic of a Quoridor game. It handles players, game state,
 * rule checking, moves, and turn management.
 */
public class GameEngine {

  private final GameState state;

  public GameEngine(GameState state) {
    this.state = state;
  }

  public void play(Move move) {
    if (!RuleChecker.isMoveLegal(state, move)) throw new IllegalArgumentException("Coup illégal");

    Player p = state.currentPlayer();
    Board b = state.getBoard();

    if (move.getType() == Move.Type.PAWN) {
      b.getCell(p.getPosition()).setPlayer(null);
      p.setPosition(move.getTo());
      b.getCell(move.getTo()).setPlayer(p);
    } else {
      b.addWall(move.getWall());
      p.useWall();
    }

    state.nextPlayer();
  }
}
