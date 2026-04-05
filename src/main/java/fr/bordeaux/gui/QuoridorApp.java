package fr.bordeaux.gui;

import fr.bordeaux.Main.GuiLauncher;
import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.core.*;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Main JavaFX application class for Quoridor.
 *
 * <p>Displays the game board in the center, player 1 panel on the left, and player 2 panel on the
 * right. Handles initializing the game state, setting up the scene, and attaching GUI components.
 */
public class QuoridorApp extends Application {

  private static final double PANEL_WIDTH = 200.0;

  /** Default constructor for the Quoridor JavaFX application. */
  public QuoridorApp() {
    super();
  }

  /**
   * Called when the JavaFX application is started. Sets up the main window, game board, menu, and
   * GUI event handling.
   *
   * @param stage the primary stage for this application
   * @throws IOException if the game engine cannot be built
   */
  @Override
  public void start(Stage stage) throws IOException {
    GuiLauncher guiLauncher = new GuiLauncher();
    GameEngine engine = guiLauncher.buildEngine();
    GameState state = engine.getState();

    FileHandler fileHandler = new FileHandler(stage);
    ActionDispatcher dispatcher = new ActionDispatcher(engine, fileHandler);

    PlayerPanel player1Panel = new PlayerPanel(state.getPlayers().get(0).getName(), state, 0);
    PlayerPanel player2Panel = new PlayerPanel(state.getPlayers().get(1).getName(), state, 1);
    setupPanelSize(player1Panel);
    setupPanelSize(player2Panel);

    BoardView boardView = new BoardView(engine);
    StackPane boardContainer = buildBoardContainer(boardView);

    MainMenuBar menuBar = new MainMenuBar(dispatcher);
    BorderPane root = buildRootLayout(menuBar, boardContainer, player1Panel, player2Panel);

    DragAndDrop.install(engine, boardView);

    Scene scene = new Scene(root);
    setupShortcuts(scene, dispatcher);

    stage.setTitle("Quoridor");
    stage.setScene(scene);
    stage.setMinWidth(800);
    stage.setMinHeight(600);
    stage.setMaximized(true);
    stage.show();

    setupResizeListeners(scene, boardView, menuBar);
    setupAITurnListener(engine, boardView);
    setupGameOverListener(engine, boardContainer);
  }

  /**
   * Fixes the width of a player panel to {@link #PANEL_WIDTH}.
   *
   * @param panel the panel to configure
   */
  private void setupPanelSize(PlayerPanel panel) {
    panel.setMinWidth(PANEL_WIDTH);
    panel.setMaxWidth(PANEL_WIDTH);
    panel.setPrefWidth(PANEL_WIDTH);
  }

  /**
   * Builds the centered board container with alignment and margin.
   *
   * @param boardView the board view to wrap
   * @return a StackPane containing the board view
   */
  private StackPane buildBoardContainer(BoardView boardView) {
    StackPane container = new StackPane(boardView);
    container.setAlignment(Pos.CENTER);
    BorderPane.setAlignment(container, Pos.CENTER);
    return container;
  }

  /**
   * Assembles the root BorderPane layout with all components.
   *
   * @param menuBar the top menu bar
   * @param boardContainer the center board container
   * @param player1Panel the left player panel
   * @param player2Panel the right player panel
   * @return the configured BorderPane
   */
  private BorderPane buildRootLayout(
      MainMenuBar menuBar,
      StackPane boardContainer,
      PlayerPanel player1Panel,
      PlayerPanel player2Panel) {
    BorderPane.setAlignment(player1Panel, Pos.CENTER);
    BorderPane.setAlignment(player2Panel, Pos.CENTER);
    BorderPane.setMargin(player1Panel, new Insets(20));
    BorderPane.setMargin(player2Panel, new Insets(20));

    BorderPane root = new BorderPane();
    root.setTop(menuBar);
    root.setCenter(boardContainer);
    root.setLeft(player1Panel);
    root.setRight(player2Panel);
    return root;
  }

