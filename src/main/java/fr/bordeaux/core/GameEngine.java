package fr.bordeaux.core;

import fr.bordeaux.ai.MctsPlayer;
import java.io.IOException;

/**
 * The core engine of the Quoridor game. This class coordinates the game logic, manages player
 * turns, and handles actions such as saving, loading, and providing hints using AI.
 */
public class GameEngine {

  private GameState state;
  private BlitzManager blitzManager;

  /**
   * Constructs a GameEngine with the specified initial game state.
   *
   * @param state The initial {@link GameState} to be managed by the engine.
   */
  public GameEngine(GameState state) {
    this.state = state;
    initializeBlitz();
  }

  /** Helper method to initialize the BlitzManager based on the current state. */
  private void initializeBlitz() {
    if (this.state.isBlitz()) {
      this.blitzManager = new BlitzManager(this);
      this.blitzManager.start();
    }
  }

  /**
   * Attempts to execute a move in the current game state. Validates the move according to game
   * rules and checks if the game is paused or over.
   *
   * @param move The {@link Move} to be performed.
   * @return {@code true} if the move was successfully applied, {@code false} otherwise.
   */
  public boolean play(Move move) {
    if (state.isGameOver() || state.isPaused()) {
      System.out.println("GAME OVER OR  PAUSE");
      return false;
    }

    if (!RuleChecker.isMoveLegal(state, move)) return false;

    Player p = state.getCurrentPlayer();
    state.applyMove(move);

    if (state.hasReachedGoal(p)) {
      state.setGameOver(true);
      if (blitzManager != null) {
        blitzManager.pause();
      }
      System.out.println(p.getName() + " won the game!");
    }
    return true;
  }

  /** Resets the current game to its initial state and clears listeners. */
  public void newGame() {
    if (blitzManager != null) {
      blitzManager.pause();
    }
    state.reset();
  }

  /**
   * Saves the current game state to a file.
   *
   * @param filename The path to the file where the game will be saved.
   */
  public void saveGame(String filename) {
    try {
      state.saveGame(filename);
    } catch (IOException e) {
      System.err.println("Erreur sauvegarde : " + e.getMessage());
    }
  }

  /**
   * Loads a game state from a specified file.
   *
   * @param filename The path to the file to load.
   */
  public void loadGame(String filename) {
    try {
      if (blitzManager != null) {
        blitzManager.pause();
      }
      state.loadGame(filename);
      initializeBlitz();
    } catch (Exception e) {
      System.err.println("Erreur chargement : " + e.getMessage());
    }
  }

  /** Reverts the last move made in the game. */
  public void undo() {
    state.undo();
  }

  /** Re-applies the last move that was undone. */
  public void redo() {
    state.redo();
  }

  /** Toggles the pause state of the game and synchronization with the Blitz timer. */
  public void pause() {
    boolean newPauseState = !state.isPaused();
    state.setPaused(newPauseState);

    if (blitzManager != null) {
      if (newPauseState) {
        blitzManager.pause();
        System.out.println("Timer Paused.");
      } else {
        blitzManager.start();
        System.out.println("Timer Resumed.");
      }
    }
  }

  /**
   * Provides a hint for the current player by calculating the best move using an AI.
   *
   * @return The best move for the current player.
   * @throws IOException If an I/O error occurs.
   */
  public Move hint() throws IOException {

    Player current = state.getCurrentPlayer();

    final Move hint = MctsPlayer.hint(getState().getCurrentPlayer(), getState(), 3000, true);

    return hint;
  }

  /**
   * Converts a Move object into a human-readable string (e.g., "e2-e3" or "e2h").
   *
   * @param move The move to convert.
   * @return A formatted string representation of the move.
   */
  private String moveToString(Move move) {

    if (move.getType() == Move.Type.PAWN) {

      return posToString(move.getFrom()) + "-" + posToString(move.getTo());
    }

    char orientation = move.getOrientation() == Orientation.HORIZONTAL ? 'h' : 'v';

    return posToString(move.getTo()) + orientation;
  }

  /**
   * Converts a Position object into algebraic notation (e.g., "a0", "b1").
   *
   * @param p The position to convert.
   * @return A string representing the coordinates.
   */
  private String posToString(Position p) {
    char letter = (char) ('a' + p.getX());
    int number = p.getY();
    return "" + letter + number;
  }

  /**
   * Returns the current state of the game.
   *
   * @return The {@link GameState} managed by this engine.
   */
  public GameState getState() {
    return state;
  }

  /**
   * Returns the current blitz manager of the game.
   *
   * @return The {@link BlitzManager} managed by this engine.
   */
  public BlitzManager getBlitzManager() {
    return blitzManager;
  }
}
