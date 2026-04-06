package fr.bordeaux.gui;

import fr.bordeaux.config.ConfigManager;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

/**
 * Manages keyboard shortcuts for all actions in the Quoridor GUI.
 *
 * <p>This singleton class loads, stores, and updates keyboard shortcuts. It allows the user to
 * customize shortcuts and persists them in the configuration file. It also registers shortcuts on a
 * JavaFX Scene and triggers actions through an ActionDispatcher.
 */
public final class ShortcutManager {

  private static final ShortcutManager INSTANCE = new ShortcutManager();

  /** Maps each action to its keyboard shortcut */
  private final Map<Action, KeyCombination> shortcuts = new HashMap<>();

  /** Dispatcher to trigger actions when shortcuts are pressed */
  private ActionDispatcher dispatcher;

  /**
   * Private constructor for singleton pattern. Loads default shortcuts and attempts to load saved
   * shortcuts from ConfigManager.
   */
  private ShortcutManager() {
    loadDefaults();
    loadFromConfig();
  }

  /**
   * Returns the unique instance of the shortcut manager.
   *
   * @return the singleton instance of the shortcut manager
   */
  public static ShortcutManager getInstance() {
    return INSTANCE;
  }

  /**
   * Sets the ActionDispatcher used to execute actions when a shortcut is pressed.
   *
   * @param dispatcher The ActionDispatcher instance
   */
  public void setDispatcher(final ActionDispatcher dispatcher) {
    this.dispatcher = dispatcher;
  }

  /**
   * Retrieves the keyboard shortcut for a given action.
   *
   * @param action The action to query
   * @return The KeyCombination associated with the action
   */
  public KeyCombination getShortcut(final Action action) {
    return shortcuts.get(action);
  }

  /**
   * Sets or updates the shortcut for an action and saves it in the configuration. Ignores shortcuts
   * that consist only of modifier keys (Ctrl, Shift, Alt).
   *
   * @param action The action to update
   * @param combination The new KeyCombination
   */
  public void setShortcut(final Action action, final KeyCombination combination) {
    shortcuts.put(action, combination);
    final ConfigManager config = ConfigManager.getInstance();
    config.setOption("shortcut." + action.name(), combination.getName());
    config.save();
  }

  private void loadDefaults() {
    shortcuts.put(Action.NEW_GAME, new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
    shortcuts.put(Action.LOAD_GAME, new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN));
    shortcuts.put(Action.SAVE_GAME, new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
    shortcuts.put(
        Action.CONFIGURATION, new KeyCodeCombination(KeyCode.COMMA, KeyCombination.CONTROL_DOWN));
    shortcuts.put(Action.INFO, new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN));
    shortcuts.put(Action.QUIT, new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
    shortcuts.put(Action.UNDO, new KeyCodeCombination(KeyCode.U, KeyCombination.CONTROL_DOWN));
    shortcuts.put(Action.REDO, new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN));
    shortcuts.put(Action.PAUSE, new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN));
    shortcuts.put(Action.HINT, new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN));
  }

  /**
   * Loads previously saved shortcuts from the ConfigManager. If a saved value is invalid, the
   * default shortcut is retained.
   */
  private void loadFromConfig() {
    final ConfigManager config = ConfigManager.getInstance();
    for (final Action action : Action.values()) {
      final String value = config.getOption("shortcut." + action.name(), null);
      if (value != null) {
        try {
          shortcuts.put(action, parseKeyCombination(value));
        } catch (final IllegalArgumentException ignored) {
          // Ignored if invalid
        }
      }
    }
  }

  /**
   * Parses a string (like "Ctrl+Shift+P") into a KeyCodeCombination.
   *
   * @param value The string representation of the shortcut
   * @return A KeyCodeCombination object representing the shortcut
   */
  private KeyCodeCombination parseKeyCombination(final String value) {
    final String[] parts = value.split("\\+");
    final KeyCode code =
        KeyCode.valueOf(parts[parts.length - 1].toUpperCase(java.util.Locale.ENGLISH));

    KeyCombination.Modifier ctrl = null;
    KeyCombination.Modifier shift = null;
    KeyCombination.Modifier alt = null;

    for (int index = 0; index < parts.length - 1; index++) {
      final String part = parts[index].toUpperCase(java.util.Locale.ENGLISH);
      switch (part) {
        case "CTRL":
          ctrl = KeyCombination.CONTROL_DOWN;
          break;
        case "SHIFT":
          shift = KeyCombination.SHIFT_DOWN;
          break;
        case "ALT":
          alt = KeyCombination.ALT_DOWN;
          break;
        default:
          break;
      }
    }

    final KeyCodeCombination result;
    if (ctrl != null && shift != null && alt != null) {
      result = new KeyCodeCombination(code, ctrl, shift, alt);
    } else if (ctrl != null && shift != null) {
      result = new KeyCodeCombination(code, ctrl, shift);
    } else if (ctrl != null && alt != null) {
      result = new KeyCodeCombination(code, ctrl, alt);
    } else if (shift != null && alt != null) {
      result = new KeyCodeCombination(code, shift, alt);
    } else if (ctrl != null) {
      result = new KeyCodeCombination(code, ctrl);
    } else if (shift != null) {
      result = new KeyCodeCombination(code, shift);
    } else if (alt != null) {
      result = new KeyCodeCombination(code, alt);
    } else {
      result = new KeyCodeCombination(code);
    }
    return result;
  }

  /**
   * Registers all shortcuts on a given JavaFX Scene. When a registered shortcut is pressed, the
   * corresponding action is dispatched.
   *
   * @param scene The Scene to register the shortcuts on
   * @throws IllegalStateException if the ActionDispatcher is not set
   */
  public void register(final Scene scene) {
    if (dispatcher == null) {
      throw new IllegalStateException("ActionDispatcher not set");
    }

    scene.addEventHandler(
        KeyEvent.KEY_PRESSED,
        event -> {
          for (final Map.Entry<Action, KeyCombination> entry : shortcuts.entrySet()) {
            final KeyCombination combination = entry.getValue();
            if (combination.match(event)) {
              try {
                this.dispatcher.dispatch(entry.getKey());
              } catch (final IOException exception) {
                throw new java.io.UncheckedIOException(exception);
              }
              event.consume();
            }
          }
        });
  }
}
