package fr.bordeaux.cli.command;

import fr.bordeaux.core.Board;
import fr.bordeaux.core.GameEngine;
import fr.bordeaux.core.Move;
import fr.bordeaux.core.Orientation;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;
import fr.bordeaux.util.Logger;
import fr.bordeaux.util.TimeConvertor;
import java.io.PrintStream;
import java.util.List;

/**
 * Command that displays various information about the current game, such as the board state,
 * configurations, history, or remaining time.
 */
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
   * @param t the tokens entered by the user
   */
  public ShowCommand(final String[] t) {
    this.tokens = t;
  }

  /**
   * Creates a ShowCommand with a specific option.
   *
   * @param t the option specifying what should be displayed
   */
  public ShowCommand(final String t) {
    this(new String[] {"show", t});
  }

  /**
   * Executes the show command and displays the requested information. The specific behavior depends
   * on the option provided in the tokens.
   *
   * @param engine the current game engine
   * @return the unchanged game engine
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
                  "Your time left : " + TimeConvertor.formatTime(player.getRemainingTime()));
            } else {
              writer.println(
                  "Time left for "
                      + player.getName()
                      + " ---> "
                      + TimeConvertor.formatTime(player.getRemainingTime()));
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
        LOGGER.error("The command \"show{0}\" doesn't exist...", option);
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
    LOGGER.info("  Displays information about the current game.\n");
    LOGGER.verbose("  If any option is given, displays the board by default");
    LOGGER.verbose("Options:");
    LOGGER.verbose("  board         : Displays the current board state.");
    LOGGER.verbose("  history       : Displays the list of moves played so far.");
    LOGGER.verbose("  configuration : Displays all the configuations set for the game.");
    LOGGER.verbose("  time          : Displays the remaining time for each player.\n");
    LOGGER.verbose("If you want the help with an option, you can enter \"help show [option]\".\n");
  }

  /** Displays help information for the "show board" option. */
  public void helpBoard() {
    LOGGER.info("=== SHOW BOARD ===\n");
    LOGGER.info("Usage:");
    LOGGER.info("  show board\n");
    LOGGER.info("Description:");
    LOGGER.info("  Displays the current state of the board.\n");
  }

  /** Displays help information for the "show history" option. */
  public void helpHistory() {
    LOGGER.info("=== SHOW HISTORY ===\n");
    LOGGER.info("Usage:");
    LOGGER.info("  show history\n");
    LOGGER.info("Description:");
    LOGGER.info("  Displays the chronological list of all moves played.\n");
  }

  /** Displays help information for the "show time" option. */
  public void helpTime() {
    LOGGER.info("=== SHOW TIME ===\n");
    LOGGER.info("Usage:");
    LOGGER.info("  show time\n");
    LOGGER.info("Description:");
    LOGGER.info("  Displays the remaining time for each player.\n");
    LOGGER.info("  Only relevant if a timed mode is active.\n");
  }

  /** Displays help information for the "show configuration" option. */
  public void helpConfig() {
    LOGGER.info("=== SHOW CONFIGURATION ===\n");
    LOGGER.info("Usage:");
    LOGGER.info("  show configuration\n");
    LOGGER.info("Description:");
    LOGGER.info("  Displays the current game configuration.\n");
    LOGGER.verbose("  This includes all active settings such as:");
    LOGGER.verbose("    - Board size");
    LOGGER.verbose("    - Number of players");
    LOGGER.verbose("    - Game mode (e.g., Blitz)");
    LOGGER.verbose("    - Debug mode");
    LOGGER.verbose("    - Any other configurable parameters\n");
    LOGGER.verbose("This command does not modify the game state.\n");
  }

  /**
   * Prints a textual representation of the current board state including player positions and
   * walls.
   *
   * @param engine the current game engine
   */
  private void printBoard(final GameEngine engine) {
    final Board b = engine.getState().getBoard();
    final int boardSize = b.getSize();
    for (int i = 0; i < boardSize; i++) {
      writer.print(((char) ('A' + i)) + " ");
    }
    writer.println("");
    for (int i = 0; i < boardSize; i++) {
      for (int j = 0; j < boardSize; j++) {
        boolean playerFound = false;
        for (final Player player : engine.getState().getPlayers()) {
          if (player.getPosition().equals(new Position(i, j))) {
            writer.print(player.getColor().getId() + printWall(b, i, j, false));
            playerFound = true;
            break;
          }
        }
        if (!playerFound) {
          writer.print("_" + ((j != boardSize - 1) ? printWall(b, i, j, false) : " "));
        }
        if (j == boardSize - 1) {
          writer.print(i + 1);
        }
      }
      if (i != boardSize - 1) {
        writer.println("");
        for (int j = 0; j < boardSize; j++) {
          writer.print(printWall(b, i, j, true));
        }
        writer.println("");
      }
    }
    writer.println("");
  }

  /**
   * Returns a string representation of a wall or connection between cells depending on the board
   * graph structure.
   *
   * @param b the game board
   * @param i the row index
   * @param j the column index
   * @param under indicates whether the wall is printed under the cell
   * @return the string representation of the wall
   */
  private String printWall(final Board b, final int i, final int j, final boolean under) {
    final String result;
    if (under) {
      if (i >= b.getSize() - 1 || !b.getGraph().hasEdge(b.getCellId(i, j), b.getCellId(i + 1, j))) {
        result = "X ";
      } else {
        result = ". ";
      }
    } else {
      if (j >= b.getSize() - 1 || !b.getGraph().hasEdge(b.getCellId(i, j), b.getCellId(i, j + 1))) {
        result = "X";
      } else {
        result = " ";
      }
    }
    return result;
  }

  /**
   * Converts a board position into a human-readable string. The format follows the CLI coordinate
   * notation (e.g., a0, b3).
   *
   * @param p the position to convert
   * @return the string representation of the position
   */
  private String posToString(final Position p) {
    final char letter = (char) ('a' + p.getY());
    final int number = p.getX() + 1;
    return String.valueOf(letter) + number;
  }

  /**
   * Displays the complete history of moves played during the game. Format: [Player Number] -
   * [StartPos]-[EndPos] for pawns, or [Player Number] - [Pos][Orientation] for walls.
   *
   * @param engine The game engine containing the current state and move history.
   */
  public void showHistory(final GameEngine engine) {
    writer.println("Oldest move");
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
    writer.println("Newest move");
  }
}
