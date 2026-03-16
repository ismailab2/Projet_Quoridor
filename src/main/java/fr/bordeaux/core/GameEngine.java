package fr.bordeaux.core;

import fr.bordeaux.ai.MinimaxPlayer;
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
      state = state.loadGame(filename);
    } catch (Exception e) {
      System.err.println("Erreur chargement : " + e.getMessage());
    }
  }

  public void undo() {
    state.undo();
  }

  public void redo() {
    state.redo();
  }

  public void pause() {
    if (state.isPaused()) {
      System.out.println("Resuming...");
      state.setPaused(false);
      state.setBlitz(false);
    } else {
      System.out.println("Pausing...");
      state.setPaused(true);
      state.setBlitz(true);
    }
  }

  public void hint() {

    Player current = state.getCurrentPlayer();

    MinimaxPlayer ia = MinimaxPlayer.builder(current.getName(), current.getPosition()).build();

    Move hint = ia.getNextMove(state);

    System.out.println("Best move : " + moveToString(hint));
  }

  private String moveToString(Move move) {

    if (move.getType() == Move.Type.PAWN) {

      return posToString(move.getFrom()) + "-" + posToString(move.getTo());
    }

    char orientation = move.getOrientation() == Orientation.HORIZONTAL ? 'h' : 'v';

    return posToString(move.getTo()) + orientation;
  }

  private String posToString(Position p) {
    char letter = (char) ('a' + p.getX());
    int number = p.getY();
    return "" + letter + number;
  }

  public GameState getState() {
    return state;
  }
}
