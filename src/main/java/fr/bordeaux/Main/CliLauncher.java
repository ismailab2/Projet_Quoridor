package fr.bordeaux.Main;

import fr.bordeaux.cli.CliApp;
import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.core.Board;
import fr.bordeaux.core.GameEngine;
import fr.bordeaux.core.GameState;
import fr.bordeaux.util.Logger;

/**
 * This launcher initializes the Quoridor game board, the game state, and the game engine. It then
 * starts the CLI interface for user interaction.
 *
 * <p>The CLI handles user commands, moves, and displays the game board and state in the console.
 */
public class CliLauncher implements AppLauncher {

  /** Default board size for Quoridor (9x9) */
  public static final int BOARD_SIZE = 9;

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
    Board board = new Board(BOARD_SIZE);
    GameState state = new GameState(board, PlayerFactory.createPlayers(config));

    final boolean isBlitz = config.getBoolean("blitz", false);

    if (isBlitz) {
      final int timeoutMinutes = config.getInt("timeout", 30);
      final long timeoutMillis = timeoutMinutes * 60 * 1000L;
      state.setLimitTimePlayer(timeoutMillis);
    }

    GameEngine engine =
        new GameEngine(
            state,
            isBlitz,
            config.getBoolean("verbose", false),
            config.getBoolean("contest", false),
            config.getBoolean("debug", false),
            Integer.parseInt(config.getOption("nb-walls", "10")));

    try {
      new CliApp(engine).run();
    } catch (Exception e) {
      Logger.getInstance().error("CLI Error: " + e.getMessage());
    }
  }
}
