package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;
import fr.bordeaux.i18n.I18n;
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
      LOGGER.info("Usage: save FILE");
      return engine;
    }
    try {
      LOGGER.info(I18n.get("attempt", tokens[1]));
      engine.saveGame(tokens[1]);
      LOGGER.info(I18n.get("success"));
    } catch (Exception e) {
      LOGGER.error(I18n.get("failed", tokens[1]), e);
    }
    return engine;
  }

  /** Displays help information about the save command, including its usage and example. */
  @Override
  public void helpText() {
    LOGGER.info("=== SAVE Command ===\n");
    LOGGER.info("Usage:");
    LOGGER.info("  save FILE\n");
    LOGGER.info("Description:");
    LOGGER.info("  "+ I18n.get("saveDesc"));
    LOGGER.verbose("Arguments:");
    LOGGER.verbose("  FILE   : " + I18n.get("saveFile"));
    LOGGER.verbose(I18n.get("loadEx"));
    LOGGER.verbose("  save mygame.txt\n");
  }
}
