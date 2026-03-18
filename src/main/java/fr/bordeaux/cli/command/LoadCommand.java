package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;
import fr.bordeaux.persistence.GameLoader;
import java.io.PrintStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command that loads a saved game from a file. The loaded game replaces the current game engine
 * state.
 */
public class LoadCommand implements Command {

  private String[] tokens;
  private static final Logger logger = LoggerFactory.getLogger(LoadCommand.class);
  private final PrintStream writer = System.out;

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
      writer.println("Usage: load FILE");
      return engine;
    }
    try {
      logger.info("Loading game state from file: {}", tokens[1]);
      return new GameEngine(GameLoader.loadGame(tokens[1]));
    } catch (Exception e) {
      e.printStackTrace();
      logger.error("Error while loading file {}", tokens[1], e);
    }
    return engine;
  }

  /**
   * Displays help information about the load command, including its usage, arguments, and an
   * example.
   */
  @Override
  public void helpText() {
    logger.info("=== LOAD Command ===\n");
    logger.info("Usage:");
    logger.info("  load FILE\n");
    logger.info("Description:");
    logger.info("  Loads a saved game from the specified FILE.\n");
    logger.info("Arguments:");
    logger.info("  FILE   : Path to the save file (required).\n");
    logger.info("Example:");
    logger.info("  load savegame.txt\n");
  }
}
