package fr.bordeaux.gui;

import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.core.*;
import fr.bordeaux.main.GuiLauncher;
import java.io.IOException;
import java.util.List;
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

/** Main JavaFX app for Quoridor. Displays board and player panels. */
public class QuoridorApp extends Application {

  private static final double PANEL_WIDTH = 200.0;
  private static final double MIN_WIDTH = 800.0;
  private static final double MIN_HEIGHT = 600.0;
  private static final double DEFAULT_MARGIN = 20.0;
  private static final int OVERLAY_SPACING = 20;
  private static final double OVERLAY_WIDTH = 400.0;
  private static final double OVERLAY_HEIGHT = 200.0;

  /** Default constructor. */
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
  public void start(final Stage stage) throws IOException {
    final GuiLauncher guiLauncher = new GuiLauncher();
    final GameEngine engine = guiLauncher.buildEngine();
    final GameState state = engine.getState();

    final FileHandler fileHandler = new FileHandler(stage);
    final ActionDispatcher dispatcher = new ActionDispatcher(engine, fileHandler);

    final List<Player> players = state.getPlayers();
    final PlayerPanel[] panels = new PlayerPanel[players.size()];
    for (int i = 0; i < players.size(); i++) {
      panels[i] = new PlayerPanel(players.get(i).getName(), state, i);
      setupPanelSize(panels[i]);
    }

    final BoardView boardView = new BoardView(engine);
    final StackPane boardContainer = buildBoardContainer(boardView);

    final MainMenuBar menuBar = new MainMenuBar(dispatcher);
    final BorderPane root = buildRootLayout(menuBar, boardContainer, panels);

    DragAndDrop.install(engine, boardView);

    final Scene scene = new Scene(root);
    setupShortcuts(scene, dispatcher);

    stage.setTitle("Quoridor");
    stage.setScene(scene);
    stage.setMinWidth(MIN_WIDTH);
    stage.setMinHeight(MIN_HEIGHT);
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
  private void setupPanelSize(final PlayerPanel panel) {
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
  private StackPane buildBoardContainer(final BoardView boardView) {
    final StackPane container = new StackPane(boardView);
    container.setAlignment(Pos.CENTER);
    BorderPane.setAlignment(container, Pos.CENTER);
    return container;
  }

  /**
   * Assembles root layout components.
   *
   * @param menuBar the top menu bar
   * @param boardContainer the center board container
   * @param panels array of player panels
   * @return the configured BorderPane
   */
  private BorderPane buildRootLayout(
      final MainMenuBar menuBar, final StackPane boardContainer, final PlayerPanel[] panels) {

    final BorderPane root = new BorderPane();
    final Insets margin = new Insets(DEFAULT_MARGIN);

    final VBox leftContainer = new VBox(DEFAULT_MARGIN);
    leftContainer.setAlignment(Pos.CENTER);

    if (panels.length > 0) {
      leftContainer.getChildren().add(panels[0]);
    }
    if (panels.length > 2) {
      leftContainer.getChildren().add(panels[2]);
    }

    final VBox rightContainer = new VBox(DEFAULT_MARGIN);
    rightContainer.setAlignment(Pos.CENTER);

    if (panels.length > 1) {
      rightContainer.getChildren().add(panels[1]);
    }
    if (panels.length > 3) {
      rightContainer.getChildren().add(panels[3]);
    }

    BorderPane.setMargin(leftContainer, margin);
    BorderPane.setMargin(rightContainer, margin);

    root.setTop(menuBar);
    root.setCenter(boardContainer);
    root.setLeft(leftContainer);
    root.setRight(rightContainer);

    return root;
  }

  /**
   * Registers the shortcut manager on the scene and sets its dispatcher.
   *
   * @param scene the JavaFX scene
   * @param dispatcher the action dispatcher
   */
  private void setupShortcuts(final Scene scene, final ActionDispatcher dispatcher) {
    final ShortcutManager manager = ShortcutManager.getInstance();
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
  private void setupResizeListeners(
      final Scene scene, final BoardView boardView, final MainMenuBar menuBar) {
    scene
        .widthProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              resizeBoard(boardView, scene, menuBar.getHeight());
            });
    scene
        .heightProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              resizeBoard(boardView, scene, menuBar.getHeight());
            });
    Platform.runLater(
        () -> {
          resizeBoard(boardView, scene, menuBar.getHeight());
        });
  }

  /**
   * Resizes the board view to fill the available square area in the scene.
   *
   * @param boardView the board view to resize
   * @param scene the current scene
   * @param menuHeight the height of the menu bar to subtract
   */
  private void resizeBoard(final BoardView boardView, final Scene scene, final double menuHeight) {
    final double availableWidth = scene.getWidth() - (PANEL_WIDTH + DEFAULT_MARGIN * 2) * 2;
    final double availableHeight = scene.getHeight() - menuHeight;
    final double size = Math.min(availableWidth, availableHeight);
    if (size > 0.0) {
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
  void setupGameOverListener(final GameEngine engine, final StackPane container) {
    final AtomicBoolean popupShown = new AtomicBoolean(false);
    final GameState state = engine.getState();

    state.addListener(
        () -> {
          if (state.isGameOver()) {
            if (popupShown.compareAndSet(false, true)) {
              findWinner(state)
                  .ifPresent(
                      winner ->
                          Platform.runLater(
                              () -> {
                                showGameOverOverlay(container, winner.getName(), popupShown);
                              }));
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
  private java.util.Optional<Player> findWinner(final GameState state) {
    return state.getPlayers().stream().filter(state::hasReachedGoal).findFirst();
  }

  /**
   * Displays the game-over overlay on the board container.
   *
   * @param container the container to add the overlay to
   * @param winnerName the name of the winning player
   * @param popupShown the flag to reset when the overlay is closed
   */
  void showGameOverOverlay(
      final StackPane container, final String winnerName, final AtomicBoolean popupShown) {
    final VBox overlay = new VBox(OVERLAY_SPACING);
    overlay.setAlignment(Pos.CENTER);
    overlay.setMaxSize(OVERLAY_WIDTH, OVERLAY_HEIGHT);
    overlay.setStyle(
        "-fx-background-color: rgba(0, 0, 0, 0.85);"
            + "-fx-padding: 30;"
            + "-fx-background-radius: 15;"
            + "-fx-border-color: rgba(255, 255, 255, 0.2);"
            + "-fx-border-radius: 15;"
            + "-fx-border-width: 2;");

    final Label titleLabel = new Label("Game Over");
    titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold;");

    final Label winLabel = new Label(winnerName + " won the game!");
    winLabel.setStyle("-fx-text-fill: lightgreen; -fx-font-size: 20px;");

    final Button closeBtn = new Button("Close");
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
  void setupAITurnListener(final GameEngine engine, final BoardView boardView) {
    final AtomicBoolean aiComputing = new AtomicBoolean(false);
    final GameState state = engine.getState();

    final Runnable aiTurnTask =
        () -> {
          if (!state.isGameOver() && !state.isPaused()) {
            final Player currentPlayer = state.getCurrentPlayer();
            if (currentPlayer.isAI() && aiComputing.compareAndSet(false, true)) {
              final Thread aiThread =
                  new Thread(
                      () -> {
                        Move move = null;
                        try {
                          move = currentPlayer.getNextMove(state);
                        } catch (final Exception e) {
                          e.printStackTrace();
                        } finally {
                          final Move finalMove = move;
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
                      });
              aiThread.start();
            }
          }
        };

    state.addListener(aiTurnTask);
    Platform.runLater(aiTurnTask);
  }

  /**
   * Called when the JavaFX application is stopped. Saves configuration or performs cleanup tasks.
   */
  @Override
  public void stop() {
    ConfigManager.getInstance().save();
  }
}
