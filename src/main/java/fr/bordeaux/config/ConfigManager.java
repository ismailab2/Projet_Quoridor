package fr.bordeaux.config;

import fr.bordeaux.i18n.I18n;
import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/**
 * Manages the application configuration using a singleton pattern. This class handles loading and
 * saving settings (like timeouts, blitz mode, or shortcuts) from a persistent file located in the
 * user's home directory.
 */
public class ConfigManager {

  private static ConfigManager instance;

  /** Configuration properties and shortcuts. */
  private final Properties props = new Properties();

  /** Private constructor for singleton pattern */
  private ConfigManager() {}

  /**
   * Returns the singleton instance of ConfigManager. Lazily initialized. initialized.
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
    final String customPath = System.getProperty("quoridor.config.file");
    if (customPath != null) {
      return Paths.get(customPath);
    }
    return Paths.get(System.getProperty("user.home"), ".quoridorrc");
  }

  /** Loads config if it exists. Otherwise saves defaults. */
  public void load() {
    props.clear();
    final Path path = getFilePath();
    if (Files.exists(path)) {

      try (final InputStream inputStream = Files.newInputStream(path)) {

        props.load(inputStream);

      } catch (final IOException e) {

        System.err.println(I18n.get("warning"));
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
    try (final OutputStream out = Files.newOutputStream(getFilePath())) {

      props.store(out, "Quoridor configuration");

    } catch (IOException e) {
      System.err.println(I18n.get("configError"));
    }
  }

  /**
   * Retrieves a configuration option as a String.
   *
   * @param key The property key.
   * @param defaultValue The value to return if the key is not found.
   * @return The property value or the default value.
   */
  public String getOption(final String key, final String defaultValue) {
    return props.getProperty(key, defaultValue);
  }

  /**
   * Sets a configuration option.
   *
   * @param key The property key.
   * @param value The value to associate with the key.
   */
  public void setOption(final String key, final String value) {
    props.setProperty(key, value);
  }

  /**
   * Retrieves a configuration option as a boolean.
   *
   * @param key The property key.
   * @param defaultValue The value to return if the key is not found or invalid.
   * @return The boolean value of the property.
   */
  public boolean getBoolean(final String key, final boolean defaultValue) {
    return Boolean.parseBoolean(props.getProperty(key, String.valueOf(defaultValue)));
  }

  /**
   * Retrieves a configuration option as an integer.
   *
   * @param key property key
   * @param defaultValue default value
   * @return integer value of property
   */
  public int getInt(final String key, final int defaultValue) {
    try {
      final String value = props.getProperty(key);
      if (value == null) return defaultValue;
      return Integer.parseInt(value);
    } catch (final NumberFormatException e) {
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
   * Returns the path of the position file.
   *
   * @return contest file path, or ""
   */
  public String getContestFile() {
    return getOption("contest-file", "");
  }

  /**
   * Enables contest mode and stores the associated position-file path.
   *
   * @param filePath absolute or relative path to the position file
   */
  public void enableContest(final String filePath) {
    setOption("contest", "true");
    setOption("contest-file", filePath);
  }
}
