package fr.bordeaux.cli.command;

import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.core.Board;
import fr.bordeaux.core.GameEngine;
import fr.bordeaux.core.Move;
import fr.bordeaux.core.Orientation;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;

/**
 * Command that displays various information about the current game, such as the board state,
 * configurations, history, or remaining time.
 */
public class ShowCommand implements Command {

  private String[] tokens;

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
          String[] configs = new String[] {"verbose", "blitz", "timeout"};
          for (String cf : configs) {
            System.out.println(cf + " = " + ConfigManager.getInstance().getOption(cf, null));
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
        System.out.println("The command  \"save " + tokens[1] + "\" doesn't exist...");
        break;
    }
    return engine;
  }

  /** Displays general help information for the show command. */
  @Override
  public void helpText() {
    System.out.println("=== SHOW Command ===\n");
    System.out.println("Usage:");
    System.out.println("  show board");
    System.out.println("  show history");
    System.out.println("  show time\n");
    System.out.println("Description:");
    System.out.println("  Displays information about the current game.\n");
    System.out.println("Options:");
    System.out.println("  board         : Displays the current board state.");
    System.out.println("  history       : Displays the list of moves played so far.");
    System.out.println("  configuration : Displays all the configuations set for the game.");
    System.out.println("  time          : Displays the remaining time for each player.\n");
    System.out.println(
        "If you want the help with an option, you can enter \"help show [option]\".\n");
  }

  /** Displays help information for the "show board" option. */
  public void helpBoard() {
    System.out.println("=== SHOW BOARD ===\n");
    System.out.println("Usage:");
    System.out.println("  show board\n");
    System.out.println("Description:");
    System.out.println("  Displays the current state of the board.\n");
  }

  /** Displays help information for the "show history" option. */
  public void helpHistory() {
    System.out.println("=== SHOW HISTORY ===\n");
    System.out.println("Usage:");
    System.out.println("  show history\n");
    System.out.println("Description:");
    System.out.println("  Displays the chronological list of all moves played.\n");
  }

  /** Displays help information for the "show time" option. */
  public void helpTime() {
    System.out.println("=== SHOW TIME ===\n");
    System.out.println("Usage:");
    System.out.println("  show time\n");
    System.out.println("Description:");
    System.out.println("  Displays the remaining time for each player.\n");
    System.out.println("  Only relevant if a timed mode is active.\n");
  }

  /** Displays help information for the "show configuration" option. */
  public void helpConfig() {
    System.out.println("=== SHOW CONFIGURATION ===\n");
    System.out.println("Usage:");
    System.out.println("  show configuration\n");
    System.out.println("Description:");
    System.out.println("  Displays the current game configuration.\n");
    System.out.println("  This includes all active settings such as:");
    System.out.println("    - Board size");
    System.out.println("    - Number of players");
    System.out.println("    - Game mode (e.g., Blitz)");
    System.out.println("    - Debug mode");
    System.out.println("    - Any other configurable parameters\n");
    System.out.println("This command does not modify the game state.\n");
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
    for (int i = 0; i < boardSize; i++) System.out.print(((char) ('A' + i)) + " ");
    System.out.println("");
    for (int i = 0; i < boardSize; i++) {
      boolean empty = true;
      for (int j = 0; j < boardSize; j++) {
        int id = 1;
        for (Player player : engine.getState().getPlayers()) {
          if (player.getPosition().equals(new Position(i, j))) {
            System.out.print(id + printWall(b, i, j, false));
            empty = false;
          }
          id++;
        }
        if (empty) System.out.print("_" + ((j != boardSize - 1) ? printWall(b, i, j, false) : " "));
        empty = true;
        if (j == boardSize - 1) System.out.print(i + 1);
      }
      if (i == boardSize - 1) break;
      System.out.println("");
      for (int j = 0; j < boardSize; j++) {
        System.out.print(printWall(b, i, j, true));
      }
      System.out.println("");
    }
    System.out.println("");
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

  public void showHistory(GameEngine engine) {
    int nbPlayers = engine.getState().getPlayers().size();
    int count = 0;
    Move[] moves = engine.getState().getMovesPlayed();
    for (Move move : moves) {
      String strMove = "";
      if (move.isPawn()) {
        Position from = move.getFrom();
        strMove += (count%nbPlayers)+1 + " - " + posToString(from) + '-' + posToString(move.getTo());
      } else {
        Orientation orientation = move.getOrientation();
        strMove += (count%nbPlayers)+1 + " - " + posToString(move.getTo()) + orientation.toString();
      }
      count++;
      strMove += ";";
      System.out.println(strMove);
    }
  }
}
