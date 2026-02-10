package fr.bordeaux.gui;

import java.io.*;
import java.util.Properties;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

public class ShortcutConfig {

  private static final String FILE = "config.properties";
  private final Properties props = new Properties();

  public ShortcutConfig() {
    load();
  }

  /** Charge les raccourcis depuis le fichier */
  private void load() {
    try (FileInputStream fis = new FileInputStream(FILE)) {
      props.load(fis);
    } catch (IOException e) {
      System.out.println("Aucun fichier de configuration trouvé, valeurs par défaut utilisées.");
    }
  }

  /** Sauvegarde les raccourcis dans le fichier */
  private void save() {
    try (FileOutputStream fos = new FileOutputStream(FILE)) {
      props.store(fos, "Keyboard shortcuts");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Applique un raccourci à un MenuItem et le sauvegarde
   *
   * @param item MenuItem à configurer
   * @param key Clé unique pour ce raccourci
   * @param defaultValue Valeur par défaut si aucun raccourci existant
   */
  public void apply(MenuItem item, String key, String defaultValue) {
    String value = props.getProperty(key, defaultValue);
    props.setProperty(key, value);
    item.setAccelerator(KeyCombination.keyCombination(value));
    // save(); // facultatif, on peut sauvegarder à la fin via showConfigDialog
  }

  /** Affiche un dialogue permettant à l'utilisateur de modifier les raccourcis */
  public void showConfigDialog(Stage owner) {
    Dialog<Void> dialog = new Dialog<>();
    dialog.setTitle("Configuration du logiciel");
    dialog.initOwner(owner);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);

    int row = 0;
    for (String key : props.stringPropertyNames()) {
      TextField field = new TextField(props.getProperty(key));
      field.setPrefWidth(300);
      GridPane.setHgrow(field, Priority.ALWAYS);

      grid.add(new Label(key + ":"), 0, row);
      grid.add(field, 1, row);

      field.textProperty().addListener((obs, oldVal, newVal) -> props.setProperty(key, newVal));

      row++;
    }

    dialog.getDialogPane().setContent(grid);
    dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

    dialog.getDialogPane().setPrefSize(400, 300);
    dialog.getDialogPane().setMinSize(400, 300);
    dialog.getDialogPane().setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

    dialog.setResultConverter(
        btn -> {
          save();
          return null;
        });

    dialog.showAndWait();
  }
}
