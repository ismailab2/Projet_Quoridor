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


public static void startGame() { System.out.println("Nouvelle partie lancée !"); }
public static void loadGame() { System.out.println("Charger une partie !"); }
public static void saveGame() { System.out.println("Partie sauvegardée !"); }
public static void undo() { System.out.println("Undo !"); }
public static void redo() { System.out.println("Redo !"); }
public static void pause() { System.out.println("Pause !"); }
public static void hint() { System.out.println("Hint !"); }

}
