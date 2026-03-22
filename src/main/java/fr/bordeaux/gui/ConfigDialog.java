package fr.bordeaux.gui;

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

  /** Private constructor to prevent instantiation. */
  private ConfigDialog() {}

  /**
   * Shows the configuration dialog for keyboard shortcuts. Allows the user to view and customize
   * shortcuts for all actions.
   */
  public static void show() {
    // Create a new window (Stage) for the configuration dialog
    Stage stage = new Stage();
    stage.setTitle("Shortcut Configuration");
    stage.setResizable(true);

    // GridPane layout to arrange labels and buttons in rows and columns
    GridPane grid = new GridPane();
    grid.setVgap(10);
    grid.setHgap(10);

    int row = 0;

    for (Action action : Action.values()) {
      Label actionLabel = new Label(action.name());
      KeyCombination currentShortcut = ShortcutManager.getInstance().getShortcut(action);
      Button keyButton = new Button(currentShortcut.getName());

      keyButton.setFocusTraversable(true); // Allow button to receive key events
      keyButton.setOnKeyPressed(
          (KeyEvent ke) -> {
            KeyCode code = ke.getCode();
            if (code == KeyCode.CONTROL || code == KeyCode.SHIFT || code == KeyCode.ALT) return;

            // Detect pressed modifier keys
            KeyCombination.Modifier ctrl = ke.isControlDown() ? KeyCombination.CONTROL_DOWN : null;
            KeyCombination.Modifier shift = ke.isShiftDown() ? KeyCombination.SHIFT_DOWN : null;
            KeyCombination.Modifier alt = ke.isAltDown() ? KeyCombination.ALT_DOWN : null;

            // Build the new KeyCodeCombination based on modifiers
            KeyCodeCombination newShortcut;
            if (ctrl != null && shift != null && alt != null)
              newShortcut = new KeyCodeCombination(code, ctrl, shift, alt);
            else if (ctrl != null && shift != null)
              newShortcut = new KeyCodeCombination(code, ctrl, shift);
            else if (ctrl != null && alt != null)
              newShortcut = new KeyCodeCombination(code, ctrl, alt);
            else if (shift != null && alt != null)
              newShortcut = new KeyCodeCombination(code, shift, alt);
            else if (ctrl != null) newShortcut = new KeyCodeCombination(code, ctrl);
            else if (shift != null) newShortcut = new KeyCodeCombination(code, shift);
            else if (alt != null) newShortcut = new KeyCodeCombination(code, alt);
            else newShortcut = new KeyCodeCombination(code);

            // Update the shortcut in ShortcutManager and change the button text
            ShortcutManager.getInstance().setShortcut(action, newShortcut);
            keyButton.setText(newShortcut.getName());
          });

      grid.add(actionLabel, 0, row);
      grid.add(keyButton, 1, row);
      row++;
    }

    Button saveButton = new Button("Save");
    saveButton.setOnAction(e -> stage.close());

    HBox saveBox = new HBox(saveButton);
    saveBox.setSpacing(10);
    grid.add(saveBox, 0, row, 2, 1); // span two columns

    Scene scene = new Scene(grid, 400, 50 + row * 40);
    stage.setScene(scene);
    stage.show();
  }
}
