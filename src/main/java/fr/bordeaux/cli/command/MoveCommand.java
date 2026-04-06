package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;
import fr.bordeaux.core.Move;
import fr.bordeaux.core.Orientation;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;
import fr.bordeaux.core.RuleChecker;
import fr.bordeaux.util.Logger;

/**
 * CLI command that interprets and executes a player move. pawn movement or a wall placement
 * depending on the format of the input string.
 */
public class MoveCommand implements Command {

  /** Move that has been entered by the user */
  private String move;

  /** Logger to display informations */
  private static final Logger LOGGER = Logger.getInstance();

  /**
   * Creates a MoveCommand using the provided move string.
   *
   * @param moveInput the move entered by the user
   */
  public MoveCommand(final String moveInput) {
    move = moveInput;
  }

  /** Creates a MoveCommand with no predefined move. */
  public MoveCommand() {
    this(null);
  }

  /**
   * Executes move command. Parses pawn move or wall placement. placement and delegates the
   * processing to the appropriate handler.
   *
   * @param engine the current game engine
   * @return updated game engine or original if invalid. is invalid
   */
  @Override
  public GameEngine execute(final GameEngine engine) {
    try {
      if (move.contains("-") && (move.length() == 5 || move.length() == 7)) {
        LOGGER.debug("You played: {0}", this.move);
        return handleMove(engine);
      } else if ((move.contains("v") || move.contains("h")) && move.length() == 3) {
        LOGGER.debug("You played: {0}", this.move);
        return handleWall(engine);
      } else {
        LOGGER.debug("Please use help to see how to give a valid command");
        return engine;
      }
    } catch (final Exception e) {
      LOGGER.error("Error while reading the move {0}", this.move);
      return engine;
    }
  }

  /** Displays help information for the move command. in the CLI. */
  @Override
  public void helpText() {
    LOGGER.info("=== Move Commands ===\n");
    LOGGER.info("Usage:");
    LOGGER.info("To move a pawn : e2-e3 (if it's legal).");
    LOGGER.info(
        "        Remember that you can't neither move diagonally nor jump over a wall nor exit the board.");
    LOGGER.info("To place a wall : e2h (if it's legal).");
    LOGGER.info("        It place the wall horizontally under the case e2 and under the case f2.");
    LOGGER.info(
        "        You can't place a wall horizontally if it's the right edge of the board (colonne E for a 5x5 board).");
    LOGGER.info("                : e2v (if it's legal).");
    LOGGER.info("        It place the wall vertically next to the case e2 and next to the case e1");
    LOGGER.info(
        "        You can't place a wall vertically if it's the top edge of the board. (row 1 on any board).\n");
  }

  /**
   * Converts a position string to a Position object (e.g. "e2"). letter followed by a number (e.g.,
   * "e2").
   *
   * @param str the string representing the board position
   * @return the corresponding Position object
   * @throws IllegalArgumentException if the format is invalid
   */
  public Position parsePosition(final String str) {
    if (str.length() < 2) {
      LOGGER.error("Usage: c3-c4 / c3h / c3v");
      throw new IllegalArgumentException("Invalid position format");
    }
    final int row = Integer.parseInt(str.substring(1)) - 1;
    final int col = str.charAt(0) - 'a';

    return new Position(row, col);
  }

  /**
   * Handles execution of a pawn move, validating format and positions. applies the move to the game
   * engine.
   *
   * @param engine the current game engine
   * @return the updated game engine after the move
   */
  public GameEngine handleMove(final GameEngine engine) {
    final String[] moves = move.split("-");
    if (moves.length != 2) {
      LOGGER.error("Usage: c3-c4 / c3h / c3v");
      throw new IllegalArgumentException("Invalid move syntax");
    }
    final Position from = parsePosition(moves[0]);
    final Position targetPosition = parsePosition(moves[1]);

    final Player current = engine.getState().getCurrentPlayer();
    if (!current.getPosition().equals(from)) {
      LOGGER.error("There is no pawn on the case you want to play");
      throw new IllegalArgumentException("No pawn at " + moves[0]);
    }
    engine.play(Move.pawn(from, targetPosition));
    (new ShowCommand()).execute(engine);
    return engine;
  }

  /**
   * Handles wall placement by extracting position and orientation. string and applies the wall
   * placement to the game engine.
   *
   * @param engine the current game engine
   * @return the updated game engine after the wall is placed
   */
  public GameEngine handleWall(final GameEngine engine) {
    final Position pos = parsePosition(move.substring(0, move.length() - 1));
    final Orientation orientation =
        (move.charAt(move.length() - 1) == 'v') ? Orientation.VERTICAL : Orientation.HORIZONTAL;
    if (!RuleChecker.isMoveLegal(engine.getState(), Move.wall(pos, orientation))) {
      LOGGER.error("That''s an illegal move: {0}", pos);
      return engine;
    }
    engine.play(Move.wall(pos, orientation));
    (new ShowCommand()).execute(engine);
    return engine;
  }
}
