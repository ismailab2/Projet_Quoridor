package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command that replays moves that were previously undone. The user can redo a single move or
 * multiple moves.
 */
public class RedoCommand implements Command {
  /** Command tokens. */
  private String[] tokens;

  /** Logger to trace redo actions. */
  private static final Logger logger = LoggerFactory.getLogger(RedoCommand.class);

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
      logger.info("Usage: redo [N]");
      return engine;
    }
    int count = (tokens.length > 1) ? Integer.parseInt(tokens[1]) : 1;
    logger.info("Redoing {} move(s)...", count);

    for (int i = 0; i < count; i++) {
      engine.getState().redo();
    }
    return engine;
  }

  /** Displays help information about the redo command, including its usage and examples. */
  @Override
  public void helpText() {
    logger.info("=== REDO Command ===\n");
    logger.info("Usage:");
    logger.info("  redo");
    logger.info("  redo [N]");
    logger.info("Description:");
    logger.info("  Replays N previously undone moves.\n");
    logger.debug("  - If no number is specified, one move is redone.");
    logger.debug("  - If N is specified, N moves are redone.\n");
    logger.debug("Examples:");
    logger.debug("  redo      -> redo one move");
    logger.debug("  redo 3    -> redo three moves\n");
  }
}
