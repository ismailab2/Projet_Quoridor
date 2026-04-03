package fr.bordeaux.gui;

/** Enumerates the actions available in the graphical user interface. */
public enum Action {
  /** Starts a new game. */
  NEW_GAME,

  /** Loads a saved game. */
  LOAD_GAME,

  /** Saves the current game. */
  SAVE_GAME,

  /** Opens the configuration dialog. */
  CONFIGURATION,

  /** Opens the information dialog. */
  INFO,

  /** Quits the application. */
  QUIT,

  /** Undoes the last action. */
  UNDO,

  /** Redoes the last undone action. */
  REDO,

  /** Pauses the current game. */
  PAUSE,

  /** Displays a hint for the current player. */
  HINT
}
