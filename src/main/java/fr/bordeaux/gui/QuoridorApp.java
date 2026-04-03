package fr.bordeaux.gui;

import fr.bordeaux.ai.MctsPlayer;
import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.core.*;
import java.io.IOException;
import java.util.List;
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
  public void start(Stage stage) throws IOException {
    int BOARD_SIZE = 9;
    Player player1 = new HumanPlayer("Human Player", new Position(0, 4), Color.WHITE);
    Player player2 = new MctsPlayer("MCTS Player", new Position(8, 4), 3000, true, Color.BLACK);
    GameState state = new GameState(new Board(BOARD_SIZE), List.of(player1, player2));
    GameEngine engine =
        new GameEngine(
            state,
            Boolean.parseBoolean(ConfigManager.getInstance().getOption("blitz", "false")),
            false,
            false,
            false,
            Integer.parseInt(ConfigManager.getInstance().getOption("nb-walls", "10")));
    FileHandler fileHandler = new FileHandler(stage);
    ActionDispatcher dispatcher = new ActionDispatcher(engine, fileHandler);

    PlayerPanel player1Panel = new PlayerPanel(state.getPlayers().get(0).getName(), state, 0);
    PlayerPanel player2Panel = new PlayerPanel(state.getPlayers().get(1).getName(), state, 1);

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

    setupAITurnListener(engine, boardView);
    setupGameOverListener(engine, boardContainer);
  }

  /**
   * Sets up a listener to show a popup when the game is over.
   *
   * @param engine The game engine to interact with.
   * @param container The container where the overlay will be displayed.
   */
  private void setupGameOverListener(GameEngine engine, StackPane container) {
    java.util.concurrent.atomic.AtomicBoolean popupShown =
        new java.util.concurrent.atomic.AtomicBoolean(false);

    engine
        .getState()
        .addListener(
            () -> {
              GameState state = engine.getState();
              if (state.isGameOver()) {
                if (popupShown.compareAndSet(false, true)) {
                  Player winner = null;
                  for (Player p : state.getPlayers()) {
                    if (state.hasReachedGoal(p)) {
                      winner = p;
                      break;
                    }
                  }
                  if (winner != null) {
                    final String winnerName = winner.getName();
                    Platform.runLater(
                        () -> {
                          VBox overlay = new VBox(20);
                          overlay.setAlignment(Pos.CENTER);
                          overlay.setMaxSize(400, 200);
                          overlay.setStyle(
                              "-fx-background-color: rgba(0, 0, 0, 0.85);"
                                  + "-fx-padding: 30;"
                                  + "-fx-background-radius: 15;"
                                  + "-fx-border-color: rgba(255, 255, 255, 0.2);"
                                  + "-fx-border-radius: 15;"
                                  + "-fx-border-width: 2;");

                          javafx.scene.control.Label titleLabel =
                              new javafx.scene.control.Label("Game Over");
                          titleLabel.setStyle(
                              "-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold;");

                          javafx.scene.control.Label winLabel =
                              new javafx.scene.control.Label(winnerName + " won the game!");
                          winLabel.setStyle("-fx-text-fill: lightgreen; -fx-font-size: 20px;");

                          javafx.scene.control.Button closeBtn =
                              new javafx.scene.control.Button("Close");
                          closeBtn.setStyle(
                              "-fx-font-size: 16px; -fx-padding: 8 20; -fx-cursor: hand;");
                          closeBtn.setOnAction(e -> container.getChildren().remove(overlay));

                          overlay.getChildren().addAll(titleLabel, winLabel, closeBtn);
                          container.getChildren().add(overlay);
                        });
                  }
                }
              } else {
                popupShown.set(false);
              }
            });
  }

  /**
   * Sets up a listener to trigger AI turns automatically without freezing the JavaFX thread.
   *
   * @param engine The game engine to interact with.
   * @param boardView The board view to redraw after an AI move.
   */
  private void setupAITurnListener(GameEngine engine, BoardView boardView) {
    java.util.concurrent.atomic.AtomicBoolean aiComputing =
        new java.util.concurrent.atomic.AtomicBoolean(false);

    Runnable aiTurnTask =
        () -> {
          GameState state = engine.getState();
          if (!state.isGameOver() && !state.isPaused()) {
            Player currentPlayer = state.getCurrentPlayer();
            if (currentPlayer.isAI() && aiComputing.compareAndSet(false, true)) {
              new Thread(
                      () -> {
                        Move move = null;
                        try {
                          move = currentPlayer.getNextMove(state);
                        } catch (Exception e) {
                          e.printStackTrace();
                        } finally {
                          Move finalMove = move;
                          Platform.runLater(
                              () -> {
                                aiComputing.set(false);
                                if (finalMove != null
                                    && !state.isGameOver()
                                    && !state.isPaused()
                                    && state.getCurrentPlayer() == currentPlayer) {
                                  boolean success = engine.play(finalMove);
                                  if (success) {
                                    boardView.drawBoard();
                                  }
                                }
                              });
                        }
                      })
                  .start();
            }
          }
        };

    engine.getState().addListener(aiTurnTask);
    Platform.runLater(aiTurnTask);
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