  /**
   * Registers the shortcut manager on the scene and sets its dispatcher.
   *
   * @param scene the JavaFX scene
   * @param dispatcher the action dispatcher
   */
  private void setupShortcuts(Scene scene, ActionDispatcher dispatcher) {
    ShortcutManager manager = ShortcutManager.getInstance();
    manager.setDispatcher(dispatcher);
    manager.register(scene);
  }

  /**
   * Attaches resize listeners to the scene to keep the board square.
   *
   * @param scene the JavaFX scene
   * @param boardView the board view to resize
   * @param menuBar the menu bar (its height is excluded from available space)
   */
  private void setupResizeListeners(Scene scene, BoardView boardView, MainMenuBar menuBar) {
    scene
        .widthProperty()
        .addListener((obs, oldVal, newVal) -> resizeBoard(boardView, scene, menuBar.getHeight()));
    scene
        .heightProperty()
        .addListener((obs, oldVal, newVal) -> resizeBoard(boardView, scene, menuBar.getHeight()));
    Platform.runLater(() -> resizeBoard(boardView, scene, menuBar.getHeight()));
  }

  /**
   * Resizes the board view to fill the available square area in the scene.
   *
   * @param boardView the board view to resize
   * @param scene the current scene
   * @param menuHeight the height of the menu bar to subtract
   */
  private void resizeBoard(BoardView boardView, Scene scene, double menuHeight) {
    double availableWidth = scene.getWidth() - (PANEL_WIDTH + 40) * 2;
    double availableHeight = scene.getHeight() - menuHeight;
    double size = Math.min(availableWidth, availableHeight);
    if (size > 0) {
      boardView.setPrefSize(size, size);
      boardView.setMinSize(size, size);
      boardView.setMaxSize(size, size);
    }
  }

  /**
   * Sets up a listener to show a game-over overlay when the game ends.
   *
   * @param engine the game engine to observe
   * @param container the container where the overlay will be displayed
   */
  private void setupGameOverListener(GameEngine engine, StackPane container) {
    AtomicBoolean popupShown = new AtomicBoolean(false);

    engine
        .getState()
        .addListener(
            () -> {
              GameState state = engine.getState();
              if (state.isGameOver()) {
                if (popupShown.compareAndSet(false, true)) {
                  findWinner(state)
                      .ifPresent(
                          winner ->
                              Platform.runLater(
                                  () ->
                                      showGameOverOverlay(
                                          container, winner.getName(), popupShown)));
                }
              } else {
                popupShown.set(false);
              }
            });
  }

  /**
   * Finds the winner in the current game state.
   *
   * @param state the current game state
   * @return an Optional containing the winning player, or empty if none found
   */
  private java.util.Optional<Player> findWinner(GameState state) {
    return state.getPlayers().stream().filter(state::hasReachedGoal).findFirst();
  }

  /**
   * Displays the game-over overlay on the board container.
   *
   * @param container the container to add the overlay to
   * @param winnerName the name of the winning player
   * @param popupShown the flag to reset when the overlay is closed
   */
  private void showGameOverOverlay(
      StackPane container, String winnerName, AtomicBoolean popupShown) {
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

    Label titleLabel = new Label("Game Over");
    titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold;");

    Label winLabel = new Label(winnerName + " won the game!");
    winLabel.setStyle("-fx-text-fill: lightgreen; -fx-font-size: 20px;");

    Button closeBtn = new Button("Close");
    closeBtn.setStyle("-fx-font-size: 16px; -fx-padding: 8 20; -fx-cursor: hand;");
    closeBtn.setOnAction(
        e -> {
          container.getChildren().remove(overlay);
          popupShown.set(false);
        });

    overlay.getChildren().addAll(titleLabel, winLabel, closeBtn);
    container.getChildren().add(overlay);
  }

  /**
   * Sets up a listener to trigger AI turns automatically without freezing the JavaFX thread.
   *
   * @param engine the game engine to interact with
   * @param boardView the board view to redraw after an AI move
   */
  private void setupAITurnListener(GameEngine engine, BoardView boardView) {
    AtomicBoolean aiComputing = new AtomicBoolean(false);

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
                                  if (engine.play(finalMove)) {
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
