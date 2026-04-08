package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;
import fr.bordeaux.i18n.I18n;
import fr.bordeaux.util.Logger;

/** CLI command that loads a saved game from a file. state. */
public class LoadCommand implements Command {

  /** Command entered by the user */
  private String[] tokens;

  /** Logger to display informations */
  private static final Logger LOGGER = Logger.getInstance();

  /**
   * Creates a LoadCommand with the provided command tokens.
   *
   * @param tokens the tokens entered by the user in the CLI
   */
  public LoadCommand(final String[] tokens) {
    this.tokens = tokens;
  }

  /**
   * Executes load command and loads game from file. If loading succeeds, a new GameEngine is
   * created with the loaded state.
   *
   * @param engine the current game engine
   * @return Loaded GameEngine or the current one if failed. fails
   */
  @Override
  public GameEngine execute(final GameEngine engine) {
    if (tokens.length < 2) {
      LOGGER.info("Usage: load FILE");
      return engine;
    }
    try {
      LOGGER.info(I18n.get("loading")+" {0}", tokens[1]);
      engine.loadGame(tokens[1]);
    } catch (final Exception e) {
      LOGGER.error(I18n.get("loadError")+" {0}", tokens[1]);
    }
    return engine;
  }

  /** Displays help information for the load command. example. */
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
