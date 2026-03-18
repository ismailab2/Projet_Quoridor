package fr.bordeaux.cli.command;

import fr.bordeaux.ai.*;
import fr.bordeaux.core.*;
import java.util.Arrays;
import java.util.List;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command that starts a new game from the CLI. It allows configuring the board size and the number
 * of players, and interactively asks for the type and name of each player.
 */
public class NewCommand implements Command {

  /** Command entered by the user */
  private String[] tokens;

  /** The JLine terminal abstraction used to handle system-level input, output, and signals. */
  private Terminal terminal;

  /**
   * The line reader responsible for text editing, history management, and tab-completion
   * interaction.
   */
  private LineReader reader;

  /** Logger to display informations */
  private static final Logger logger = LoggerFactory.getLogger(NewCommand.class);

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
    if (tokens.length > 5) logger.error("wrong amount of arguments...");

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
          logger.info("Player's type available :");
          logger.info(
              "- [h]uman  - [mcts] Monte Carlo ia  - [mini] Minimax ia  - [nn] NeuralNetwork ia  - [r]andom ia");
          String typePlayer = reader.readLine("Player's type >> ");
          if (typePlayer == null) throw new EndOfFileException();
          if (!typePlayer.equals("h")
              && !typePlayer.equals("mcts")
              && !typePlayer.equals("mini")
              && !typePlayer.equals("nn")
              && !typePlayer.equals("r")) {
            logger.error("Wrong type, please enter h / mcts / mini / nn / r");
            i--;
            continue;
          }
          String inputName = reader.readLine("Player's name >> ");
          if (inputName == null) {
            logger.error("Should give a name to the player...");
            throw new EndOfFileException();
          }
          switch (typePlayer) {
            case "h":
              {
                players[i] = new HumanPlayer(inputName, positions[i]);
                break;
              }
            case "mcts":
              {
                players[i] = new MCTSPlayer(inputName, positions[i]);
                break;
              }
            case "mini":
              {
                players[i] = MinimaxPlayer.builder(inputName, positions[i]).build();
                break;
              }
            case "nn":
              {
                players[i] = new NeuralNetPlayer(inputName, positions[i]);
                break;
              }
            default:
              {
                players[i] = new RandomPlayer(inputName, positions[i]);
                break;
              }
          }
        } catch (EndOfFileException | UserInterruptException e) {
          logger.debug("You choose to interrupt the interaction.");
          try {
            terminal.close();
          } catch (Exception exc) {
            logger.error("Terminal closed.");
          } finally {
            return engine;
          }
        }
      }
      String setNbPlayers = "nb-players=" + nbPlayer;
      new SetCommand(setNbPlayers).execute(engine);
      return new GameEngine(new GameState(board, Arrays.stream(players).toList()));
    }
  }

  /**
   * Displays help information about the new command, including its usage, arguments and examples.
   */
  @Override
  public void helpText() {
    logger.info("=== NEW Command ===\n");
    logger.info("Usage:");
    logger.info("  new");
    logger.info("  new [boardSize] [nbPlayer]\n");
    logger.info("Description:");
    logger.info("  Starts a new game.\n");
    logger.info("Arguments (optional):");
    logger.info("  boardSize    : An odd integer greater than or equal to 3.");
    logger.info("  playerCount  : Must be either 2 or 4.\n");
    logger.info("If no arguments are provided, default settings are used.\n");
    logger.debug("Examples:");
    logger.debug("  new       -> 9x9 board with 2 players");
    logger.debug("  new 5     -> 5x5 board with 2 players");
    logger.debug("  new 7 4   -> 7x7 board with 4 players\n");
  }
}
