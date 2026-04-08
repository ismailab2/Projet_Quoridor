package fr.bordeaux.core;

import fr.bordeaux.ai.mcts.MctsPlayer;
import fr.bordeaux.persistence.GameLoadException;
import fr.bordeaux.util.Logger;
import java.io.IOException;

/** Core engine for Quoridor game logic, player turns, and AI hints. */
public class GameEngine {

  /** The current state of the game. */
  private GameState state;

  /** The manager for blitz mode (timed games). */
  private BlitzManager blitzManager;

  /** Whether Blitz mode is enabled. */
  private boolean blitz;

  /** Whether verbose logging is enabled. */
  private boolean verbose;

  /** Whether contest mode is enabled. */
  private boolean contest;

  /** Whether debug mode is enabled. */
  private boolean debug;

  /** The initial number of walls for each player. */
  private int initialWalls;

  /**
   * Constructs GameEngine.
   *
   * @param state initial game state
   * @param blitz blitz mode flag
   * @param verbose verbose flag
   * @param contest contest mode flag
   * @param debug debug flag
   * @param initialWalls initial wall count
   */
  public GameEngine(
      final GameState state,
      final boolean blitz,
      final boolean verbose,
      final boolean contest,
      final boolean debug,
      final int initialWalls) {
    this.state = state;
    this.blitz = blitz;
    this.verbose = verbose;
    this.contest = contest;
    this.debug = debug;
    this.initialWalls = initialWalls;
    initializeBlitz();
  }

  /** Helper method to initialize the BlitzManager based on the current state. */
  private void initializeBlitz() {
    if (this.blitz) {
      this.blitzManager = new BlitzManager(this);
      this.blitzManager.start();
    }
  }

  /**
   * Executes a game move.
   *
   * @param move move to execute
   * @return true if success
   */
  public boolean play(final Move move) {
    final GameState currentState = this.state;
    if (currentState.isGameOver() || currentState.isPaused()) {
      Logger.getInstance().info("GAME OVER OR PAUSED");
      return false;
    }

    if (!RuleChecker.isMoveLegal(currentState, move)) {
      Logger.getInstance().info("Invalid move! Please try again.");
      return false;
    }

    final Player currentPlayer = currentState.getCurrentPlayer();
    currentState.applyMove(move);

    if (currentState.hasReachedGoal(currentPlayer)) {
      currentState.setGameOver(true);
      if (blitzManager != null) {
        blitzManager.pause();
      }
      if (Logger.getInstance().isInfoEnabled()) {
        Logger.getInstance().info("{0} won the game!", currentPlayer.getName());
      }
    }
    return true;
  }

  /**
   * Resets the current game to a specified initial state.
   *
   * @param boardSize the board size
   * @param players the list of players
   * @param blitzTimeoutMs the blitz timeout in milliseconds, or null if not blitz
   */
  public void newGame(
      final int boardSize, final java.util.List<Player> players, final Long blitzTimeoutMs) {
    if (blitzManager != null) {
      blitzManager.pause();
    }
    state.reset(boardSize, players, blitzTimeoutMs);
    initializeBlitz();
  }

  /**
   * Saves the current game state to a file.
   *
   * @param filename The path to the file where the game will be saved.
   */
  public void saveGame(final String filename) {
    try {
      fr.bordeaux.persistence.GameSaver.saveGame(this, filename);
    } catch (final IOException exception) {
      if (Logger.getInstance().isErrorEnabled()) {
        Logger.getInstance().error("Saving error : {0}", exception.getMessage());
      }
    }
  }

