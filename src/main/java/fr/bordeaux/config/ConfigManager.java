package fr.bordeaux.config;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

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

        System.err.println("Warning: invalid .qoridorrc file. Using defaults.");
      }
    } else {
      // Default configuration values
      props.setProperty("verbose", "true");
      props.setProperty("blitz", "false");
      props.setProperty("timeout", "30");
      save();
    }
  }


  public void save() {
    try (OutputStream out = Files.newOutputStream(Paths.get(FILE))) {

      props.store(out, "Quoridor configuration");

    } catch (IOException e) {
      System.err.println("Error saving .quoridorrc");
    }
  }

  public String getOption(String key, String defaultValue) {
    return props.getProperty(key, defaultValue);
  }

  public void setOption(String key, String value) {
    props.setProperty(key, value);
  }

  public boolean getBoolean(String key, boolean defaultValue) {

    return Boolean.parseBoolean(props.getProperty(key, String.valueOf(defaultValue)));
  }

  public int getInt(String key, int defaultValue) {

    try {
      return Integer.parseInt(props.getProperty(key));
    } catch (Exception e) {
      return defaultValue;
    }
  }
}
