package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;
import fr.bordeaux.i18n.I18n;
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
      LOGGER.debug(I18n.get("noCommand"));
    } else if (tokens.length == 1) {
      LOGGER.debug(I18n.get("allCommandHelp"));
      helpCli();
    } else {
      LOGGER.debug(I18n.get("helpFor"), tokens[1]);
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
    LOGGER.info("new [ARGS] :          " + I18n.get("newArgs"));
    LOGGER.info("help [CMD] :          " + I18n.get("helpCmd"));
    LOGGER.info("                      " + I18n.get("helpCmd2"));
    LOGGER.info("help move :           " + I18n.get("helpMove"));
    LOGGER.info("quit :                " + I18n.get("helpQuit"));
    LOGGER.info("load [filename] :     " + I18n.get("helpLoad"));
    LOGGER.info("save [filename] :     " + I18n.get("helpSave"));
    LOGGER.info("pause :               " + I18n.get("helpPause"));
    LOGGER.info("hint :                " + I18n.get("helpHint"));
    LOGGER.info("undo [N] :            " + I18n.get("helpUndo"));
    LOGGER.info("redo [N] :            " + I18n.get("helpRedo"));
    LOGGER.info("show board :          " + I18n.get("helpBoard"));
    LOGGER.info("show history :        " + I18n.get("helpHistory"));
    LOGGER.info("show time :           " + I18n.get("helpTime"));
    LOGGER.info("show configurations : " + I18n.get("helpConfigs"));
    LOGGER.info("set PARAM=VALUE :     " + I18n.get("helpSet"));
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
        LOGGER.debug(I18n.get("unknownCmd") + "{0}", commandName);
        LOGGER.info("Usage : help [CMD]");
      }
    }
  }
}
