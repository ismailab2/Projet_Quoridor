package fr.bordeaux.cli.command;

import fr.bordeaux.ai.*;
import fr.bordeaux.core.*;
import fr.bordeaux.i18n.I18n;
import fr.bordeaux.util.Logger;
import java.io.IOException;
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
  private static final Logger LOGGER = Logger.getInstance();

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
    if (tokens.length > 5) LOGGER.error(I18n.get("newNbArgs"));

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
                  new HumanPlayer("Human", new Position(0, 4), Color.WHITE),
                  new HumanPlayer("Bob", new Position(8, 4), Color.BLACK))),
          engine.isBlitz(),
          engine.isVerbose(),
          engine.isContest(),
          engine.isDebug(),
          engine.getInitialWalls());
    else {
      int nbPlayer = tokens.length <= 2 ? 2 : Integer.parseInt(tokens[2]);
      Board board = new Board(size + 1);
      Player[] players = new Player[nbPlayer];

      for (int i = 0; i < nbPlayer; i++) {
        try {
          LOGGER.info(I18n.get("playersType", I18n.get("availType")));
          LOGGER.info(
              "- [h]uman  - [mcts] Monte Carlo ia  - [mini] Minimax ia  - [mctsnn] Monte Carlo ia "+I18n.get("nnSelect", " - [r]andom ia"));
          String typePlayer = reader.readLine(I18n.get("playersType", " >> "));
          if (typePlayer == null) throw new EndOfFileException();
          if (!typePlayer.equals("h")
              && !typePlayer.equals("mcts")
              && !typePlayer.equals("mini")
              && !typePlayer.equals("r")
              && !typePlayer.equals("mctsnn")) {
            LOGGER.error(I18n.get("wrongType"));
            i--;
            continue;
          }
          String inputName = reader.readLine(I18n.get("playersName", " >> "));
          if (inputName == null) {
            LOGGER.error(I18n.get("shouldName"));
            throw new EndOfFileException();
          }
          switch (typePlayer) {
            case "h":
              {
                players[i] = new HumanPlayer(inputName, positions[i], Color.fromId(i + 1));
                break;
              }
            case "mcts":
              {
                players[i] = new MctsPlayer(inputName, positions[i], Color.fromId(i + 1));
                break;
              }
            case "mini":
              {
                players[i] =
                    MinimaxPlayer.builder(inputName, positions[i], Color.fromId(i + 1)).build();
                break;
              }
            case "mctsnn":
              {
                players[i] =
                    new MctsPlayer(inputName, positions[i], 2000, true, Color.fromId(i + 1));
              }
            default:
              {
                players[i] = new RandomPlayer(inputName, positions[i], Color.fromId(i + 1));
                break;
              }
          }
        } catch (EndOfFileException | UserInterruptException e) {
          LOGGER.debug(I18n.get("interruptNew"));
          try {
            terminal.close();
          } catch (Exception exc) {
            LOGGER.error(I18n.get("termClose"));
          }
          return engine;
        } catch (
            IOException e) { // IOException can be caused by loading default NN for mctsnn option
          throw new RuntimeException(e);
        }
      }
      String setNbPlayers = "nb-players=" + nbPlayer;
      new SetCommand(setNbPlayers).execute(engine);
      return new GameEngine(
          new GameState(board, Arrays.stream(players).toList()),
          engine.isBlitz(),
          engine.isVerbose(),
          engine.isContest(),
          engine.isDebug(),
          engine.getInitialWalls());
    }
  }

  /**
   * Displays help information about the new command, including its usage, arguments and examples.
   */
  @Override
  public void helpText() {
    LOGGER.info("=== NEW Command ===\n");
    LOGGER.info("Usage:");
    LOGGER.info("  new");
    LOGGER.info("  new [boardSize] [nbPlayer]\n");
    LOGGER.info("Description:");
    LOGGER.info("  "+I18n.get("newDesc"));
    LOGGER.info("Arguments:");
    LOGGER.info("  boardSize    : "+I18n.get("newBoardSize"));
    LOGGER.info("  playerCount  : "+I18n.get("newNbPlayers"));
    LOGGER.info(I18n.get("newHelp"));
    LOGGER.verbose(I18n.get("loadEx"));
    LOGGER.verbose("  new       -> 2 "+I18n.get("newEx", "9x9"));
    LOGGER.verbose("  new 5     -> 2 "+I18n.get("newEx", "5x5"));
    LOGGER.verbose("  new 7 4   -> 4 "+I18n.get("newEx", "7x7"));
  }
}
