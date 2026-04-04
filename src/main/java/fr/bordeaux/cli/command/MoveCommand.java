package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;
import fr.bordeaux.core.Move;
import fr.bordeaux.core.Orientation;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;
import fr.bordeaux.core.RuleChecker;
import fr.bordeaux.util.Logger;

/**
 * Command that interprets and executes a player move entered in the CLI. A move can either be a
 * pawn movement or a wall placement depending on the format of the input string.
 */
public class MoveCommand implements Command {

  /** Move that has been entered by the user */
  private String move;

  /** Logger to display informations */
  private static final Logger LOGGER = Logger.getInstance();

  /**
   * Creates a MoveCommand using the provided move string.
   *
   * @param m the move entered by the user
   */
  public MoveCommand(String m) {
    move = m;
  }

  /** Creates a MoveCommand with no predefined move. */
  public MoveCommand() {
    this(null);
  }

  /**
   * Executes the move command. Determines whether the input represents a pawn move or a wall
   * placement and delegates the processing to the appropriate handler.
   *
   * @param engine the current game engine
   * @return the updated game engine after the move is applied, or the unchanged engine if the move
   *     is invalid
   */
  @Override
  public GameEngine execute(GameEngine engine) {
    try {
      if (move.contains("-") && (move.length() == 5 || move.length() == 7)) {
        LOGGER.debug("You played: " + this.move);
        return handleMove(engine);
      } else if ((move.contains("v") || move.contains("h")) && move.length() == 3) {
        LOGGER.debug("You played: " + this.move);
        return handleWall(engine);
      } else {
        LOGGER.debug("Please use help to see how to give a valid command");
        return engine;
      }
    } catch (Exception e) {
      LOGGER.error("Error while reading the move " + this.move);
      return engine;
    }
  }

  /**
   * Displays help information about the move command, explaining how to move a pawn or place a wall
   * in the CLI.
   */
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
   * Parses a position string and converts it into a Position object. The expected format is a
   * letter followed by a number (e.g., "e2").
   *
   * @param str the string representing the board position
   * @return the corresponding Position object
   * @throws IllegalArgumentException if the format is invalid
   */
  public Position parsePosition(String str) {
    if (str.length() < 2) {
      LOGGER.error("Usage: c3-c4 / c3h / c3v");
      throw new IllegalArgumentException("Invalid position format");
    }
    int row = Integer.parseInt(str.substring(1)) - 1;
    int col = str.charAt(0) - 'a';

    return new Position(row, col);
  }

  /**
   * Handles the execution of a pawn move. Validates the move format, converts the positions, and
   * applies the move to the game engine.
   *
   * @param engine the current game engine
   * @return the updated game engine after the move
   */
  public GameEngine handleMove(GameEngine engine) {
    String[] moves = move.split("-");
    if (moves.length != 2) {
      LOGGER.error("Usage: c3-c4 / c3h / c3v");
      throw new IllegalArgumentException("Invalid move syntax");
    }
    Position from = parsePosition(moves[0]);
    Position to = parsePosition(moves[1]);

    Player current = engine.getState().getCurrentPlayer();
    if (!current.getPosition().equals(from)) {
      LOGGER.error("There is no pawn on the case you want to play");
      throw new IllegalArgumentException("No pawn at " + moves[0]);
    }
    engine.play(Move.pawn(from, to));
    (new ShowCommand()).execute(engine);
    return engine;
  }

  /**
   * Handles the execution of a wall placement. Extracts the position and orientation from the move
   * string and applies the wall placement to the game engine.
   *
   * @param engine the current game engine
   * @return the updated game engine after the wall is placed
   */
  public GameEngine handleWall(GameEngine engine) {
    Position pos = parsePosition(move.substring(0, move.length() - 1));
    Orientation orientation =
        (move.charAt(move.length() - 1) == 'v') ? Orientation.VERTICAL : Orientation.HORIZONTAL;
    if (!RuleChecker.isMoveLegal(engine.getState(), Move.wall(pos, orientation))) {
      LOGGER.error("That's an illegal move: " + pos);
      return engine;
    }
    engine.play(Move.wall(pos, orientation));
    (new ShowCommand()).execute(engine);
    return engine;
  }
}
