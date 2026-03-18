package fr.bordeaux.cli.command;

import fr.bordeaux.ai.MCTSPlayer;
import fr.bordeaux.core.GameEngine;
import fr.bordeaux.core.Move;
import fr.bordeaux.core.Orientation;
import fr.bordeaux.core.Position;
import fr.bordeaux.core.RuleChecker;
import java.io.PrintStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command that suggests the best possible move for the current player. The move is computed using a
 * Minimax AI based on the current game state.
 */
public class HintCommand implements Command {

  /** Logger for AI calculation messages. */
  private static final Logger logger = LoggerFactory.getLogger(HintCommand.class);

  /** Stream where to display texts. */
  private final PrintStream writer = System.out;

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
  public GameEngine execute(GameEngine engine) {
    logger.info("Calculating best move...");
    /*     MinimaxPlayer ia =
    MinimaxPlayer.builder("hint", engine.getState().getCurrentPlayer().getPosition())
        .withHeuristic("advanced")
        .build(); */
    MCTSPlayer ia = new MCTSPlayer("hint", engine.getState().getCurrentPlayer().getPosition());
    Move hint = ia.getNextMove(engine.getState());

    logger.debug("Move legality check: {}", RuleChecker.isMoveLegal(engine.getState(), hint));

    String bestMove = formatMove(hint);
    logger.info("Best move : {}", bestMove);
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
    logger.info("=== HINT Command ===\n");
    logger.info("Usage:");
    logger.info("  hint\n");
    logger.info("Description:");
    logger.info("  Provides a suggestion for the next move.\n");
    logger.info("  The suggested move is computed based on the current game state.\n");
    logger.debug("Displays:");
    logger.debug("  Should display a legal pawn or wall move.\n  Example:");
    logger.debug("    Pawn move -> b3-b4");
    logger.debug("    Wall move -> b3v / b3h");
  }
}
