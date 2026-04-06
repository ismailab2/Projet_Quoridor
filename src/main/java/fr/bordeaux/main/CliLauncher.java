package fr.bordeaux.main;

import fr.bordeaux.cli.CliApp;
import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.core.*;
import fr.bordeaux.util.Logger;

/**
 * This launcher initializes the Quoridor game board, the game state, and the game engine. It then
 * starts the CLI interface for user interaction.
 *
 * <p>The CLI handles user commands, moves, and displays the game board and state in the console.
 */
public class CliLauncher implements AppLauncher {

  /** Default board size for Quoridor (9x9) */
  private static final int DEFAULT_BOARD_SIZE = 9;

  /** CliLaucher */
  public CliLauncher() {}

  /**
   * Initializes the board, game state, and engine, then starts the CLI interface. Exceptions are
   * logged using the Logger utility.
   *
   * @param args command-line arguments (not currently used)
   * @param config configuration manager instance for loading settings
   */
  @Override
  public void launch(String[] args, ConfigManager config) {
    try {
      GameEngine engine = GameEngineFactory.createGameEngine(config);
      new CliApp(engine).run();
    } catch (Exception e) {
      if (Logger.getInstance().isErrorEnabled()) {
        Logger.getInstance().error("CLI Error: " + e.getMessage());
      }
    }
  }
}
