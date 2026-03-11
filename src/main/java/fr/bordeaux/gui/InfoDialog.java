package fr.bordeaux.gui;

import javafx.scene.control.Alert;

public class InfoDialog {
  public static void show() {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Info du jeu Quoridor");
    alert.setHeaderText("Quoridor");
    alert.setGraphic(null);
    alert.setContentText("Version 1.0\nAuthors: Isma,...Students at Bordeaux University");
    alert.show();
    // alert.showAndWait();
  }
}
