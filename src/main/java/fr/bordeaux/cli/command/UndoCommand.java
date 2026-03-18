package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;
import java.io.PrintStream;

/**
 * Command that cancels previously played moves. The user can undo a single move or multiple moves.
 */
public class UndoCommand implements Command {
  /** Command tokens. */
  private String[] tokens;

  /** Logger to trace undo actions. */
  private static final Logger logger = LoggerFactory.getLogger(UndoCommand.class);

  /** Stream where to display texts. */
  private final PrintStream writer = System.out;

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
      logger.info("Usage: undo [N]");
      return engine;
    }
    int count = (tokens.length > 1) ? Integer.parseInt(tokens[1]) : 1;
    logger.info("Undoing {} move(s)...", count);

    for (int i = 0; i < count; i++) {
      engine.getState().undo();
    }
    return engine;
  }

  /** Displays help information about the undo command, including its usage and examples. */
  @Override
  public void helpText() {
    logger.info("=== UNDO Command ===\n");
    logger.info("Usage:");
    logger.info("  undo");
    logger.info("  undo [N]\n");
    logger.info("Description:");
    logger.info("  Cancels previously played moves.\n");
    logger.debug("  - If no number is specified, one move is undone.");
    logger.debug("  - If N is specified, N moves are undone.\n");
    logger.debug("Examples:");
    logger.debug("  undo      -> undo last move");
    logger.debug("  undo 2    -> undo last two moves\n");
  }
}
