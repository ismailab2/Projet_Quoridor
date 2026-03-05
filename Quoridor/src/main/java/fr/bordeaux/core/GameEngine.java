package fr.bordeaux.core;

public class GameEngine {

  private GameState state;

  public GameEngine(GameState state) {
    this.state = state;
  }

  public boolean play(Move move) {
    if (state.isGameOver() || state.isPaused()) {
      System.out.println("GAME OVER OR  PAUSE");
      return false;
    }

    if (!RuleChecker.isMoveLegal(state, move)) return false;

    Player p = state.getCurrentPlayer();
    state.applyMove(move);
    if (state.hasReachedGoal(p)) {
      System.out.println(p.getName() + " won the game!");
    }
    return true;
  }

  public void newGame() {
    state.reset();
    state.addListener(() -> {});
  }

  public void saveGame(String filename) {
    System.out.println("jeu sauvegarde !");
  }

  public void loadGame(String filename) {
    System.out.println("jeu load !");
  }

  public void undo() {
    state.undo();
    // System.out.println("jeu undo !");
  }

  public void redo() {
    state.redo();
    // System.out.println("jeu redo !");
  }

  public void pause() {
    state.setPaused(true);
    System.out.println("EN PAUSE ");
  }

  public void hint() {
    //  TODO
  }

  public GameState getState() {
    return state;
  }
}
