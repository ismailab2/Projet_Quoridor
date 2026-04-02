package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;
import fr.bordeaux.util.Logger;

/**
 * Command that pauses the game timer when the game is in Blitz mode. If the game is not in Blitz
 * mode, the command has no effect.
 */
public class PauseCommand implements Command {

  /** Logger to display informations */
  private static final Logger LOGGER = Logger.getInstance();

  /** Creates a PauseCommand. */
  public PauseCommand() {}

  /**
   * Executes the pause command. Disables Blitz mode if it is currently active.
   *
   * @param engine the current game engine
   * @return the unchanged game engine
   */
  @Override
  public GameEngine execute(GameEngine engine) {
    if (engine.isBlitz()) {
      LOGGER.debug("Pausing the game.");
      engine.getState().setPaused(true);
    } else LOGGER.error("Not in blitz mode, pause is doing nothing.");
    return engine;
  }

  /** Displays help information about the pause command, including its usage and behavior. */
  @Override
  public void helpText() {
    LOGGER.info("=== PAUSE Command ===\n");
    LOGGER.info("Usage:");
    LOGGER.info("  pause\n");
    LOGGER.info("Description:");
    LOGGER.info("  Pauses the game timer if the game mode is Blitz.\n");
    LOGGER.verbose("  If the game is not in Blitz mode, this command has no effect.\n");
  }
}
