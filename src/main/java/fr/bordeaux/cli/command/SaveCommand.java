package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;
import fr.bordeaux.util.Logger;

/** Command that saves the current game state to a file. */
public class SaveCommand implements Command {

  /** Command entered by the user */
  private String[] tokens;

  /** Logger to display informations */
  private static final Logger LOGGER = Logger.getInstance();

  /**
   * Creates a SaveCommand using the specified file path.
   *
   * @param path the path of the file where the game will be saved
   */
  public SaveCommand(final String path) {
    this(new String[] {"save", path});
  }

  /**
   * Creates a SaveCommand using the provided CLI tokens.
   *
   * @param tokens the tokens entered by the user
   */
  public SaveCommand(final String[] tokens) {
    this.tokens = tokens;
  }

  /**
   * Executes save command. Writes the game state to the specified file.
   *
   * @param engine the current game engine
   * @return the unchanged game engine
   */
  @Override
  public GameEngine execute(final GameEngine engine) {
    if (tokens.length < 2) {
      LOGGER.info("Usage: save FILE");
      return engine;
    }
    try {
      LOGGER.info("Attempting to save game to: {0}", tokens[1]);
      engine.saveGame(tokens[1]);
      LOGGER.info("Game saved successfully.");
    } catch (final Exception e) {
      LOGGER.error("Failed to save game at path: {0}", tokens[1]);
    }
    return engine;
  }

  /** Displays help information for the save command. */
  @Override
  public void helpText() {
    LOGGER.info("=== SAVE Command ===\n");
    LOGGER.info("Usage:");
    LOGGER.info("  save FILE\n");
    LOGGER.info("Description:");
    LOGGER.info("  Saves the current game state into the specified file FILE.\n");
    LOGGER.verbose("Arguments:");
    LOGGER.verbose("  FILE   : Destination file path (required).\n");
    LOGGER.verbose("Example:");
    LOGGER.verbose("  save mygame.txt\n");
  }
}
