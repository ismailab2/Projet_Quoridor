package fr.bordeaux.gui;

import java.io.File;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Handles file system interactions for saving and loading games.
 *
 * <p>This class encapsulates the JavaFX {@link FileChooser} logic to provide a consistent interface
 * for file selection dialogs.
 */
public class FileHandler {
  /** Description for the text file filter. */
  private static final String FILE_DESCRIPTION = "Text files";

  /** Extension used for save files. */
  private static final String FILE_EXTENSION = "*.txt";

  /** The owner window for the dialogs. */
  private final Stage stage;

  /**
   * Constructs a FileHandler associated with a specific stage.
   *
   * @param stage the primary stage used as the owner of the file dialogs
   */
  public FileHandler(final Stage stage) {
    this.stage = stage;
  }

  /**
   * Opens a save file dialog.
   *
   * @return the absolute path of the selected file, or {@code null} if the user cancelled
   */
  public String chooseSaveFile() {
    final FileChooser chooser = createBaseChooser("Save the game");
    chooser.setInitialFileName("quoridor_save.txt");

    final File file = chooser.showSaveDialog(this.stage);
    return file == null ? null : file.getAbsolutePath();
  }

  /**
   * Opens an open file dialog.
   *
   * @return the absolute path of the selected file, or {@code null} if the user cancelled
   */
  public String chooseLoadFile() {
    final FileChooser chooser = createBaseChooser("Load a game");

    final File file = chooser.showOpenDialog(this.stage);
    return file == null ? null : file.getAbsolutePath();
  }

  /**
   * Creates a FileChooser with common configuration.
   *
   * @param title the title of the dialog window
   * @return a configured FileChooser instance
   */
  private FileChooser createBaseChooser(final String title) {
    final FileChooser chooser = new FileChooser();
    chooser.setTitle(title);
    chooser
        .getExtensionFilters()
        .add(new FileChooser.ExtensionFilter(FILE_DESCRIPTION, FILE_EXTENSION));
    return chooser;
  }
}
