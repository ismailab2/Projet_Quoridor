package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;
import fr.bordeaux.core.Move;
import fr.bordeaux.core.Orientation;
import fr.bordeaux.core.Position;
import fr.bordeaux.i18n.I18n;
import fr.bordeaux.util.Logger;
import java.io.IOException;

/**
 * Suggests the best move computed by the AI based on the current state. Minimax AI based on the
 * current game state.
 */
public class HintCommand implements Command {

  /** Logger for AI calculation messages. */
  private static final Logger LOGGER = Logger.getInstance();

  /** Creates a new HintCommand. */
  public HintCommand() {
    LOGGER.debug("Instantiating HintCommand");
  }

  /**
   * Executes the hint command, suggesting the best move. state and suggests the best move for the
   * current player.
   *
   * @param engine the current game engine
   * @return the unchanged game engine
   */
  @Override
  public GameEngine execute(final GameEngine engine) throws IOException {
    LOGGER.info(I18n.get("hintCalc"));
    final Move hint = engine.hint();

    final String bestMove = formatMove(hint);
    LOGGER.info(I18n.get("hintPlay") + " {0}", bestMove);
    return engine;
  }

  /**
   * Formats a Move object into a readable string (e.g., e2-e3 or a1h).
   *
   * @param move the move to format
   * @return the string representation of the move
   */
  private String formatMove(final Move move) {
    if (move.getType() == Move.Type.PAWN) {
      return posToString(move.getFrom()) + "-" + posToString(move.getTo());
    }
    final char orientationChar;
    if (move.getOrientation() == Orientation.HORIZONTAL) {
      orientationChar = 'h';
    } else {
      orientationChar = 'v';
    }
    return posToString(move.getTo()) + orientationChar;
  }

  /**
   * Converts board position to a human-readable CLI coordinate string. notation (e.g., a0, b3).
   *
   * @param position the position to convert
   * @return the string representation of the position
   */
  private String posToString(final Position position) {
    return (char) ('a' + position.getY()) + "" + (position.getX() + 1);
  }

  /** Displays help information for the hint command. */
  @Override
  public void helpText() {
    LOGGER.info("=== HINT Command ===\n");
    LOGGER.info("Usage:");
    LOGGER.info("  hint\n");
    LOGGER.info("Description:");
    LOGGER.info("  " + I18n.get("hintHelp1"));
    LOGGER.verbose("  " + I18n.get("hintHelp2"));
    LOGGER.verbose("  " + I18n.get("hintHelpEx"));
  }
}
