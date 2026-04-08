package fr.bordeaux.gui;

import fr.bordeaux.core.*;
import fr.bordeaux.main.GuiLauncher;
import fr.bordeaux.util.Logger;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
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

  /** Static width for the player panels. */
  private static final double PANEL_WIDTH = 200.0;

  /** Minimum window width for the application. */
  private static final double MIN_WIDTH = 800.0;

  /** Minimum window height for the application. */
  private static final double MIN_HEIGHT = 600.0;

  /** Default margin used for layout spacing. */
  private static final double DEFAULT_MARGIN = 20.0;

  /** Spacing between elements in the game-over overlay. */
  private static final int OVERLAY_SPACING = 20;

  /** Width of the game-over overlay. */
  private static final double OVERLAY_WIDTH = 400.0;

  /** Height of the game-over overlay. */
  private static final double OVERLAY_HEIGHT = 200.0;

  /** Vertical spacing in the central layout box. */
  private static final int CENTER_SPACING = 10;

  /** Spacing between Hint button and its status label. */
  private static final int HINT_AREA_SPACING = 15;

  /** CSS style for the Hint button. */
  private static final String HINT_BUTTON_STYLE = "-fx-font-size: 14px; -fx-padding: 8 16;";

  /** CSS style for the Hint status label. */
  private static final String HINT_STATUS_STYLE = "-fx-text-fill: #666666; -fx-font-style: italic;";

  /** Message displayed when AI is computing a hint. */
  private static final String HINT_BUSY_TEXT = "AI is thinking...";

  /** Message displayed when AI is idle. */
  private static final String HINT_IDLE_TEXT = "";

  /**Tracks the current hint task to allow cancellation. */
  private final AtomicReference<CompletableFuture<Move>> currentHintTask = new AtomicReference<>();

  /** Default constructor. */
  public QuoridorApp() {
    Logger.getInstance().debug("Instantiating QuoridorApp");
  }

  /**
   * Called when the JavaFX application is started. Sets up the main window, game board, menu, and
   * event handling.
   *
   * @param stage the primary stage for this application
   * @throws IOException if the game engine cannot be built
   */
  @Override
  public void start(final Stage stage) throws IOException {
    final GuiLauncher guiLauncher = new GuiLauncher();
    final GameEngine engine = guiLauncher.buildEngine();
    final GameState state = engine.getState();

    state.addListener(
        () -> {
          final CompletableFuture<Move> task = currentHintTask.getAndSet(null);
          if (task != null) {
            task.cancel(true);
          }
        });

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

    final javafx.scene.Node hintArea = createHintArea(engine, boardView);
    if (hintArea instanceof HBox container && !container.getChildren().isEmpty()) {
      final Button hintButton = (Button) container.getChildren().get(0);
      dispatcher.setHintHandler(hintButton::fire);
    }

    final VBox centerBox = new VBox(CENTER_SPACING, boardContainer, hintArea);
    centerBox.setAlignment(Pos.CENTER);
    VBox.setVgrow(boardContainer, Priority.ALWAYS);

    final MainMenuBar menuBar = new MainMenuBar(dispatcher);
    final BorderPane root = buildRootLayout(menuBar, centerBox, panels);

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
   * Creates the Hint button area and configures its async hinting logic.
   *
   * @param engine the game engine
   * @param boardView the board view for highlighting the move
   * @return a Node containing the Hint button and status label
   */
  /* default */ javafx.scene.Node createHintArea(final GameEngine engine, final BoardView boardView) {
    final Button hintButton = new Button("Hint");
    hintButton.setStyle(HINT_BUTTON_STYLE);

    final Label statusLabel = new Label(HINT_IDLE_TEXT);
    statusLabel.setStyle(HINT_STATUS_STYLE);

    final HBox container = new HBox(HINT_AREA_SPACING, hintButton, statusLabel);
    container.setAlignment(Pos.CENTER);

    hintButton.setOnAction(
        event -> {
          hintButton.setDisable(true);
          statusLabel.setText(HINT_BUSY_TEXT);

          final CompletableFuture<Move> future =
              CompletableFuture.supplyAsync(
                  () -> {
                    try {
                      return engine.hint();
                    } catch (IOException e) {
                      if (Logger.getInstance().isErrorEnabled()) {
                        Logger.getInstance().error("Error computing hint: {0}", e.getMessage());
                      }
                      return null;
                    }
                  });

          currentHintTask.set(future);

          future.whenComplete(
              (move, throwable) ->
                  Platform.runLater(
                      () -> {
                        hintButton.setDisable(false);
                        statusLabel.setText(HINT_IDLE_TEXT);
                        currentHintTask.compareAndSet(future, null);
                        if (move != null && !future.isCancelled()) {
                          boardView.highlightMove(move);
                        }
                      }));
        });
    return container;
  }

  /**
   * Fixes the width of a player panel to {@link #PANEL_WIDTH}.
   *
   * @param panel the panel to configure
   */
  static void setupPanelSize(final PlayerPanel panel) {
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
  static StackPane buildBoardContainer(final BoardView boardView) {
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
  static BorderPane buildRootLayout(
      final MainMenuBar menuBar, final Region centerNode, final PlayerPanel[] panels) {

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
    root.setCenter(centerNode);
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
    final double availableHeight = scene.getHeight() - menuHeight - 50.0;
    final double size = Math.min(availableWidth, availableHeight) - 1.0; // 1.0px safety margin
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
              state
                  .getWinner()
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
    final AtomicReference<Player> aiComputing = new AtomicReference<>();
    final GameState state = engine.getState();

    final Runnable aiTurnTask =
        () -> {
          if (!state.isGameOver() && !state.isPaused()) {
            final Player currentPlayer = state.getCurrentPlayer();
            if (currentPlayer.isAI() && aiComputing.compareAndSet(null, currentPlayer)) {
              final Thread aiThread =
                  new Thread(
                      () -> {
                        Move move = null;
                        try {
                          move = currentPlayer.getNextMove(state);
                        } catch (final Exception e) {
                          Logger.getInstance().error("AI move computation failed", e);
                        } finally {
                          final Move finalMove = move;
                          Platform.runLater(
                              () -> {
                                aiComputing.compareAndSet(currentPlayer, null);
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

  /** Called when the JavaFX application is stopped. */
  @Override
  public void stop() {}
}
