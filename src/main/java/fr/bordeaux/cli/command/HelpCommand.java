package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;
import java.io.PrintStream;
import java.util.Map;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command that displays help information for the CLI. It can show a global help message or detailed
 * help for a specific command.
 */
public class HelpCommand implements Command {

  /** Command entered by the user */
  private String[] tokens;

  /** Logger to display informations */
  private static final Logger logger = LoggerFactory.getLogger(HelpCommand.class);

  /** Stream where to display texts */
  private final PrintStream writer = System.out;

  /** Reference from the CliApp command registry */
  private final Map<String, Function<String[], Command>> registry;

  /** Creates a HelpCommand without specific tokens. Used when no command arguments are provided. */
  public HelpCommand() {
    this(null, null);
  }

  /**
   * Creates a HelpCommand with the given command tokens.
   *
   * @param userTokens the tokens entered by the user in the CLI
   * @param registryCli the CLI command registry
   */
  public HelpCommand(String[] userTokens, Map<String, Function<String[], Command>> registryCli) {
    this.tokens = userTokens;
    this.registry = registryCli;
  }

  /**
   * Executes the help command. Displays the general help message or the help for a specific command
   * depending on the provided tokens.
   *
   * @param engine the current game engine
   * @return the unchanged game engine
   */
  @Override
  public GameEngine execute(GameEngine engine) {
    if (tokens == null) {
      logger.debug("Any command has been given.");
    } else if (tokens.length == 1) {
      logger.debug("Showing all commands help.");
      helpCli();
    } else {
      logger.debug("Displaying help for the command {}", tokens[1]);
      helpFetch();
    }
    return engine;
  }

  /** Displays a short help description for this command. */
  @Override
  public void helpText() {
    writer.println("Rien");
  }

  /** Displays the global help message listing all available CLI commands. */
  public void helpCli() {
    writer.println("/=== Quoridor Command Line Help ===/\n");
    writer.println("Commands :\n");
    writer.println("new [ARGS] :          Start a new game with the given args");
    writer.println("help [CMD] :          If no command is given, displays this message.");
    writer.println(
        "                      Otherwise, displays a more detailed help for a specific command.");
    writer.println("help move :           Shows you how to play a move.");
    writer.println(
        "quit :                Quit the current game. It'll ask you if you want to save it before quitting.");
    writer.println("load [filename] :     Load a game from the given file \"filename\".");
    writer.println("save [filename] :     Save the current game to a file \"filename\".");
    writer.println(
        "pause :               Pause the timer if you're in a blitz game, else it does nothing.");
    writer.println("hint :                Suggest you a move if you don't know what to play.");
    writer.println(
        "undo [N] :            Unplays the N last move played. Just once if no N given.");
    writer.println(
        "redo [N] :            Replays the N undone move played. Just once if no N given.");
    writer.println("show board :          Displays the board of the current game.");
    writer.println(
        "show history :        Displays all the moves that were played during the game. From older to the more recent.");
    writer.println("show time :           Displays the time you and your opponent(s) each remain.");
    writer.println("show configuration :  Displays the configurations set on your game.");
    writer.println("set PARAM=VALUE :     Changing the value of a given parameters PARAM to VALUE");
  }

  /* public void helpMove() {
    ;
  } */

  /**
   * Displays detailed help for a specific command based on the user input. The command is
   * determined from the provided tokens.
   */
  public void helpFetch() {
    if (this.tokens.length <= 1) {
      logger.info("Usage: help [CMD]");
      return;
    } else {
      if (registry.containsKey(tokens[1])) {
        // Temporary instance of the command to use helpText()
        Command cmd = registry.get(tokens[1]).apply(new String[] {tokens[1]});
        cmd.helpText();
      } else {
        writer.println("Unknown command : " + tokens[1]);
        logger.debug("Usage : help [CMD]");
      }
    }
  }
}
