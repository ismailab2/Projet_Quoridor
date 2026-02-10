package fr.bordeaux.gui;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class GameController {

  public void newGame() {
    showMessage("Nouvelle partie lancée !");
  }

  public void loadGame() {
    showMessage("Charger une partie !");
  }

  public void saveGame() {
    showMessage("Partie sauvegardée !");
  }

  public void undo() {
    showMessage("Undo !");
  }

  public void redo() {
    showMessage("Redo !");
  }

  public void pause() {
    showMessage("Pause !");
  }

  public void hint() {
    showMessage("Hint !");
  }

  public void showInfo() {
    showMessage("JavaFX Game v1.0\nAuteur: Isma");
  }

  private void showMessage(String message) {
    Alert alert = new Alert(AlertType.INFORMATION);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
}
