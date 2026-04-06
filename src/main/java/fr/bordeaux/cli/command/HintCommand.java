package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;
import fr.bordeaux.core.Move;
import fr.bordeaux.core.Orientation;
import fr.bordeaux.core.Position;
import fr.bordeaux.core.RuleChecker;
import fr.bordeaux.util.Logger;
import java.io.IOException;

/**
 * Command that suggests the best possible move for the current player. The move is computed using a
 * Minimax AI based on the current game state.
 */
public class HintCommand implements Command {

  /** Logger for AI calculation messages. */
  private static final Logger LOGGER = Logger.getInstance();

  /** Creates a new HintCommand. */
  public HintCommand() {}

  /**
   * Executes the hint command. It creates a temporary AI player that evaluates the current game
   * state and suggests the best move for the current player.
   *
   * @param engine the current game engine
   * @return the unchanged game engine
   */
  @Override
  public GameEngine execute(final GameEngine engine) throws IOException {
    LOGGER.info("Calculating best move...");
    final Move hint = engine.hint();

    if (!RuleChecker.isMoveLegal(engine.getState(), hint)) {
      LOGGER.error("Suggested move: {0} isn''t legal", hint);
    }
    final String bestMove = formatMove(hint);
    LOGGER.info("Best move : {0}", bestMove);
    return engine;
  }

  /**
   * Formats a Move object into a readable string (e.g., e2-e3 or a1h).
   *
   * @param m the move to format
   * @return the string representation of the move
   */
  private String formatMove(final Move m) {
    if (m.getType() == Move.Type.PAWN) {
      return posToString(m.getFrom()) + "-" + posToString(m.getTo());
    }
    final char orientationChar;
    if (m.getOrientation() == Orientation.HORIZONTAL) {
      orientationChar = 'h';
    } else {
      orientationChar = 'v';
    }
    return posToString(m.getTo()) + orientationChar;
  }

  /**
   * Converts a board position into a human-readable string. The format follows the CLI coordinate
   * notation (e.g., a0, b3).
   *
   * @param p the position to convert
   * @return the string representation of the position
   */
  private String posToString(final Position p) {
    return (char) ('a' + p.getY()) + "" + (p.getX() + 1);
  }

  /** Displays help information about the hint command, including its usage and description. */
  @Override
  public void helpText() {
    LOGGER.info("=== HINT Command ===\n");
    LOGGER.info("Usage:");
    LOGGER.info("  hint\n");
    LOGGER.info("Description:");
    LOGGER.info("  Provides a suggestion for the next move.\n");
    LOGGER.info("  The suggested move is computed based on the current game state.\n");
    LOGGER.verbose("Displays:");
    LOGGER.verbose("  Should display a legal pawn or wall move.");
    LOGGER.verbose("Example:");
    LOGGER.verbose("  Pawn move -> b3-b4");
    LOGGER.verbose("  Wall move -> b3v / b3h");
  }
}
