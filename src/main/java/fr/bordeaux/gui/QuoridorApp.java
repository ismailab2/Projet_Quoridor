package fr.bordeaux.gui;

import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.core.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
    GameState state = new GameState(new Board(BOARD_SIZE), Player.createDefaultPlayers());
    GameEngine engine = new GameEngine(state);
      FileHandler fileHandler = new FileHandler(stage);
      ActionDispatcher dispatcher = new ActionDispatcher(engine, fileHandler);


    PlayerPanel player1Panel = new PlayerPanel("Player 1", state, 0);
    PlayerPanel player2Panel = new PlayerPanel("Player 2", state, 1);

    double panelWidth = 200;
    for (PlayerPanel p : new PlayerPanel[] {player1Panel, player2Panel}) {
      p.setMinWidth(panelWidth);
      p.setMaxWidth(panelWidth);
      p.setPrefWidth(panelWidth);
    }

    BorderPane.setAlignment(player1Panel, Pos.CENTER);
    BorderPane.setAlignment(player2Panel, Pos.CENTER);
    BorderPane.setMargin(player1Panel, new Insets(20));
    BorderPane.setMargin(player2Panel, new Insets(20));

    BoardView boardView = new BoardView(engine);

    StackPane boardContainer = new StackPane(boardView);
    boardContainer.setAlignment(Pos.CENTER);
    BorderPane.setAlignment(boardContainer, Pos.CENTER);

    BorderPane root = new BorderPane();
    MainMenuBar menuBar = new MainMenuBar(dispatcher);
    root.setTop(menuBar);
    root.setCenter(boardContainer);
    root.setLeft(player1Panel);
    root.setRight(player2Panel);

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

    scene
        .widthProperty()
        .addListener(
            (obs, oldVal, newVal) ->
                resizeBoard(boardView, scene, panelWidth, menuBar.getHeight()));

    scene
        .heightProperty()
        .addListener(
            (obs, oldVal, newVal) ->
                resizeBoard(boardView, scene, panelWidth, menuBar.getHeight()));

    Platform.runLater(() -> resizeBoard(boardView, scene, panelWidth, menuBar.getHeight()));
  }

  /**
   * @param boardView boardView
   * @param scene scene
   * @param panelWidth panelWidth
   * @param menuHeight menuHeight
   */
  private void resizeBoard(BoardView boardView, Scene scene, double panelWidth, double menuHeight) {
    double availableWidth = scene.getWidth() - (panelWidth + 40) * 2;
    double availableHeight = scene.getHeight() - menuHeight;

    double size = Math.min(availableWidth, availableHeight);

    if (size > 0) {
      boardView.setPrefSize(size, size);
      boardView.setMinSize(size, size);
      boardView.setMaxSize(size, size);
    }
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
