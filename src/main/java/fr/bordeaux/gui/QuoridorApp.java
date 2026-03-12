package fr.bordeaux.gui;

import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.core.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Main JavaFX application class for Quoridor. Shows the board in the center, player 1 walls on
 * right, player 2 walls on left.
 */
public class QuoridorApp extends Application {

  @Override
  public void start(Stage stage) {
    ConfigManager.getInstance().load();

    GameState state = new GameState(new Board(9), Player.createDefaultPlayers());
    GameEngine engine = new GameEngine(state);

    ActionDispatcher dispatcher = new ActionDispatcher(engine);

    BorderPane root = new BorderPane();

    // Menu bar on top
    MainMenuBar menuBar = new MainMenuBar(dispatcher);
    root.setTop(menuBar);

    BoardView boardView = new BoardView(engine);
    root.setCenter(boardView);
      // Attach drag-and-drop functionality
      DragAndDrop.install(engine, boardView);

    //
    Scene scene = new Scene(root, 900, 700);

    ShortcutManager manager = ShortcutManager.getInstance();
    manager.setDispatcher(dispatcher);
    manager.register(scene);

    stage.setTitle("Quoridor");
    stage.setScene(scene);
    stage.setResizable(true);
    stage.show();
  }

  @Override
  public void stop() {
    ConfigManager.getInstance().save();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
