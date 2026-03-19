package fr.bordeaux.cli.command;

import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.core.GameEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command that modifies a configuration parameter of the game. The command expects an assignment in
 * the form PARAM=VALUE.
 */
public class SetCommand implements Command {

  /** Command entered by the user */
  private String[] tokens;

  /** Logger to display informations */
  private static final Logger logger = LoggerFactory.getLogger(SetCommand.class);

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
      logger.info("? Usage: set PARAM=VALUE");
      return engine;
    }

    String argument = tokens[1];
    if (!argument.contains("=")) {
      logger.error("Invalid format. Use PARAM=VALUE (e.g., set debug=true)");
      return engine;
    }

    // Splitting the argument into key and value
    String[] parts = argument.split("=", 2);
    String key = parts[0].trim();
    String value = parts[1].trim();

    if (key.isEmpty() || value.isEmpty()) {
      logger.error("Key or value cannot be empty.");
      return engine;
    }

    try {
      ConfigManager config = ConfigManager.getInstance();

      // Update the option in memory
      config.setOption(key, value);

      // Persist the change to the file (.quoridorrc)
      config.save();

      logger.debug("Configuration updated: {} set to {}", key, value);
    } catch (Exception e) {
      logger.error("Failed to update configuration for key: {}", key, e);
    }

    return engine;
  }

  /** Displays help information about the set command, including its usage and an example. */
  @Override
  public void helpText() {
    logger.info("=== SET Command ===\n");
    logger.info("Usage:");
    logger.info("  set PARAM=VALUE\n");
    logger.info("Description:");
    logger.info("  Updates a configuration parameter of the game.\n");
    logger.debug("Arguments:");
    logger.debug("  PARAM   : Name of the configuration option.");
    logger.debug("  VALUE   : New value to assign.\n");
    logger.debug("Example:");
    logger.debug("  set debug=true\n");
  }
}
