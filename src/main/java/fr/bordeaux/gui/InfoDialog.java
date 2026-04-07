package fr.bordeaux.gui;

import fr.bordeaux.i18n.I18n;
import javafx.scene.control.Alert;

/** Utility class used to display the application information dialog. */
public final class InfoDialog {

  /** Private constructor to prevent instantiation. */
  private InfoDialog() {}

  /** Displays the information dialog of the application. */
  public static void show() {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(I18n.get("info"));
    alert.setHeaderText("Quoridor");
    alert.setGraphic(null);
    alert.setContentText("Version 1.0\n"+I18n.get("authors") + " Isma, Khadidja, Joris, Matthieu, Cengiz. "+I18n.get("students"));
    alert.show();
  }
}
