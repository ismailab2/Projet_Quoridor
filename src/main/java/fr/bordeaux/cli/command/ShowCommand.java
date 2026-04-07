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
          List<Player> players = engine.getState().getPlayers();
          for (Player player : players) {
            if (player.equals(engine.getState().getCurrentPlayer())) {
              writer.println(
                  I18n.get("timeLeft") + TimeConvertor.formatTime(player.getRemainingTime()));
            } else {
              writer.println(
                  I18n.get("timeFor", 
                  player.getName(), " ---> ",
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
    LOGGER.info("  "+I18n.get("showDesc"));
    LOGGER.verbose("  "+I18n.get("showVerb"));
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
    LOGGER.verbose("    "+ I18n.get("showCfgs"));
    LOGGER.verbose(I18n.get("showCfgsMore"));
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
        for (Player player : engine.getState().getPlayers()) {
          if (player.getPosition().equals(new Position(i, j))) {
            writer.print(player.getColor().getId() + printWall(b, i, j, false));
            empty = false;
          }
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
    writer.println(I18n.get("oldest"));
    List<Player> players = engine.getState().getPlayers();
    int nbPlayers = engine.getState().getPlayers().size();
    int count = 0;
    Move[] moves = engine.getState().getMovesPlayed();
    for (Move move : moves) {
      String strMove = "";
      if (move.isPawn()) {
        Position from = move.getFrom();
        strMove +=
            "Player "
                + players.get(count % nbPlayers).getName()
                + " - "
                + posToString(from)
                + '-'
                + posToString(move.getTo());
      } else {
        Orientation orientation = move.getOrientation();
        strMove +=
            "Player "
                + players.get(count % nbPlayers).getName()
                + " - "
                + posToString(move.getTo())
                + orientation.toString();
      }
      count++;
      strMove += ";";
      writer.println(strMove);
    }
    writer.println(I18n.get("newest"));
  }
}
