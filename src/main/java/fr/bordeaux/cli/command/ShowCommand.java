package fr.bordeaux.cli.command;

import fr.bordeaux.core.Board;
import fr.bordeaux.core.GameEngine;
import fr.bordeaux.core.Move;
import fr.bordeaux.core.Orientation;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;
import fr.bordeaux.i18n.I18n;
import fr.bordeaux.util.Logger;
import fr.bordeaux.util.TimeConvertor;
import java.io.PrintStream;
import java.util.List;

/** CLI command that displays game information, board state, or history. */
public class ShowCommand implements Command {

  /** Command entered by the user */
  private String[] tokens;

  /** Logger to display informations */
  private static final Logger LOGGER = Logger.getInstance();

  /** Where the show command will write */
  private final PrintStream writer = System.out;

  /** Creates a ShowCommand with the default option "board". */
  public ShowCommand() {
    this(new String[] {"show", "board"});
  }

  /**
   * Creates a ShowCommand using the provided CLI tokens.
   *
   * @param tokens the tokens entered by the user
   */
  public ShowCommand(final String[] tokens) {
    this.tokens = tokens;
  }

  /**
   * Creates a ShowCommand with a specific option.
   *
   * @param optionString the option specifying what should be displayed
   */
  public ShowCommand(final String optionString) {
    this(new String[] {"show", optionString});
  }

  /**
   * @return unchanged engine.
   */
  @Override
  public GameEngine execute(final GameEngine engine) {
    final String option = tokens[1];
    switch (option) {
      case "board":
        {
          printBoard(engine);
          break;
        }
      case "configurations":
      case "config":
        {
          writer.println("verbose = " + engine.isVerbose());
          writer.println("blitz = " + engine.isBlitz());
          writer.println("contest = " + engine.isContest());
          writer.println("debug = " + engine.isDebug());
          writer.println("nb-walls = " + engine.getInitialWalls());
          writer.println("nb-players = " + engine.getState().getPlayers().size());
          break;
        }
      case "time":
        {
          final List<Player> players = engine.getState().getPlayers();
          final Player currentPlayer = engine.getState().getCurrentPlayer();
          for (final Player player : players) {
            if (player.equals(currentPlayer)) {
              writer.println(
                  I18n.get("timeLeft") + TimeConvertor.formatTime(player.getRemainingTime()));
            } else {
              writer.println(
                  I18n.get(
                      "timeFor",
                      player.getName(),
                      " ---> ",
                      TimeConvertor.formatTime(player.getRemainingTime())));
            }
          }

          break;
        }
      case "history":
        {
          showHistory(engine);
          break;
        }
      default:
        LOGGER.error(I18n.get("showNot", tokens[1], I18n.get("notExist")));
        break;
    }
    return engine;
  }

  /** Displays general help information for the show command. */
  @Override
  public void helpText() {
    LOGGER.info("=== SHOW Command ===\n");
    LOGGER.info("Usage:");
    LOGGER.info("  show board");
    LOGGER.info("  show history");
    LOGGER.info("  show configuration");
    LOGGER.info("  show time\n");
    LOGGER.info("Description:");
    LOGGER.info("  " + I18n.get("showDesc"));
    LOGGER.verbose("  " + I18n.get("showVerb"));
    LOGGER.verbose("Options:");
    LOGGER.verbose("  board         : " + I18n.get("showVBoard"));
    LOGGER.verbose("  history       : " + I18n.get("showVHisto"));
    LOGGER.verbose("  configuration : " + I18n.get("showVConfig"));
    LOGGER.verbose("  time          : " + I18n.get("showVTime"));
    LOGGER.verbose(I18n.get("showVMore"));
  }

  /** Displays help information for the "show board" option. */
  public void helpBoard() {
    LOGGER.info("=== SHOW BOARD ===\n");
    LOGGER.info("Usage:");
    LOGGER.info("  show board\n");
    LOGGER.info("Description:");
    LOGGER.info("  " + I18n.get("showDescBoard"));
  }

  /** Displays help information for the "show history" option. */
  public void helpHistory() {
    LOGGER.info("=== SHOW HISTORY ===\n");
    LOGGER.info("Usage:");
    LOGGER.info("  show history\n");
    LOGGER.info("Description:");
    LOGGER.info("  " + I18n.get("showDescHisto"));
  }

