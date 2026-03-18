package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;

/**
 * Command that pauses the game timer when the game is in Blitz mode. If the game is not in Blitz
 * mode, the command has no effect.
 */
public class PauseCommand implements Command {

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
    if (engine.getState().isBlitz()) engine.getState().setPaused(true);
    else System.out.println("Not in blitz mode, pause is doing nothing.");
    return engine;
  }

  /** Displays help information about the pause command, including its usage and behavior. */
  @Override
  public void helpText() {
    System.out.println("=== PAUSE Command ===\n");
    System.out.println("Usage:");
    System.out.println("  pause\n");
    System.out.println("Description:");
    System.out.println("  Pauses the game timer if the game mode is Blitz.\n");
    System.out.println("  If the game is not in Blitz mode, this command has no effect.\n");
  }
}
