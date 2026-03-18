package fr.bordeaux.cli.command;

import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.core.Board;
import fr.bordeaux.core.GameEngine;
import fr.bordeaux.core.Move;
import fr.bordeaux.core.Orientation;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;
import java.io.PrintStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command that displays various information about the current game, such as the board state,
 * configurations, history, or remaining time.
 */
public class ShowCommand implements Command {

  /** Command entered by the user */
  private String[] tokens;

  /** Logger to display informations */
  private static final Logger logger = LoggerFactory.getLogger(ShowCommand.class);

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
  public ShowCommand(String[] t) {
    this.tokens = t;
  }

  /**
   * Creates a ShowCommand with a specific option.
   *
   * @param t the option specifying what should be displayed
   */
  public ShowCommand(String t) {
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
  public GameEngine execute(GameEngine engine) {
    switch (tokens[1]) {
      case "board":
        {
          printBoard(engine);
          break;
        }
      case "configurations":
      case "config":
        {
          String[] configs =
              new String[] {
                "verbose", "blitz", "timeout", "ai", "turns-limit", "debug", "nb-players"
              };
          for (String cf : configs) {
            System.out.println(cf + " = " + ConfigManager.getInstance().getOption(cf, "None"));
          }
          break;
        }
      case "time":
        {
          System.out.println("time");
          break;
        }
      case "history":
        {
          showHistory(engine);
          break;
        }
      default:
        logger.error("The command  \"show " + tokens[1] + "\" doesn't exist...");
        break;
    }
    return engine;
  }

  /** Displays general help information for the show command. */
  @Override
  public void helpText() {
    logger.info("=== SHOW Command ===\n");
    logger.info("Usage:");
    logger.info("  show board");
    logger.info("  show history");
    logger.info("  show configuration");
    logger.info("  show time\n");
    logger.info("Description:");
    logger.info("  Displays information about the current game.\n");
    logger.debug("  If any option is given, displays the board by default");
    logger.debug("Options:");
    logger.debug("  board         : Displays the current board state.");
    logger.debug("  history       : Displays the list of moves played so far.");
    logger.debug("  configuration : Displays all the configuations set for the game.");
    logger.debug("  time          : Displays the remaining time for each player.\n");
    logger.debug("If you want the help with an option, you can enter \"help show [option]\".\n");
  }

  /** Displays help information for the "show board" option. */
  public void helpBoard() {
    logger.info("=== SHOW BOARD ===\n");
    logger.info("Usage:");
    logger.info("  show board\n");
    logger.info("Description:");
    logger.info("  Displays the current state of the board.\n");
  }

  /** Displays help information for the "show history" option. */
  public void helpHistory() {
    logger.info("=== SHOW HISTORY ===\n");
    logger.info("Usage:");
    logger.info("  show history\n");
    logger.info("Description:");
    logger.info("  Displays the chronological list of all moves played.\n");
  }

  /** Displays help information for the "show time" option. */
  public void helpTime() {
    logger.info("=== SHOW TIME ===\n");
    logger.info("Usage:");
    logger.info("  show time\n");
    logger.info("Description:");
    logger.info("  Displays the remaining time for each player.\n");
    logger.info("  Only relevant if a timed mode is active.\n");
  }

  /** Displays help information for the "show configuration" option. */
  public void helpConfig() {
    logger.info("=== SHOW CONFIGURATION ===\n");
    logger.info("Usage:");
    logger.info("  show configuration\n");
    logger.info("Description:");
    logger.info("  Displays the current game configuration.\n");
    logger.debug("  This includes all active settings such as:");
    logger.debug("    - Board size");
    logger.debug("    - Number of players");
    logger.debug("    - Game mode (e.g., Blitz)");
    logger.debug("    - Debug mode");
    logger.debug("    - Any other configurable parameters\n");
    logger.debug("This command does not modify the game state.\n");
  }

  /**
   * Prints a textual representation of the current board state including player positions and
   * walls.
   *
   * @param engine the current game engine
   */
  private void printBoard(GameEngine engine) {
    int boardSize = engine.getState().getBoard().getSize();
    Board b = engine.getState().getBoard();
    for (int i = 0; i < boardSize; i++) writer.print(((char) ('A' + i)) + " ");
    writer.println("");
    for (int i = 0; i < boardSize; i++) {
      boolean empty = true;
      for (int j = 0; j < boardSize; j++) {
        int id = 1;
        for (Player player : engine.getState().getPlayers()) {
          if (player.getPosition().equals(new Position(i, j))) {
            writer.print(id + printWall(b, i, j, false));
            empty = false;
          }
          id++;
        }
        if (empty) writer.print("_" + ((j != boardSize - 1) ? printWall(b, i, j, false) : " "));
        empty = true;
        if (j == boardSize - 1) writer.print(i + 1);
      }
      if (i == boardSize - 1) break;
      writer.println("");
      for (int j = 0; j < boardSize; j++) {
        writer.print(printWall(b, i, j, true));
      }
      writer.println("");
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
  private String printWall(Board b, int i, int j, boolean under) {
    if (under) {
      if (i >= b.getSize() - 1) return "X ";
      if (b.getGraph().hasEdge(b.id(i, j), b.id(i + 1, j))) return ". ";
      else return "X ";
    } else {
      if (j >= b.getSize() - 1) return "X";
      if (b.getGraph().hasEdge(b.id(i, j), b.id(i, j + 1))) return " ";
      else return "X";
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
    char letter = (char) ('a' + p.getY());
    int number = p.getX() + 1;
    return "" + letter + number;
  }

  /**
   * Displays the complete history of moves played during the game. Format: [Player Number] -
   * [StartPos]-[EndPos] for pawns, or [Player Number] - [Pos][Orientation] for walls.
   *
   * @param engine The game engine containing the current state and move history.
   */
  public void showHistory(GameEngine engine) {
    int nbPlayers = engine.getState().getPlayers().size();
    int count = 0;
    Move[] moves = engine.getState().getMovesPlayed();
    for (Move move : moves) {
      String strMove = "";
      if (move.isPawn()) {
        Position from = move.getFrom();
        strMove +=
            (count % nbPlayers) + 1 + " - " + posToString(from) + '-' + posToString(move.getTo());
      } else {
        Orientation orientation = move.getOrientation();
        strMove +=
            (count % nbPlayers) + 1 + " - " + posToString(move.getTo()) + orientation.toString();
      }
      count++;
      strMove += ";";
      writer.println(strMove);
    }
  }
}
