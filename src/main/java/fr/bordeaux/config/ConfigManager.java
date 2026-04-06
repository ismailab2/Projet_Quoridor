package fr.bordeaux.config;

import fr.bordeaux.util.Log;
import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/**
 * Manages the application configuration using a singleton pattern. This class handles loading and
 * saving settings (like timeouts, blitz mode, or shortcuts) from a persistent file located in the
 * user's home directory.
 */
public final class ConfigManager {

  private static ConfigManager instance;

  /** Stores all configuration properties and shortcuts */
  private final Properties props = new Properties();

  /** Private constructor for singleton pattern */
  private ConfigManager() {}

  /**
   * Returns the singleton instance of ConfigManager. If the instance does not exist, it is lazily
   * initialized.
   *
   * @return The unique {@link ConfigManager} instance.
   */
  public static synchronized ConfigManager getInstance() {
    if (instance == null) {
      instance = new ConfigManager();
    }
    return instance;
  }

  /**
   * Returns the dynamic path to the config file
   *
   * @return Path to the config file
   */
  private Path getFilePath() {
    return Paths.get(System.getProperty("user.home"), ".quoridorrc");
  }

  /**
   * Loads the configuration file if it exists. If the file does not exist, initializes default
   * values and saves them.
   */
  public void load() {
    Path path = getFilePath();
    if (Files.exists(path)) {

      try (InputStream in = Files.newInputStream(path)) {

        props.load(in);

      } catch (IOException e) {

        Log.error("Warning: invalid .quoridorrc file. Using defaults.", e);
      }
    } else {
      props.setProperty("verbose", "true");
      props.setProperty("blitz", "false");
      props.setProperty("timeout", "30");
      props.setProperty("debug", "true");
      props.setProperty("ai", "false");
      props.setProperty("nb-walls", "20");
      props.setProperty("board-size", "9");
      props.setProperty("contest", "false");
      props.setProperty("contest-file", "");
      save();
    }
  }

  /** Saves the current properties to the configuration file. */
  public void save() {
    try (OutputStream out = Files.newOutputStream(getFilePath())) {

      props.store(out, "Quoridor configuration");

    } catch (IOException e) {
      Log.error("Error saving .quoridorrc", e);
    }
  }

  /**
   * Retrieves a configuration option as a String.
   *
   * @param key The property key.
   * @param defaultValue The value to return if the key is not found.
   * @return The property value or the default value.
   */
  public String getOption(String key, String defaultValue) {
    return props.getProperty(key, defaultValue);
  }

  /**
   * Sets a configuration option.
   *
   * @param key The property key.
   * @param value The value to associate with the key.
   */
  public void setOption(String key, String value) {
    props.setProperty(key, value);
  }

  /**
   * Retrieves a configuration option as a boolean.
   *
   * @param key The property key.
   * @param defaultValue The value to return if the key is not found or invalid.
   * @return The boolean value of the property.
   */
  public boolean getBoolean(String key, boolean defaultValue) {
    return Boolean.parseBoolean(props.getProperty(key, String.valueOf(defaultValue)));
  }

  /**
   * Retrieves a configuration option as an integer.
   *
   * @param key The property key.
   * @param defaultValue The value to return if the key is not found or not an integer.
   * @return The integer value of the property.
   */
  public int getInt(String key, int defaultValue) {
    try {
      String value = props.getProperty(key);
      if (value == null) return defaultValue;
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  /**
   * Returns {@code true} when the application was launched with {@code --contest}.
   *
   * @return whether contest mode is active
   */
  public boolean isContestMode() {
    return getBoolean("contest", false);
  }

  /**
   * Returns the path of the position file supplied to {@code --contest}. Returns an empty string
   * when no file was given.
   *
   * @return the contest file path, or {@code ""}
   */
  public String getContestFile() {
    return getOption("contest-file", "");
  }

  /**
   * Enables contest mode and stores the associated position-file path.
   *
   * @param filePath absolute or relative path to the position file
   */
  public void enableContest(String filePath) {
    setOption("contest", "true");
    setOption("contest-file", filePath);
  }
}
