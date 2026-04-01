package fr.bordeaux.gui;

import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.core.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Main JavaFX application class for Quoridor.
 *
 * <p>Displays the game board in the center, player 1 walls on the right, and player 2 walls on the
 * left. Handles initializing the game state, setting up the scene, and attaching GUI components.
 */
public class QuoridorApp extends Application {

  /** Default constructor for the Quoridor JavaFX application. */
  public QuoridorApp() {
    super();
  }

  /**
   * Called when the JavaFX application is started. Sets up the main window, game board, menu, and
   * GUI event handling.
   *
   * @param stage the primary stage for this application
   */
  @Override
  public void start(Stage stage) {
    int BOARD_SIZE = 9;
    GameState state;

    state = new GameState(new Board(BOARD_SIZE), Player.createDefaultPlayers());
    GameEngine engine = new GameEngine(state);

    FileHandler fileHandler = new FileHandler(stage);

    ActionDispatcher dispatcher = new ActionDispatcher(engine, fileHandler);

    BorderPane root = new BorderPane();

    // Menu bar on top
    MainMenuBar menuBar = new MainMenuBar(dispatcher);
    root.setTop(menuBar);

    BoardView boardView = new BoardView(engine);
    root.setCenter(boardView);
    // Attach drag-and-drop functionality
    DragAndDrop.install(engine, boardView);

    Scene scene = new Scene(root);

    ShortcutManager manager = ShortcutManager.getInstance();
    manager.setDispatcher(dispatcher);
    manager.register(scene);

    stage.setTitle("Quoridor");
    stage.setScene(scene);
    stage.setMinWidth(800);
    stage.setMinHeight(600);
    stage.setMaximized(true);
    stage.show();
  }

  /**
   * Called when the JavaFX application is stopped. Saves configuration or performs cleanup tasks.
   */
  @Override
  public void stop() {
    ConfigManager.getInstance().save();
  }

  /**
   * Main entry point for launching the JavaFX application.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    launch(args);
  }
}
