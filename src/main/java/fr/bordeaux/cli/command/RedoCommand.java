package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;
import fr.bordeaux.util.Logger;

/**
 * Command that replays moves that were previously undone. The user can redo a single move or
 * multiple moves.
 */
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
   * @param t the tokens entered by the user
   */
  public RedoCommand(String[] t) {
    this.tokens = t;
  }

  /**
   * Executes the redo command. Replays the specified number of undone moves.
   *
   * @param engine the current game engine
   * @return the updated game engine after redoing moves
   */
  @Override
  public GameEngine execute(GameEngine engine) {
    if (tokens.length >= 3) {
      LOGGER.info("Usage: redo [N]");
      return engine;
    }
    int count = (tokens.length > 1) ? Integer.parseInt(tokens[1]) : 1;
    LOGGER.info("Redoing {0} move(s)...", count);

    for (int i = 0; i < count; i++) {
      engine.getState().redo();
    }
    return engine;
  }

  /** Displays help information about the redo command, including its usage and examples. */
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
