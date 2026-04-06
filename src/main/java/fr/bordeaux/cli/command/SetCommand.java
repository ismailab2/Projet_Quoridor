package fr.bordeaux.cli.command;

import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.core.GameEngine;
import fr.bordeaux.util.Logger;

/** CLI command that modifies a game configuration parameter. */
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
   * @param setting name of the command
   */
  public SetCommand(final String setting) {
    this(new String[] {"set", setting});
  }

  /**
   * Creates a SetCommand with the provided CLI tokens.
   *
   * @param tokens the command tokens (e.g., ["set", "debug=true"])
   */
  public SetCommand(final String[] tokens) {
    this.tokens = tokens;
  }

  /**
   * Executes set command. Updates a configuration option in the engine.
   *
   * @param engine the current game engine
   * @return the unchanged game engine
   */
  @Override
  public GameEngine execute(final GameEngine engine) {
    if (tokens == null || tokens.length < 2) {
      LOGGER.info("Usage: set PARAM=VALUE");
      return engine;
    }

    final String argument = tokens[1];
    if (!argument.contains("=")) {
      LOGGER.error("Invalid format. Use PARAM=VALUE (e.g., set debug=true)");
      return engine;
    }

    // Splitting the argument into key and value
    final String[] parts = argument.split("=", 2);
    final String key = parts[0].trim();
    final String value = parts[1].trim();

    if (key.isEmpty() || value.isEmpty()) {
      LOGGER.error("Key or value cannot be empty.");
      return engine;
    }

    try {
      final ConfigManager config = ConfigManager.getInstance();

      // Update the option in memory
      config.setOption(key, value);

      // Update the engine in-memory state
      switch (key) {
        case "blitz":
          engine.setBlitz(Boolean.parseBoolean(value));
          break;
        case "verbose":
          engine.setVerbose(Boolean.parseBoolean(value));
          break;
        case "contest":
          engine.setContest(Boolean.parseBoolean(value));
          break;
        case "debug":
          engine.setDebug(Boolean.parseBoolean(value));
          break;
        case "walls":
        case "wall":
        case "nb-walls":
          engine.setInitialWalls(Integer.parseInt(value));
          break;
        default:
          break;
      }

      // Persist the change to the file (.quoridorrc)
      config.save();

      LOGGER.debug("Configuration updated: {0} set to {1}", key, value);
    } catch (final Exception e) {
      LOGGER.error("Failed to update configuration for key: {0}", key);
    }

    return engine;
  }

  /** Displays help information for the set command. */
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
