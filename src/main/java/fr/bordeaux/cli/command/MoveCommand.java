package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;
import fr.bordeaux.core.Move;
import fr.bordeaux.core.Orientation;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;
import fr.bordeaux.core.RuleChecker;
import fr.bordeaux.i18n.I18n;
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
      if (move.contains("-") && (move.length() >= 5 || move.length() <= 7)) {
        LOGGER.debug(I18n.get("youPlayed", this.move));
        return handleMove(engine);
      } else if ((move.contains("v") || move.contains("h")) && move.length() == 3) {
        LOGGER.debug(I18n.get("youPlayed", this.move));
        return handleWall(engine);
      } else {
        LOGGER.debug(I18n.get("helpValidCommand"));
        return engine;
      }
    } catch (Exception e) {
      LOGGER.error(I18n.get("readingMove", this.move));
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
    LOGGER.info(I18n.get("pawnMove", "e2-e3", I18n.get("ifLegal")));
    LOGGER.info("        "+I18n.get("illegalPawnMove"));
    LOGGER.info(I18n.get("pawnMove", "e2h", I18n.get("ifLegal")));
    LOGGER.info("        "+I18n.get("placeWall", I18n.get("horizontally"), I18n.get("under"), I18n.get("e2f2cases")));
    LOGGER.info(
        "        "+I18n.get("cantPlace", I18n.get("horizontally"), I18n.get("rightEdge")));
    LOGGER.info("                : e2v "+I18n.get("ifLegal"));
    LOGGER.info("        "+I18n.get("placeWall", I18n.get("vertically"), I18n.get("next"), I18n.get("e2f2cases")));
    LOGGER.info(
        "        "+I18n.get("cantPlace", I18n.get("vertically"), I18n.get("bottomEdge")));
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
      throw new IllegalArgumentException(I18n.get("positionFormat"));
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
      throw new IllegalArgumentException(I18n.get("moveSyntax"));
    }
    Position from = parsePosition(moves[0]);
    Position to = parsePosition(moves[1]);

    Player current = engine.getState().getCurrentPlayer();
    if (!current.getPosition().equals(from)) {
      LOGGER.error(I18n.get("noPawnPlayed"));
      throw new IllegalArgumentException(I18n.get("noPawn", moves[0]));
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
      LOGGER.error(I18n.get("moveIllegal", posToString(pos)));
      return engine;
    }
    engine.play(Move.wall(pos, orientation));
    (new ShowCommand()).execute(engine);
    return engine;
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
}
