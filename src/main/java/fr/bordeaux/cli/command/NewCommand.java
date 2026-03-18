package fr.bordeaux.cli.command;

import fr.bordeaux.ai.*;
import fr.bordeaux.core.*;
import java.util.Arrays;
import java.util.List;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;

/**
 * Command that starts a new game from the CLI. It allows configuring the board size and the number
 * of players, and interactively asks for the type and name of each player.
 */
public class NewCommand implements Command {

  private String[] tokens;
  private Terminal terminal;
  private LineReader reader;

  /**
   * Creates a NewCommand with the given CLI tokens, terminal and line reader.
   *
   * @param t the tokens entered by the user
   * @param term the terminal used for input/output
   * @param r the line reader used to interact with the user
   */
  public NewCommand(String[] t, Terminal term, LineReader r) {
    this.tokens = t;
    this.terminal = term;
    this.reader = r;
  }

  /**
   * Creates a NewCommand with default tokens and the provided terminal and reader.
   *
   * @param t the terminal used for input/output
   * @param r the line reader used to read user input
   */
  public NewCommand(Terminal t, LineReader r) {
    this(new String[] {"9", "2"}, t, r);
  }

  /** Creates an empty NewCommand. Used mainly when only help information is needed. */
  public NewCommand() {
    this(null, null, null);
  }

  /**
   * Executes the new command. Initializes a new game engine with a board and players according to
   * the provided tokens. If necessary, the user is prompted to choose player types and names
   * interactively.
   *
   * @param engine the current game engine
   * @return a new GameEngine instance representing the new game
   */
  @Override
  public GameEngine execute(GameEngine engine) {
    if (tokens.length > 5) {
      System.out.println("wrong amount of arguments...");
    }
    int size = (tokens.length >= 2 ? Integer.parseInt(tokens[1]) : 9) - 1;
    Position[] positions = {
      new Position(0, size / 2), new Position(size, size / 2),
      new Position(size / 2, 0), new Position(size / 2, size)
    };
    if (tokens.length == 1)
      return new GameEngine(
          new GameState(
              new Board(size + 1),
              List.of(
                  new HumanPlayer("Human", new Position(0, 4)),
                  new HumanPlayer("Bob", new Position(8, 4)))));
    else {
      int nbPlayer = tokens.length <= 2 ? 2 : Integer.parseInt(tokens[2]);
      Board board = new Board(size + 1);
      Player[] players = new Player[nbPlayer];

      for (int i = 0; i < nbPlayer; i++) {
        try {
          System.out.println("Player's type available :");
          System.out.println(
              "- [h]uman  - [mcts] Monte Carlo ia  - [mini] Minimax ia  - [r]andom ia");
          String typePlayer = reader.readLine("Player's type >> ");
          if (typePlayer == null) throw new EndOfFileException();
          if (!typePlayer.equals("h")
              && !typePlayer.equals("mcts")
              && !typePlayer.equals("mini")
              && !typePlayer.equals("r")) {
            System.out.println("Wrong type, please enter h / mcts / mini / r");
            i--;
            continue;
          }
          String inputName = reader.readLine("Player's name >> ");
          if (inputName == null) throw new EndOfFileException();
          switch (typePlayer) {
            case "h":
              {
                players[i] = new HumanPlayer(inputName, positions[i]);
                break;
              }
            case "mcts":
              {
                players[i] = new MctsPlayer(inputName, positions[i]);
                break;
              }
            case "mini":
              {
                players[i] = MinimaxPlayer.builder(inputName, positions[i]).build();
                break;
              }
            default:
              {
                players[i] = new RandomPlayer(inputName, positions[i]);
                break;
              }
          }
        } catch (EndOfFileException | UserInterruptException e) {
          System.err.println("Tout va bien, je m'arrête !\n");
          try {
            terminal.close();
          } catch (Exception exc) {
            System.err.println("Terminal fermé");
            return engine;
          }
        }
      }
      return new GameEngine(new GameState(board, Arrays.stream(players).toList()));
    }
  }

  /**
   * Displays help information about the new command, including its usage, arguments and examples.
   */
  @Override
  public void helpText() {
    System.out.println("=== NEW Command ===\n");
    System.out.println("Usage:");
    System.out.println("  new");
    System.out.println("  new [boardSize] [nbPlayer]\n");
    System.out.println("Description:");
    System.out.println("  Starts a new game.\n");
    System.out.println("Arguments (optional):");
    System.out.println("  boardSize    : An odd integer greater than or equal to 3.");
    System.out.println("  playerCount  : Must be either 2 or 4.\n");
    System.out.println("If no arguments are provided, default settings are used.\n");
    System.out.println("Examples:");
    System.out.println("  new       -> 9x9 board with 2 players");
    System.out.println("  new 5     -> 5x5 board with 2 players");
    System.out.println("  new 7 4   -> 7x7 board with 4 players\n");
  }
}
