package fr.bordeaux.cli.command;

import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.core.GameEngine;
import fr.bordeaux.util.Logger;

/**
 * Command that modifies a configuration parameter of the game. The command expects an assignment in
 * the form PARAM=VALUE.
 */
public class SetCommand implements Command {

  /** Command entered by the user */
  private String[] tokens;

  /** Logger to display informations */
  private static final Logger LOGGER = Logger.getInstance();

  /** Creates a SetCommand. Currently not implemented. */
  public SetCommand() {
    this.tokens = null;
  }

  /**
   * Creates a SetCommand. Currently not implemented.
   *
   * @param s name of the command
   */
  public SetCommand(String s) {
    this(new String[] {"set", s});
  }

  /**
   * Creates a SetCommand with the provided CLI tokens.
   *
   * @param t the command tokens (e.g., ["set", "debug=true"])
   */
  public SetCommand(String[] t) {
    this.tokens = t;
  }

  /**
   * Executes the set command. Intended to update a configuration option in the game engine.
   *
   * @param engine the current game engine
   * @return the unchanged game engine
   */
  @Override
  public GameEngine execute(GameEngine engine) {
    if (tokens == null || tokens.length < 2) {
      LOGGER.info("Usage: set PARAM=VALUE");
      return engine;
    }

    String argument = tokens[1];
    if (!argument.contains("=")) {
      LOGGER.error("Invalid format. Use PARAM=VALUE (e.g., set debug=true)");
      return engine;
    }

    // Splitting the argument into key and value
    String[] parts = argument.split("=", 2);
    String key = parts[0].trim();
    String value = parts[1].trim();

    if (key.isEmpty() || value.isEmpty()) {
      LOGGER.error("Key or value cannot be empty.");
      return engine;
    }

    try {
      ConfigManager config = ConfigManager.getInstance();

      // Update the option in memory
      config.setOption(key, value);

      // Persist the change to the file (.quoridorrc)
      config.save();

      LOGGER.debug("Configuration updated: "+key+" set to "+value);
    } catch (Exception e) {
      LOGGER.error("Failed to update configuration for key: "+key, e);
    }

    return engine;
  }

  /** Displays help information about the set command, including its usage and an example. */
  @Override
  public void helpText() {
    LOGGER.info("=== SET Command ===\n");
    LOGGER.info("Usage:");
    LOGGER.info("  set PARAM=VALUE\n");
    LOGGER.info("Description:");
    LOGGER.info("  Updates a configuration parameter of the game.\n");
    LOGGER.verbose("Arguments:");
    LOGGER.verbose("  PARAM   : Name of the configuration option.");
    LOGGER.verbose("  VALUE   : New value to assign.\n");
    LOGGER.verbose("Example:");
    LOGGER.verbose("  set debug=true\n");
  }
}
