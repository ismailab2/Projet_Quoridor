package fr.bordeaux.config;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

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

  private void loadDefaults() {
    props.setProperty("verbose", "false");
    props.setProperty("debug", "false");
    props.setProperty("blitz", "false");
    props.setProperty("timeout", "30");
  }

  public void save() {

    try (OutputStream out = Files.newOutputStream(Paths.get(FILE))) {

      props.store(out, "Quoridor configuration");

    } catch (IOException e) {

      System.err.println("Error saving configuration file.");
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
