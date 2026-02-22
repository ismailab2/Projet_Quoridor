package fr.bordeaux.config;

import fr.bordeaux.gui.Action;
import java.io.*;
import java.util.Map;
import java.util.Properties;
import javafx.scene.input.KeyCombination;

public class ConfigManager {

  private static final ConfigManager INSTANCE = new ConfigManager();
  private final File file = new File(System.getProperty("user.home"), ".quoridorrc");
  private final Properties props = new Properties();

  private ConfigManager() {}

  public static ConfigManager getInstance() {
    return INSTANCE;
  }

  public void load() {
    if (!file.exists()) return;
    try (FileReader reader = new FileReader(file)) {
      props.load(reader);
    } catch (IOException ignored) {
    }
  }

  public void save() {
    try (FileWriter writer = new FileWriter(file)) {
      props.store(writer, "Quoridor configuration");
    } catch (IOException ignored) {
    }
  }

  public void applyShortcuts(Map<Action, KeyCombination> map) {
    map.forEach(
        (action, defaultCombo) -> {
          String value = props.getProperty("shortcut." + action.name());
          if (value != null) {
            map.put(action, KeyCombination.valueOf(value));
          }
        });
  }

  public void saveShortcut(Action action, KeyCombination combo) {
    props.setProperty("shortcut." + action.name(), combo.getName());
  }
}
