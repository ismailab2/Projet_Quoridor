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

  public boolean play(Move move) {
    if (state.isGameOver()) {
      System.out.println("GAME OVER");
      return false;
    }

    if (!RuleChecker.isMoveLegal(state, move)) return false;

    Player p = state.currentPlayer();
    state.applyMove(move);
    if (state.hasReachedGoal(p)) {
      System.out.println(p.getName() + " won the game!");
    }
    return true;
  }

  public GameState getState() {
    return state;
  }
}
