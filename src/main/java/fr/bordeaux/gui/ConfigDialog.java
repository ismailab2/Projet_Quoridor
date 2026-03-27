package fr.bordeaux.gui;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/** Utility class used to display the shortcut configuration dialog. */
public final class ConfigDialog {

  private ConfigDialog() {}

  /** Shows the configuration dialog for keyboard shortcuts. */
  public static void show() {

    Stage stage = new Stage();
    stage.setTitle("Shortcut Configuration");
    stage.setResizable(true);

    GridPane grid = new GridPane();
    grid.setVgap(10);
    grid.setHgap(10);

    int row = 0;

    for (Action action : Action.values()) {

      Label actionLabel = new Label(action.name());

      KeyCombination currentShortcut = ShortcutManager.getInstance().getShortcut(action);

      Button keyButton = new Button(currentShortcut.getName());

      keyButton.setFocusTraversable(true);

      keyButton.setOnKeyPressed(ke -> handleKeyPress(ke, action, keyButton));

      grid.add(actionLabel, 0, row);
      grid.add(keyButton, 1, row);
      row++;
    }

    Button saveButton = new Button("Save");
    saveButton.setOnAction(e -> stage.close());

    HBox saveBox = new HBox(saveButton);
    saveBox.setSpacing(10);

    grid.add(saveBox, 0, row, 2, 1);

    Scene scene = new Scene(grid, 400, 50 + row * 40);
    stage.setScene(scene);
    stage.show();
  }

  /** Handles keyboard shortcut editing. */
  private static void handleKeyPress(KeyEvent ke, Action action, Button keyButton) {

    KeyCode code = ke.getCode();

    // Ignore modifier-only keys
    if (isModifierKey(code)) {
      return;
    }

    KeyCombination.Modifier[] modifiers = extractModifiers(ke);

    KeyCodeCombination newShortcut = new KeyCodeCombination(code, modifiers);

    ShortcutManager.getInstance().setShortcut(action, newShortcut);

    keyButton.setText(newShortcut.getName());
  }

  /** Returns true if the key is only a modifier key. */
  private static boolean isModifierKey(KeyCode code) {
    return code == KeyCode.CONTROL || code == KeyCode.SHIFT || code == KeyCode.ALT;
  }

  /** Extracts active modifiers from the key event. */
  private static KeyCombination.Modifier[] extractModifiers(KeyEvent ke) {
    List<KeyCombination.Modifier> mods = new ArrayList<>();

    if (ke.isControlDown()) {
      mods.add(KeyCombination.CONTROL_DOWN);
    }
    if (ke.isShiftDown()) {
      mods.add(KeyCombination.SHIFT_DOWN);
    }
    if (ke.isAltDown()) {
      mods.add(KeyCombination.ALT_DOWN);
    }

    return mods.toArray(new KeyCombination.Modifier[0]);
  }
}