  /**
   * Loads a game state from a specified file.
   *
   * @param filename The path to the file to load.
   */
  public void loadGame(final String filename) {
    try {
      if (blitzManager != null) {
        blitzManager.pause();
      }
      final GameState loadedState = fr.bordeaux.persistence.GameLoader.loadGame(this, filename);
      state.loadState(loadedState);
      initializeBlitz();
    } catch (final IOException | GameLoadException exception) {
      if (Logger.getInstance().isErrorEnabled()) {
        Logger.getInstance().error("Erreur chargement : {0}", exception.getMessage());
      }
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
    if (state.getCurrentPlayer().isAI()) {
      Logger.getInstance().info("AI cannot pause the game.");
      return;
    }
    final boolean newPauseState = !state.isPaused();
    state.setPaused(newPauseState);

    if (blitzManager != null) {
      if (newPauseState) {
        blitzManager.pause();
        Logger.getInstance().info("Timer Paused.");
      } else {
        blitzManager.start();
        Logger.getInstance().info("Timer Resumed.");
      }
    }
  }

  /**
   * Suggests the best move using AI for the current player.
   *
   * @return The best move for the current player.
   * @throws IOException If an I/O error occurs.
   */
  public Move hint() throws IOException {
    final Player currentPlayer = state.getCurrentPlayer();
    final Move bestMove = MctsPlayer.hint(currentPlayer, state, 3000, true);
    if (bestMove != null) {
      moveToString(bestMove);
    }
    return bestMove;
  }

  /**
   * Converts a Move object into a human-readable string (e.g., "e2-e3" or "e2h").
   *
   * @param move The move to convert.
   * @return A formatted string representation of the move.
   */
  private String moveToString(final Move move) {
    final Move.Type moveType = move.getType();
    final String result;
    if (moveType == Move.Type.PAWN) {
      final Position from = move.getFrom();
      final Position targetPosition = move.getTo();
      result = posToString(from) + "-" + posToString(targetPosition);
    } else {
      final Orientation orientation = move.getOrientation();
      final char orientationChar = orientation == Orientation.HORIZONTAL ? 'h' : 'v';
      final Position targetPosition = move.getTo();
      result = posToString(targetPosition) + orientationChar;
    }
    return result;
  }

  /**
   * Converts a Position object into algebraic notation (e.g., "a0", "b1").
   *
   * @param pos The position to convert.
   * @return A string representing the coordinates.
   */
  private String posToString(final Position pos) {
    final int posX = pos.getX();
    final int posY = pos.getY();
    final char letter = (char) ('a' + posX);
    return String.valueOf(letter) + posY;
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

  /**
   * Returns whether blitz mode is enabled.
   *
   * @return {@code true} if blitz is enabled, {@code false} otherwise.
   */
  public boolean isBlitz() {
    return blitz;
  }

  /**
   * Sets whether blitz mode is enabled.
   *
   * @param blitz {@code true} to enable blitz, {@code false} to disable.
   */
  public void setBlitz(final boolean blitz) {
    this.blitz = blitz;
  }

  /**
   * Returns whether verbose mode is enabled.
   *
   * @return {@code true} if verbose is enabled, {@code false} otherwise.
   */
  public boolean isVerbose() {
    return verbose;
  }

  /**
   * Sets whether verbose mode is enabled.
   *
   * @param verbose {@code true} to enable verbose, {@code false} to disable.
   */
  public void setVerbose(final boolean verbose) {
    this.verbose = verbose;
  }

  /**
   * Returns whether contest mode is enabled.
   *
   * @return {@code true} if contest is enabled, {@code false} otherwise.
   */
  public boolean isContest() {
    return contest;
  }

  /**
   * Sets whether contest mode is enabled.
   *
   * @param contest {@code true} to enable contest, {@code false} to disable.
   */
  public void setContest(final boolean contest) {
    this.contest = contest;
  }

  /**
   * Returns whether debug mode is enabled.
   *
   * @return {@code true} if debug is enabled, {@code false} otherwise.
   */
  public boolean isDebug() {
    return debug;
  }

  /**
   * Sets whether debug mode is enabled.
   *
   * @param debug {@code true} to enable debug, {@code false} to disable.
   */
  public void setDebug(final boolean debug) {
    this.debug = debug;
  }

  /**
   * Returns the initial number of walls per player.
   *
   * @return The initial wall count.
   */
  public int getInitialWalls() {
    return initialWalls;
  }

  /**
   * Sets the initial number of walls per player.
   *
   * @param initialWalls The new initial wall count.
   */
  public void setInitialWalls(final int initialWalls) {
    this.initialWalls = initialWalls;
  }
}
