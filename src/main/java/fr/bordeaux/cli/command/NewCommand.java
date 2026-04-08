package fr.bordeaux.cli.command;

import fr.bordeaux.ai.base.*;
import fr.bordeaux.ai.mcts.*;
import fr.bordeaux.ai.minimax.*;
import fr.bordeaux.ai.random.*;
import fr.bordeaux.core.*;
import fr.bordeaux.util.Logger;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;

/**
 * CLI command that starts a new game, configuring board size and players. of players, and
 * interactively asks for the type and name of each player.
 */
public class NewCommand implements Command {

  /** Command entered by the user */
  private String[] tokens;

  /** JLine terminal for system-level input and output. */
  private Terminal terminal;

  /** Line reader for text editing and tab-completion. interaction. */
  private LineReader reader;

  /** Logger to display informations */
  private static final Logger LOGGER = Logger.getInstance();

  /**
   * Creates a NewCommand with the given CLI tokens, terminal and line reader.
   *
   * @param tokens the tokens entered by the user
   * @param term the terminal used for input/output
   * @param reader the line reader used to interact with the user
   */
  public NewCommand(final String[] tokens, final Terminal term, final LineReader reader) {
    this.tokens = tokens;
    this.terminal = term;
    this.reader = reader;
  }

  /**
   * Creates a NewCommand with default tokens and the provided terminal and reader.
   *
   * @param terminal the terminal used for input/output
   * @param reader the line reader used to read user input
   */
  public NewCommand(final Terminal terminal, final LineReader reader) {
    this(new String[] {"9", "2"}, terminal, reader);
  }

  /** Creates an empty NewCommand. Used mainly when only help information is needed. */
  public NewCommand() {
    this(null, null, null);
  }

  /**
   * Executes new command, initializing the game engine with board and players. Prompts for player
   * types and names if needed. interactively.
   *
   * @param engine the current game engine
   * @return a new GameEngine instance representing the new game
   */
  @Override
  public GameEngine execute(final GameEngine engine) {
    GameEngine result = engine;
    if (tokens.length > 5) {
      LOGGER.error("wrong amount of arguments...");
    }

    final int boardSizeInput = tokens.length >= 2 ? Integer.parseInt(tokens[1]) : 9;
    final int size = boardSizeInput - 1;
    final Position[] positions = {
      new Position(0, size / 2), new Position(size, size / 2),
      new Position(size / 2, 0), new Position(size / 2, size)
    };

    if (tokens.length == 1) {
      result =
          new GameEngine(
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
    } else {
      final int nbPlayer = tokens.length <= 2 ? 2 : Integer.parseInt(tokens[2]);
      final Board board = new Board(size + 1);
      final Player[] players = new Player[nbPlayer];

      boolean interrupted = false;
      for (int index = 0; index < nbPlayer; index++) {
        try {
          LOGGER.info("Player's type available :");
          LOGGER.info(
              "- [h]uman  - [mcts] Monte Carlo ia  - [mini] Minimax ia"
                  + "  - [mctsnn] Monte Carlo ia with NeuralNetwork selector  - [r]andom ia");
          final String typePlayer = reader.readLine("Player's type >> ");
          if (typePlayer == null) {
            throw new EndOfFileException();
          }
          if (!"h".equals(typePlayer)
              && !"mcts".equals(typePlayer)
              && !"mini".equals(typePlayer)
              && !"r".equals(typePlayer)
              && !"mctsnn".equals(typePlayer)) {
            LOGGER.error("Wrong type, please enter h / mcts / mini / mctsnn / r");
            index--;
            continue;
          }
          final String inputName = reader.readLine("Player's name >> ");
          if (inputName == null) {
            LOGGER.error("Should give a name to the player...");
            throw new EndOfFileException();
          }
          switch (typePlayer) {
            case "h":
              {
                players[index] =
                    new HumanPlayer(inputName, positions[index], Color.fromId(index + 1));
                break;
              }
            case "mcts":
              {
                players[index] =
                    new MctsPlayer(inputName, positions[index], Color.fromId(index + 1));
                break;
              }
            case "mini":
              {
                players[index] =
                    MinimaxPlayer.builder(inputName, positions[index], Color.fromId(index + 1))
                        .build();
                break;
              }
            case "mctsnn":
              {
                players[index] =
                    new MctsPlayer(
                        inputName, positions[index], 2000, true, Color.fromId(index + 1));
                break;
              }
            default:
              {
                players[index] =
                    new RandomPlayer(inputName, positions[index], Color.fromId(index + 1));
                break;
              }
          }
        } catch (EndOfFileException | UserInterruptException e) {
          LOGGER.debug("You choose to interrupt the interaction.");
          try {
            terminal.close();
          } catch (final Exception exc) {
            LOGGER.error("Terminal closed.");
          }
          interrupted = true;
          break;
        } catch (final IOException e) {
          throw new java.io.UncheckedIOException(e);
        }
      }

      if (!interrupted) {
        final String setNbPlayers = "nb-players=" + nbPlayer;
        new SetCommand(setNbPlayers).execute(engine);
        result =
            new GameEngine(
                new GameState(board, Arrays.stream(players).toList()),
                engine.isBlitz(),
                engine.isVerbose(),
                engine.isContest(),
                engine.isDebug(),
                engine.getInitialWalls());
      }
    }
    return result;
  }

  /** Displays help information for the new command. */
  @Override
  public void helpText() {
    LOGGER.info("=== NEW Command ===\n");
    LOGGER.info("Usage:");
    LOGGER.info("  new");
    LOGGER.info("  new [boardSize] [nbPlayer]\n");
    LOGGER.info("Description:");
    LOGGER.info("  Starts a new game.\n");
    LOGGER.info("Arguments (optional):");
    LOGGER.info("  boardSize    : An odd integer greater than or equal to 3.");
    LOGGER.info("  playerCount  : Must be either 2 or 4.\n");
    LOGGER.info("If no arguments are provided, default settings are used.\n");
    LOGGER.verbose("Examples:");
    LOGGER.verbose("  new       -> 9x9 board with 2 players");
    LOGGER.verbose("  new 5     -> 5x5 board with 2 players");
    LOGGER.verbose("  new 7 4   -> 7x7 board with 4 players\n");
  }
}
