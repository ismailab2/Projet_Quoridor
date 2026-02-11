package fr.bordeaux.gui;

import fr.bordeaux.config.ConfigManager;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;

import java.util.EnumMap;
import java.util.Map;

public class ShortcutManager {

    private static final ShortcutManager INSTANCE = new ShortcutManager();
    private final Map<Action, KeyCombination> shortcuts = new EnumMap<>(Action.class);

    private ShortcutManager() {
        loadDefaults();
        ConfigManager.getInstance().applyShortcuts(shortcuts);
    }

    public static ShortcutManager getInstance() {
        return INSTANCE;
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
        shortcuts.forEach((action, combo) ->
                scene.getAccelerators().put(
                        combo,
                        () -> ActionDispatcher.dispatch(action)
                )
        );
    }

    public KeyCombination getShortcut(Action action) {
        return shortcuts.get(action);
    }

    public Map<Action, KeyCombination> getAll() {
        return shortcuts;
    }
}
