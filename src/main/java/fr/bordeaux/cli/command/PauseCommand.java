package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;
import fr.bordeaux.i18n.I18n;
import fr.bordeaux.util.Logger;

/** CLI command that pauses the Blitz mode game timer. */
@SuppressWarnings({"PMD.GuardLogStatement"})
public class PauseCommand implements Command {

  /** Logger to display informations */
  private static final Logger LOGGER = Logger.getInstance();

  /** Creates a PauseCommand. */
  public PauseCommand() {
    LOGGER.debug(I18n.get("pauseInstantiating"));
  }

  /**
   * Executes the pause command. Disables Blitz mode if it is currently active.
   *
   * @param engine the current game engine
   * @return the unchanged game engine
   */
  @Override
  public GameEngine execute(final GameEngine engine) {
    if (engine.isBlitz()) {
      LOGGER.debug(I18n.get("pausing"));
      engine.pause();
    } else {
      LOGGER.error(I18n.get("notBlitz"));
    }
    return engine;
  }

  /** Displays help information for the pause command. */
  @Override
  public void helpText() {
    LOGGER.info("=== " + I18n.get("pauseHeader") + " " + I18n.get("cliTitle") + " ===\n");
    LOGGER.info(I18n.get("usage"));
    LOGGER.info("  " + I18n.get("pauseUsage") + "\n");
    LOGGER.info(I18n.get("description"));
    LOGGER.info("  " + I18n.get("pauseDesc"));
    LOGGER.verbose("  " + I18n.get("pauseVerb"));
  }
}
