package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;
import fr.bordeaux.i18n.I18n;
import fr.bordeaux.util.Logger;

/**
 * Command that loads a saved game from a file. The loaded game replaces the current game engine
 * state.
 */
public class LoadCommand implements Command {

  /** Command entered by the user */
  private String[] tokens;

  /** Logger to display informations */
  private static final Logger LOGGER = Logger.getInstance();

  /**
   * Creates a LoadCommand with the provided command tokens.
   *
   * @param t the tokens entered by the user in the CLI
   */
  public LoadCommand(String[] t) {
    this.tokens = t;
  }

  /**
   * Executes the load command. Attempts to load a saved game from the file specified in the tokens.
   * If loading succeeds, a new GameEngine is created with the loaded state.
   *
   * @param engine the current game engine
   * @return a new GameEngine containing the loaded game state, or the current engine if loading
   *     fails
   */
  @Override
  public GameEngine execute(GameEngine engine) {
    if (tokens.length < 2) {
      LOGGER.info("Usage: load FILE");
      return engine;
    }
    try {
      LOGGER.info(I18n.get("loading", tokens[1]));
      engine.loadGame(tokens[1]);
    } catch (Exception e) {
      LOGGER.error(I18n.get("loadError", tokens[1]), e);
    }
    return engine;
  }

  /**
   * Displays help information about the load command, including its usage, arguments, and an
   * example.
   */
  @Override
  public void helpText() {
    LOGGER.info("=== LOAD Command ===\n");
    LOGGER.info("Usage:");
    LOGGER.info("  load FILE\n");
    LOGGER.info("Description:");
    LOGGER.info("  "+I18n.get("loadHelp"));
    LOGGER.verbose("Arguments:");
    LOGGER.verbose("  "+ I18n.get("loadArg"));
    LOGGER.verbose(I18n.get("loadEx"));
    LOGGER.verbose("  load savegame.txt\n");
  }
}
