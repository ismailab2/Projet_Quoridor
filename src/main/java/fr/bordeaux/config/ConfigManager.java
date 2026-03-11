package fr.bordeaux.config;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/**
 * Manages application configuration using a singleton pattern. Configuration is stored in a file
 * located in the user's home directory.
 *
 * <p>Priority order for configuration values: 1. Default values 2. Values from the configuration
 * file (.qoridorrc) 3. Command-line arguments (highest priority)
 */
public class ConfigManager {

  private static ConfigManager INSTANCE;

  private static final String FILE =
      System.getProperty("user.home") + File.separator + ".qoridorrc";

  private final Properties props = new Properties();

  private ConfigManager() {}

  public static ConfigManager getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new ConfigManager();
    }
    return INSTANCE;
  }

  /**
   * Loads configuration.
   *
   * <p>Steps: Load default values Override with configuration file if it exists
   */
  public void load() {

    loadDefaults();

    Path path = Paths.get(FILE);

    if (Files.exists(path)) {
      try (InputStream in = Files.newInputStream(path)) {
        props.load(in);
      } catch (IOException e) {
        System.err.println("Warning: invalid .qoridorrc file. Using defaults.");
      }
    }
  }

  /** Loads default configuration values. */
  private void loadDefaults() {
    props.setProperty("verbose", "true");
    props.setProperty("blitz", "false");
    props.setProperty("timeout", "30");
  }

  /**
   * Overrides configuration using command-line arguments.
   *
   * <p>Expected format: --key=value
   */
  public void overrideWithCommandLine(String[] args) {

    for (String arg : args) {

      if (arg.startsWith("--")) {

        String option = arg.substring(2);

        String[] parts = option.split("=");

        if (parts.length == 2) {

          String key = parts[0];
          String value = parts[1];

          props.setProperty(key, value);
        }
      }
    }
  }

  /** Saves the configuration to the .qoridorrc file. */
  public void save() {

    try (OutputStream out = Files.newOutputStream(Paths.get(FILE))) {

      props.store(out, "Quoridor configuration file");

    } catch (IOException e) {

      System.err.println("Error saving configuration file.");
    }
  }

  /**
   * Returns a configuration option value.
   *
   * @param key option name
   * @param defaultValue value returned if option does not exist
   */
  public String getOption(String key, String defaultValue) {

    return props.getProperty(key, defaultValue);
  }

  /** Updates or adds a configuration option. */
  public void setOption(String key, String value) {

    props.setProperty(key, value);
  }
}
