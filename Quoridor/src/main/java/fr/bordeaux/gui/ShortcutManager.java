package fr.bordeaux.gui;

import fr.bordeaux.config.ConfigManager;
import java.util.EnumMap;
import java.util.Map;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;

public class ShortcutManager {

  private ActionDispatcher dispatcher;

  private static final ShortcutManager INSTANCE = new ShortcutManager();
  private final Map<Action, KeyCombination> shortcuts = new EnumMap<>(Action.class);

  private ShortcutManager() {
    loadDefaults();
    ConfigManager.getInstance().applyShortcuts(shortcuts);
  }

  public static ShortcutManager getInstance() {
    return INSTANCE;
  }

  public void setActionDispatcher(ActionDispatcher dispatcher) {
    this.dispatcher = dispatcher;
  }

  private void loadDefaults() {
    add(Action.NEW_GAME, "Ctrl+N");
    add(Action.LOAD_GAME, "Ctrl+L");
    add(Action.SAVE_GAME, "Ctrl+S");
    add(Action.CONFIGURATION, "Ctrl+,");
    add(Action.INFO, "Ctrl+I");
    add(Action.QUIT, "Ctrl+Q");
    add(Action.UNDO, "Ctrl+U");
    add(Action.REDO, "Ctrl+R");
    add(Action.PAUSE, "Ctrl+P");
    add(Action.HINT, "Ctrl+H");
  }

  private void add(Action action, String shortcut) {
    shortcuts.put(action, KeyCombination.valueOf(shortcut));
  }

  public void register(Scene scene) {
    if (dispatcher == null)
      throw new IllegalStateException("ActionDispatcher must be set before registering shortcuts!");
    shortcuts.forEach(
        (action, combo) -> scene.getAccelerators().put(combo, () -> dispatcher.dispatch(action)));
  }

  public KeyCombination getShortcut(Action action) {
    return shortcuts.get(action);
  }

  public Map<Action, KeyCombination> getAll() {
    return shortcuts;
  }
}
