package fr.bordeaux.gui;

import javafx.scene.control.Alert;

public class InfoDialog {
  public static void show() {
    showMessage(
        "Voici les informations "
            + "sur notre logiciel \n"
            + "Nom du jeu : Quoridor \n"
            + "Version du : 1.0.0 \n"
            + "Auteurs: Ismail, ...");
  }

  private static void showMessage(String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
}
