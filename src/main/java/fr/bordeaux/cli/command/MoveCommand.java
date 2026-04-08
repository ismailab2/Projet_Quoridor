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
        LOGGER.debug(I18n.get("youPlayed") + " {0}", this.move);
        return handleMove(engine);
      } else if ((move.contains("v") || move.contains("h")) && move.length() == 3) {
        LOGGER.debug(I18n.get("youPlayed") + " {0}", this.move);
        return handleWall(engine);
      } else {
        LOGGER.debug(I18n.get("helpValidCommand"));
        return engine;
      }
    } catch (final Exception e) {
      LOGGER.error(I18n.get("readingMove") + " {0}", this.move);
      return engine;
    }
  }

  /** Displays help information for the move command. in the CLI. */
  @Override
  public void helpText() {
    LOGGER.info("=== Move Commands ===\n");
    LOGGER.info("Usage:");
    LOGGER.info(I18n.get("pawnMove", "e2-e3", I18n.get("ifLegal")));
    LOGGER.info("        " + I18n.get("illegalPawnMove"));
    LOGGER.info(I18n.get("pawnMove", "e2h", I18n.get("ifLegal")));
    LOGGER.info(
        "        "
            + I18n.get(
                "placeWall", I18n.get("horizontally"), I18n.get("under"), I18n.get("e2f2cases")));
    LOGGER.info(
        "        " + I18n.get("cantPlace", I18n.get("horizontally"), I18n.get("rightEdge")));
    LOGGER.info("                : e2v " + I18n.get("ifLegal"));
    LOGGER.info(
        "        "
            + I18n.get(
                "placeWall", I18n.get("vertically"), I18n.get("next"), I18n.get("e2f2cases")));
    LOGGER.info("        " + I18n.get("cantPlace", I18n.get("vertically"), I18n.get("bottomEdge")));
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
      throw new IllegalArgumentException(I18n.get("positionFormat"));
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
      throw new IllegalArgumentException(I18n.get("moveSyntax"));
    }
    final Position from = parsePosition(moves[0]);
    final Position targetPosition = parsePosition(moves[1]);

    final Player current = engine.getState().getCurrentPlayer();
    if (!current.getPosition().equals(from)) {
      LOGGER.error(I18n.get("noPawnPlayed"));
      throw new IllegalArgumentException(I18n.get("noPawn", moves[0]));
    }
    if (!engine.play(Move.pawn(from, targetPosition))) {
      LOGGER.error("That''s an illegal move: {0} to {1}", from, targetPosition);
      return engine;
    }

    new ShowCommand().execute(engine);
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
      LOGGER.error(I18n.get("moveIllegal") + " {0}", pos);
      return engine;
    }
    engine.play(Move.wall(pos, orientation));
    new ShowCommand().execute(engine);
    return engine;
  }
}
