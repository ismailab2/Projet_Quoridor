package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;
import fr.bordeaux.persistence.GameSaver;
import java.io.PrintStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Command that saves the current game state to a file. */
public class SaveCommand implements Command {

  /** Command entered by the user */
  private String[] tokens;

  /** Logger to display informations */
  private static final Logger logger = LoggerFactory.getLogger(SaveCommand.class);

  /** Stream where to display texts */
  private final PrintStream writer = System.out;

  /**
   * Creates a SaveCommand using the specified file path.
   *
   * @param p the path of the file where the game will be saved
   */
  public SaveCommand(String p) {
    this(new String[] {"save", p});
  }

  /**
   * Creates a SaveCommand using the provided CLI tokens.
   *
   * @param t the tokens entered by the user
   */
  public SaveCommand(String[] t) {
    this.tokens = t;
  }

  /**
   * Executes the save command. Writes the current game state to the specified file.
   *
   * @param engine the current game engine
   * @return the unchanged game engine
   */
  @Override
  public GameEngine execute(GameEngine engine) {
    if (tokens.length < 2) {
      logger.info("Usage: save FILE");
      return engine;
    }
    try {
      logger.info("Attempting to save game to: {}", tokens[1]);
      GameSaver.saveGame(engine.getState(), tokens[1]);
      logger.info("Game saved successfully.");
    } catch (Exception e) {
      logger.error("Failed to save game at path: {}", tokens[1], e);
    }
    return engine;
  }

  /** Displays help information about the save command, including its usage and example. */
  @Override
  public void helpText() {
    logger.info("=== SAVE Command ===\n");
    logger.info("Usage:");
    logger.info("  save FILE\n");
    logger.info("Description:");
    logger.info("  Saves the current game state into the specified file FILE.\n");
    logger.debug("Arguments:");
    logger.debug("  FILE   : Destination file path (required).\n");
    logger.debug("Example:");
    logger.debug("  save mygame.txt\n");
  }
}
