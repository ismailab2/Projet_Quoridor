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

  /** Singleton instance of ConfigManager */
  private static ConfigManager INSTANCE;

  /** Path to the configuration file in the user's home directory */
  private static final String FILE =
      System.getProperty("user.home") + File.separator + ".quoridorrc";

  /** Stores all configuration properties and shortcuts */
  private final Properties props = new Properties();

  /** Private constructor for singleton pattern */
  private ConfigManager() {}

  /** Returns the singleton instance of ConfigManager. If it doesn't exist yet, creates it. */
  public static ConfigManager getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new ConfigManager();
    }
    return INSTANCE;
  }

  /**
   * Loads the configuration file if it exists. If the file does not exist, initializes default
   * values and saves them.
   */
  public void load() {
    Path path = Paths.get(FILE);
    if (Files.exists(path)) {
      try (InputStream in = Files.newInputStream(path)) {
        props.load(in);
      } catch (IOException e) {
        System.err.println("Warning: .quoridorrc is invalid, using default values.");
      }
    } else {
      // Default configuration values
      props.setProperty("verbose", "true");
      props.setProperty("blitz", "false");
      props.setProperty("timeout", "30");
      save();
    }
  }

  /** Saves the current properties to the configuration file. */
  public void save() {
    try (OutputStream out = Files.newOutputStream(Paths.get(FILE))) {
      props.store(out, "Quoridor configuration + shortcuts");
    } catch (IOException e) {
      System.err.println("Error saving .quoridorrc");
    }
  }

  /**
   * Returns the value of a configuration option.
   *
   * @param key the name of the option
   * @param defaultValue value to return if the key is not found
   * @return the value of the option or defaultValue if missing
   */
  public String getOption(String key, String defaultValue) {
    return props.getProperty(key, defaultValue);
  }

  /**
   * Sets or updates a configuration option.
   *
   * @param key the name of the option
   * @param value the value to store
   */
  public void setOption(String key, String value) {
    props.setProperty(key, value);
  }
}
