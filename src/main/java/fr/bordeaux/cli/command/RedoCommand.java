package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;
import fr.bordeaux.i18n.I18n;
import fr.bordeaux.util.Logger;

/** CLI command that replays previously undone moves. */
@SuppressWarnings({"PMD.GuardLogStatement"})
public class RedoCommand implements Command {
  /** Command tokens. */
  private String[] tokens;

  /** Logger to trace redo actions. */
  private static final Logger LOGGER = Logger.getInstance();

  /** Creates a RedoCommand that replays one move by default. */
  public RedoCommand() {
    this(new String[] {"redo", "1"});
  }

  /**
   * Creates a RedoCommand using the provided CLI tokens.
   *
   * @param tokens the tokens entered by the user
   */
  public RedoCommand(final String[] tokens) {
    this.tokens = tokens;
  }

  /**
   * Executes redo command. Replays the specified number of moves.
   *
   * @param engine the current game engine
   * @return the updated game engine after redoing moves
   */
  @Override
  public GameEngine execute(final GameEngine engine) {
    if (tokens.length >= 3) {
      LOGGER.info(I18n.get("usage") + ": redo [N]");
      return engine;
    }
    final int count = (tokens.length > 1) ? Integer.parseInt(tokens[1]) : 1;
    LOGGER.info(I18n.get("redoing") + count + I18n.get("redoMove"));

    for (int i = 0; i < count; i++) {
      engine.getState().redo();
    }
    return engine;
  }

  /** Displays help information for the redo command. */
  @Override
  public void helpText() {
    LOGGER.info("=== " + I18n.get("redoHeader") + " " + I18n.get("cliTitle") + " ===\n");
    LOGGER.info(I18n.get("usage") + ":");
    LOGGER.info("  " + I18n.get("redoUsageOne"));
    LOGGER.info("  " + I18n.get("redoUsageNb") + "\n");
    LOGGER.info(I18n.get("description") + ":");
    LOGGER.info("  " + I18n.get("redoDesc"));
    LOGGER.verbose("  " + I18n.get("redoOne"));
    LOGGER.verbose("  " + I18n.get("redoNb"));
    LOGGER.verbose(I18n.get("loadEx"));
    LOGGER.verbose("  redo      -> " + I18n.get("redoExOne"));
    LOGGER.verbose("  redo 3    -> " + I18n.get("redoExNb"));
  }
}
