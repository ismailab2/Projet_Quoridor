package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;
import fr.bordeaux.i18n.I18n;
import fr.bordeaux.util.Logger;

/**
 * Command that cancels previously played moves. The user can undo a single move or multiple moves.
 */
public class UndoCommand implements Command {
  /** Command tokens. */
  private String[] tokens;

  /** Logger to trace undo actions. */
  private static final Logger LOGGER = Logger.getInstance();

  /** Creates an UndoCommand that undoes one move by default. */
  public UndoCommand() {
    this(new String[] {"undo", "1"});
  }

  /**
   * Creates an UndoCommand using the provided CLI tokens.
   *
   * @param t the tokens entered by the user
   */
  public UndoCommand(String[] t) {
    tokens = t;
  }

  /**
   * Executes the undo command. Cancels the specified number of previously played moves.
   *
   * @param engine the current game engine
   * @return the updated game engine after undoing moves
   */
  @Override
  public GameEngine execute(GameEngine engine) {
    if (tokens.length >= 3) {
      LOGGER.info("Usage: undo [N]");
      return engine;
    }
    int count = (tokens.length > 1) ? Integer.parseInt(tokens[1]) : 1;
    LOGGER.info(I18n.get("undoing") + count + I18n.get("redoMove"));

    for (int i = 0; i < count; i++) {
      engine.getState().undo();
    }
    return engine;
  }

  /** Displays help information about the undo command, including its usage and examples. */
  @Override
  public void helpText() {
    LOGGER.info("=== UNDO Command ===\n");
    LOGGER.info("Usage:");
    LOGGER.info("  undo");
    LOGGER.info("  undo [N]\n");
    LOGGER.info("Description:");
    LOGGER.info("  " + I18n.get("undoDesc"));
    LOGGER.verbose("  " + I18n.get("undoOne"));
    LOGGER.verbose("  " + I18n.get("undoNb"));
    LOGGER.verbose(I18n.get("loadEx"));
    LOGGER.verbose("  undo      -> "+ I18n.get("undoExOne"));
    LOGGER.verbose("  undo 2    -> "+ I18n.get("undoExNb"));
  }
}
