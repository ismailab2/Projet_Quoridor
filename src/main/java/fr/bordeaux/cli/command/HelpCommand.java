package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;
import fr.bordeaux.util.Logger;
import java.util.Map;
import java.util.function.Function;

/** CLI command that displays global or detailed help information. help for a specific command. */
public class HelpCommand implements Command {

  /** Command entered by the user */
  private String[] tokens;

  /** Logger to display informations */
  private static final Logger LOGGER = Logger.getInstance();

  /** Reference from the CliApp command registry */
  private final Map<String, Function<String[], Command>> registry;

  /** Creates a HelpCommand without specific tokens. */
  public HelpCommand() {
    this(null, null);
  }

  /**
   * Creates a HelpCommand with the given command tokens and the command registry.
   *
   * @param userTokens tokens entered by the user in the CLI
   * @param registryCli mapping of command names to factory functions
   */
  public HelpCommand(
      final String[] userTokens, final Map<String, Function<String[], Command>> registryCli) {
    this.tokens = userTokens;
    this.registry = registryCli;
  }

  /**
   * Executes help command. Displays general or specific help message. depending on the provided
   * tokens.
   *
   * @param engine the current game engine
   * @return the unchanged game engine
   */
  @Override
  public GameEngine execute(final GameEngine engine) {
    if (tokens == null) {
      LOGGER.debug("Any command has been given.");
    } else if (tokens.length == 1) {
      LOGGER.debug("Showing all commands help.");
      helpCli();
    } else {
      LOGGER.debug("Displaying help for the command {0}", tokens[1]);
      helpFetch();
    }
    return engine;
  }

  /** Displays a short help description for this command. */
  @Override
  public void helpText() {
    LOGGER.info("Rien");
  }

  /** Displays the global help message listing all available CLI commands. */
  public void helpCli() {
    LOGGER.info("/=== Quoridor Command Line Help ===/\n");
    LOGGER.info("Commands :\n");
    LOGGER.info("new [ARGS] :          Start a new game with the given args");
    LOGGER.info("help [CMD] :          If no command is given, displays this message.");
    LOGGER.info(
        "                      Otherwise, displays a more detailed help for a specific command.");
    LOGGER.info("help move :           Shows you how to play a move.");
    LOGGER.info(
        "quit :                Quit the current game. It'll ask you if you want to save it before quitting.");
    LOGGER.info("load [filename] :     Load a game from the given file \"filename\".");
    LOGGER.info("save [filename] :     Save the current game to a file \"filename\".");
    LOGGER.info(
        "pause :               Pause the timer if you're in a blitz game, else it does nothing.");
    LOGGER.info("hint :                Suggest you a move if you don't know what to play.");
    LOGGER.info("undo [N] :            Unplays the N last move played. Just once if no N given.");
    LOGGER.info("redo [N] :            Replays the N undone move played. Just once if no N given.");
    LOGGER.info("show board :          Displays the board of the current game.");
    LOGGER.info(
        "show history :        Displays all the moves that were played during the game. From older to the more recent.");
    LOGGER.info("show time :           Displays the time you and your opponent(s) each remain.");
    LOGGER.info("show configurations : Displays the configurations set on your game.");
    LOGGER.info("set PARAM=VALUE :     Changing the value of a given parameters PARAM to VALUE");
  }

  /**
   * Displays detailed help for a specific command based on user input. determined from the provided
   * tokens.
   */
  public void helpFetch() {
    if (this.tokens.length <= 1) {
      LOGGER.info("Usage: help [CMD]");
    } else {
      final String commandName = tokens[1];
      if (registry.containsKey(commandName)) {
        // Temporary instance of the command to use helpText()
        final Command cmd = registry.get(commandName).apply(new String[] {commandName});
        cmd.helpText();
      } else {
        LOGGER.debug("Unknown command : {0}", commandName);
        LOGGER.info("Usage : help [CMD]");
      }
    }
  }
}
