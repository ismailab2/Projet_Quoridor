package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;
import fr.bordeaux.util.Logger;

/** CLI command that cancels previously played moves. */
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
  public UndoCommand(final String[] t) {
    tokens = t;
  }

  /**
   * Executes undo command. Cancels the specified number of moves.
   *
   * @param engine the current game engine
   * @return the updated game engine after undoing moves
   */
  @Override
  public GameEngine execute(final GameEngine engine) {
    if (tokens.length >= 3) {
      LOGGER.info("Usage: undo [N]");
      return engine;
    }
    final int count = (tokens.length > 1) ? Integer.parseInt(tokens[1]) : 1;
    LOGGER.info("Undoing {0} move(s)...", count);

    for (int i = 0; i < count; i++) {
      engine.getState().undo();
    }
    return engine;
  }

  /** Displays help information for the undo command. */
  @Override
  public void helpText() {
    LOGGER.info("=== UNDO Command ===\n");
    LOGGER.info("Usage:");
    LOGGER.info("  undo");
    LOGGER.info("  undo [N]\n");
    LOGGER.info("Description:");
    LOGGER.info("  Cancels previously played moves.\n");
    LOGGER.verbose("  - If no number is specified, one move is undone.");
    LOGGER.verbose("  - If N is specified, N moves are undone.\n");
    LOGGER.verbose("Examples:");
    LOGGER.verbose("  undo      -> undo last move");
    LOGGER.verbose("  undo 2    -> undo last two moves\n");
  }
}