  /** Displays help information for the "show time" option. */
  public void helpTime() {
    LOGGER.info("=== SHOW TIME ===\n");
    LOGGER.info("Usage:");
    LOGGER.info("  show time\n");
    LOGGER.info("Description:");
    LOGGER.info("  " + I18n.get("showDescTime"));
  }

  /** Displays help information for the "show configuration" option. */
  public void helpConfig() {
    LOGGER.info("=== SHOW CONFIGURATION ===\n");
    LOGGER.info("Usage:");
    LOGGER.info("  show configuration\n");
    LOGGER.info("Description:");
    LOGGER.info("  " + I18n.get("showDescConfig"));
    LOGGER.verbose("  " + I18n.get("showDescVConfig"));
    LOGGER.verbose("    " + I18n.get("showCfgs"));
    LOGGER.verbose(I18n.get("showCfgsMore"));
  }

  /**
   * Prints the board state, player positions, and walls.
   *
   * @param engine the current game engine
   */
  private void printBoard(final GameEngine engine) {
    final Board board = engine.getState().getBoard();
    final int boardSize = board.getSize();
    for (int col = 0; col < boardSize; col++) {
      writer.print(((char) ('A' + col)) + " ");
    }
    writer.println("");
    for (int row = 0; row < boardSize; row++) {
      for (int col = 0; col < boardSize; col++) {
        boolean playerFound = false;
        for (final Player player : engine.getState().getPlayers()) {
          if (player.getPosition().equals(new Position(row, col)) && !player.isDisabled()) {
            writer.print(
                player.getColor().getId()
                    + ((col != boardSize - 1) ? printWall(board, row, col, false) : " "));
            playerFound = true;
            break;
          }
        }
        if (!playerFound) {
          writer.print("_" + ((col != boardSize - 1) ? printWall(board, row, col, false) : " "));
        }
        if (col == boardSize - 1) {
          writer.print(row + 1);
        }
      }
      if (row != boardSize - 1) {
        writer.println("");
        for (int col = 0; col < boardSize; col++) {
          writer.print(printWall(board, row, col, true));
        }
        writer.println("");
      }
    }
    writer.println("");
  }

  /**
   * @return wall representation based on graph.
   */
  private String printWall(final Board board, final int row, final int col, final boolean under) {
    final String result;
    if (under) {
      if (row >= board.getSize() - 1) {
        result = "  ";
      } else {
        result =
            board.getGraph().hasEdge(board.getCellId(row, col), board.getCellId(row + 1, col))
                ? ". "
                : "X ";
      }
    } else {
      if (col >= board.getSize() - 1) {
        result = " ";
      } else {
        result =
            board.getGraph().hasEdge(board.getCellId(row, col), board.getCellId(row, col + 1))
                ? " "
                : "X";
      }
    }
    return result;
  }

  /**
   * Converts board position to a human-readable CLI coordinate. notation (e.g., a0, b3).
   *
   * @param position the position to convert
   * @return the string representation of the position
   */
  private String posToString(final Position position) {
    final char letter = (char) ('a' + position.getY());
    final int number = position.getX() + 1;
    return String.valueOf(letter) + number;
  }

  /**
   * Displays game history. Player - Move.
   *
   * @param engine the current game engine
   */
  public void showHistory(final GameEngine engine) {
    writer.println(I18n.get("oldest"));
    final List<Player> players = engine.getState().getPlayers();
    final int nbPlayers = players.size();
    int count = 0;
    final Move[] moves = engine.getState().getMovesPlayed();
    for (final Move move : moves) {
      final StringBuilder strMove = new StringBuilder();
      strMove.append("Player ").append(players.get(count % nbPlayers).getName()).append(" - ");
      if (move.isPawn()) {
        final Position from = move.getFrom();
        strMove.append(posToString(from)).append('-').append(posToString(move.getTo()));
      } else {
        final Orientation orientation = move.getOrientation();
        strMove.append(posToString(move.getTo())).append(orientation.toString());
      }
      count++;
      strMove.append(';');
      writer.println(strMove.toString());
    }
    writer.println(I18n.get("newest"));
  }
}
