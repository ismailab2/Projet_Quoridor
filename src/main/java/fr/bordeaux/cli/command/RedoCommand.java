package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;
import fr.bordeaux.util.Logger;

/** CLI command that replays previously undone moves. */
public class RedoCommand implements Command {
  /** Command tokens. */
  private String[] tokens;

  /** Logger to trace redo actions. */
  private static final Logger LOGGER = Logger.getInstance();

  /** Creates a RedoCommand that replays one move by default. */
  public RedoCommand() {
    this(new String[] {"redo", "1"});
  }

  /**
   * Creates a RedoCommand using the provided CLI tokens.
   *
   * @param tokens the tokens entered by the user
   */
  public RedoCommand(final String[] tokens) {
    this.tokens = tokens;
  }

  /**
   * Executes redo command. Replays the specified number of moves.
   *
   * @param engine the current game engine
   * @return the updated game engine after redoing moves
   */
  @Override
  public GameEngine execute(final GameEngine engine) {
    if (tokens.length >= 3) {
      LOGGER.info("Usage: redo [N]");
      return engine;
    }
    final int count = (tokens.length > 1) ? Integer.parseInt(tokens[1]) : 1;
    LOGGER.info("Redoing {0} move(s)...", count);

    for (int i = 0; i < count; i++) {
      engine.getState().redo();
    }
    return engine;
  }

  /** Displays help information for the redo command. */
  @Override
  public void helpText() {
    LOGGER.info("=== REDO Command ===\n");
    LOGGER.info("Usage:");
    LOGGER.info("  redo");
    LOGGER.info("  redo [N]");
    LOGGER.info("Description:");
    LOGGER.info("  Replays N previously undone moves.\n");
    LOGGER.verbose("  - If no number is specified, one move is redone.");
    LOGGER.verbose("  - If N is specified, N moves are redone.\n");
    LOGGER.verbose("Examples:");
    LOGGER.verbose("  redo      -> redo one move");
    LOGGER.verbose("  redo 3    -> redo three moves\n");
  }
}
