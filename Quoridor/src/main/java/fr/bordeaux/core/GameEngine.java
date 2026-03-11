package fr.bordeaux.core;

import fr.bordeaux.persistence.GameSerializer;
import java.io.IOException;

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
    try {
      state.saveGame(filename);
    } catch (IOException e) {
      System.err.println("Erreur sauvegarde : " + e.getMessage());
    }
  }

  public void loadGame(String filename) {
    try {
      state = GameSerializer.loadGame(filename);
    } catch (Exception e) {
      System.err.println("Erreur chargement : " + e.getMessage());
    }
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
