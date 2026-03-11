package fr.bordeaux.gui;

import fr.bordeaux.config.ConfigManager;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * @class ShortcutManager
 * @brief Manages keyboard shortcuts for all actions in the Quoridor GUI.
 *
 * This singleton class loads, stores, and updates keyboard shortcuts.
 * It allows the user to customize shortcuts and persists them in the configuration file.
 * It also registers shortcuts on a JavaFX Scene and triggers actions through an ActionDispatcher.
 */
public class ShortcutManager {

    private static final ShortcutManager INSTANCE = new ShortcutManager();

    /** Maps each action to its keyboard shortcut */
    private final Map<Action, KeyCombination> shortcuts = new HashMap<>();

    /** Dispatcher to trigger actions when shortcuts are pressed */
    private ActionDispatcher dispatcher;

    /**
     * @brief Private constructor for singleton pattern.
     * Loads default shortcuts and attempts to load saved shortcuts from ConfigManager.
     */
    private ShortcutManager() {
        loadDefaults();
        loadFromConfig();
    }


    public static ShortcutManager getInstance() {
        return INSTANCE;
    }

    /**
     * @brief Sets the ActionDispatcher used to execute actions when a shortcut is pressed.
     * @param dispatcher The ActionDispatcher instance
     */
    public void setDispatcher(ActionDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    /**
     * @brief Retrieves the keyboard shortcut for a given action.
     * @param action The action to query
     * @return The KeyCombination associated with the action
     */
    public KeyCombination getShortcut(Action action) {
        return shortcuts.get(action);
    }

    /**
     * @brief Sets or updates the shortcut for an action and saves it in the configuration.
     * Ignores shortcuts that consist only of modifier keys (Ctrl, Shift, Alt).
     * @param action The action to update
     * @param combination The new KeyCombination
     */
    public void setShortcut(Action action, KeyCombination combination) {
        if (combination instanceof KeyCodeCombination) {
            KeyCode code = ((KeyCodeCombination) combination).getCode();
            if (code == KeyCode.CONTROL || code == KeyCode.SHIFT || code == KeyCode.ALT) return;
        }

        shortcuts.put(action, combination);

        ConfigManager config = ConfigManager.getInstance();
        config.setOption("shortcut." + action.name(), combination.getName());
        config.save();
    }


    private void loadDefaults() {
        shortcuts.put(Action.NEW_GAME, new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        shortcuts.put(Action.LOAD_GAME, new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN));
        shortcuts.put(Action.SAVE_GAME, new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        shortcuts.put(Action.CONFIGURATION, new KeyCodeCombination(KeyCode.COMMA, KeyCombination.CONTROL_DOWN));
        shortcuts.put(Action.INFO, new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN));
        shortcuts.put(Action.QUIT, new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
        shortcuts.put(Action.UNDO, new KeyCodeCombination(KeyCode.U, KeyCombination.CONTROL_DOWN));
        shortcuts.put(Action.REDO, new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN));
        shortcuts.put(Action.PAUSE, new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN));
        shortcuts.put(Action.HINT, new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN));
    }

    /**
     * @brief Loads previously saved shortcuts from the ConfigManager.
     * If a saved value is invalid, the default shortcut is retained.
     */
    private void loadFromConfig() {
        ConfigManager config = ConfigManager.getInstance();
        for (Action action : Action.values()) {
            String value = config.getOption("shortcut." + action.name(), null);
            if (value != null) {
                try {
                    shortcuts.put(action, parseKeyCombination(value));
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    /**
     * @brief Parses a string (like "Ctrl+Shift+P") into a KeyCodeCombination.
     * @param value The string representation of the shortcut
     * @return A KeyCodeCombination object representing the shortcut
     */
    private KeyCodeCombination parseKeyCombination(String value) {
        String[] parts = value.split("\\+");
        KeyCode code = KeyCode.valueOf(parts[parts.length - 1].toUpperCase());

        KeyCombination.Modifier ctrl = null;
        KeyCombination.Modifier shift = null;
        KeyCombination.Modifier alt = null;

        for (int i = 0; i < parts.length - 1; i++) {
            switch (parts[i].toUpperCase()) {
                case "CTRL": ctrl = KeyCombination.CONTROL_DOWN; break;
                case "SHIFT": shift = KeyCombination.SHIFT_DOWN; break;
                case "ALT": alt = KeyCombination.ALT_DOWN; break;
            }
        }

        if (ctrl != null && shift != null && alt != null) return new KeyCodeCombination(code, ctrl, shift, alt);
        if (ctrl != null && shift != null) return new KeyCodeCombination(code, ctrl, shift);
        if (ctrl != null && alt != null) return new KeyCodeCombination(code, ctrl, alt);
        if (shift != null && alt != null) return new KeyCodeCombination(code, shift, alt);
        if (ctrl != null) return new KeyCodeCombination(code, ctrl);
        if (shift != null) return new KeyCodeCombination(code, shift);
        if (alt != null) return new KeyCodeCombination(code, alt);
        return new KeyCodeCombination(code);
    }

    /**
     * @brief Registers all shortcuts on a given JavaFX Scene.
     * When a registered shortcut is pressed, the corresponding action is dispatched.
     * @param scene The Scene to register the shortcuts on
     * @throws IllegalStateException if the ActionDispatcher is not set
     */
    public void register(Scene scene) {
        if (dispatcher == null) throw new IllegalStateException("ActionDispatcher not set");

        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            for (Action action : shortcuts.keySet()) {
                if (shortcuts.get(action).match(event)) {
                    dispatcher.dispatch(action);
                    event.consume();
                }
            }
        });
    }
}