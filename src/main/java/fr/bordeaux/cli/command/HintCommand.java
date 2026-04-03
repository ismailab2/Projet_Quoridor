package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;
import fr.bordeaux.core.Move;
import fr.bordeaux.core.Orientation;
import fr.bordeaux.core.Position;
import fr.bordeaux.core.RuleChecker;
import fr.bordeaux.i18n.I18n;
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
  public GameEngine execute(GameEngine engine) throws IOException {
    LOGGER.info(I18n.get("hintCalc"));
    Move hint = engine.hint();

    String bestMove = formatMove(hint);
    LOGGER.info(I18n.get("hintPlay", bestMove));
    return engine;
  }

  /**
   * Formats a Move object into a readable string (e.g., e2-e3 or a1h).
   *
   * @param m the move to format
   * @return the string representation of the move
   */
  private String formatMove(Move m) {
    if (m.getType() == Move.Type.PAWN) {
      return posToString(m.getFrom()) + "-" + posToString(m.getTo());
    } else {
      char orientationChar = m.getOrientation() == Orientation.HORIZONTAL ? 'h' : 'v';
      return posToString(m.getTo()) + orientationChar;
    }
  }

  /**
   * Converts a board position into a human-readable string. The format follows the CLI coordinate
   * notation (e.g., a0, b3).
   *
   * @param p the position to convert
   * @return the string representation of the position
   */
  private String posToString(Position p) {
    return (char) ('a' + p.getY()) + "" + (p.getX() + 1);
  }

  /** Displays help information about the hint command, including its usage and description. */
  @Override
  public void helpText() {
    LOGGER.info("=== HINT Command ===\n");
    LOGGER.info("Usage:");
    LOGGER.info("  hint\n");
    LOGGER.info("Description:");
    LOGGER.info("  "+I18n.get("hintHelp1"));
    LOGGER.verbose("  "+I18n.get("hintHelp2"));
    LOGGER.verbose("  "+I18n.get("hintHelpEx"));
  }
}
