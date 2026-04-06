package fr.bordeaux.gui;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import fr.bordeaux.core.*;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

class QuoridorAppTest extends ApplicationTest {

  @Override
  public void start(Stage stage) throws IOException {
    QuoridorApp app = new QuoridorApp();
    app.start(stage);
  }

  @Test
  void testApplicationStarts() {
    Stage stage =
        (Stage)
            javafx.stage.Window.getWindows().stream()
                .filter(javafx.stage.Window::isShowing)
                .findFirst()
                .orElseThrow();

    assertNotNull(stage);
    assertEquals("Quoridor", stage.getTitle());
    assertTrue(stage.isShowing());
  }

  @Test
  void testSceneIsSet() {
    Stage stage =
        (Stage)
            javafx.stage.Window.getWindows().stream()
                .filter(javafx.stage.Window::isShowing)
                .findFirst()
                .orElseThrow();

    Scene scene = stage.getScene();

    assertNotNull(scene);
    assertTrue(scene.getWidth() > 0);
    assertTrue(scene.getHeight() > 0);
  }

  /** Tests that the game-over overlay is correctly displayed and can be closed. */
  @Test
  void testShowGameOverOverlay() {
    StackPane container = new StackPane();
    QuoridorApp app = new QuoridorApp();
    AtomicBoolean popupShown = new AtomicBoolean(true);

    interact(() -> app.showGameOverOverlay(container, "TestWinner", popupShown));

    VBox overlay = (VBox) container.getChildren().get(0);
    assertNotNull(overlay);

    Label winLabel = (Label) overlay.getChildren().get(1);
    assertEquals("TestWinner won the game!", winLabel.getText());

    Button closeBtn = (Button) overlay.getChildren().get(2);
    interact(() -> closeBtn.fire());

    assertTrue(container.getChildren().isEmpty());
    assertFalse(popupShown.get());
  }

  /** Tests that the game-over listener correctly triggers the overlay when the game ends. */
  @Test
  void testSetupGameOverListener() {
    GameEngine engine = mock(GameEngine.class);
    GameState state = mock(GameState.class);
    when(engine.getState()).thenReturn(state);
    when(state.isGameOver()).thenReturn(true);

    Player winner = mock(Player.class);
    when(winner.getName()).thenReturn("Winner");
    when(state.getWinner()).thenReturn(java.util.Optional.of(winner));

    StackPane container = new StackPane();
    QuoridorApp app = new QuoridorApp();

    app.setupGameOverListener(engine, container);

    ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
    verify(state).addListener(captor.capture());

    interact(() -> captor.getValue().run());
    WaitForAsyncUtils.waitForFxEvents();

    assertFalse(container.getChildren().isEmpty());
    VBox overlay = (VBox) container.getChildren().get(0);
    Label winLabel = (Label) overlay.getChildren().get(1);
    assertEquals("Winner won the game!", winLabel.getText());
  }

  private static final Move DEFAULT_MOVE = Move.pawn(new Position(0, 0), new Position(0, 1));

  /** Helper to verify AI turn branches after the thinking phase. */
  private void verifyAiTurnBranch(
      boolean isNullMove,
      boolean gameOverAfter,
      boolean pausedAfter,
      boolean turnChanged,
      boolean playSuccess,
      boolean expectedPlay,
      boolean expectedDraw)
      throws Exception {
    GameEngine engine = mock(GameEngine.class);
    GameState state = mock(GameState.class);
    BoardView boardView = mock(BoardView.class);
    when(engine.getState()).thenReturn(state);

    Player aiPlayer = mock(Player.class);
    when(aiPlayer.isAI()).thenReturn(true);
    Player otherPlayer = mock(Player.class);

    // Initial state setup using an AtomicBoolean to switch states after thinking
    AtomicBoolean thinkingFinished = new AtomicBoolean(false);

    when(state.isGameOver()).thenAnswer(inv -> thinkingFinished.get() ? gameOverAfter : false);
    when(state.isPaused()).thenAnswer(inv -> thinkingFinished.get() ? pausedAfter : false);
    when(state.getCurrentPlayer())
        .thenAnswer(inv -> thinkingFinished.get() && turnChanged ? otherPlayer : aiPlayer);

    Move finalMove = isNullMove ? null : DEFAULT_MOVE;
    when(aiPlayer.getNextMove(state))
        .thenAnswer(
            inv -> {
              Thread.sleep(100); // Simulate thinking time
              thinkingFinished.set(true);
              return finalMove;
            });

    when(engine.play(any())).thenReturn(playSuccess);

    QuoridorApp app = new QuoridorApp();
    app.setupAITurnListener(engine, boardView);

    ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
    verify(state, atLeastOnce()).addListener(captor.capture());

    // Trigger the task
    interact(() -> captor.getValue().run());

    // Wait for AI thread and Platform.runLater
    waitForAiExecution(engine, expectedPlay);

    if (expectedPlay) {
      verify(engine, atLeastOnce()).play(DEFAULT_MOVE);
    } else {
      verify(engine, never()).play(any());
    }

    if (expectedDraw) {
      verify(boardView, atLeastOnce()).drawBoard();
    } else {
      verify(boardView, never()).drawBoard();
    }
  }

  private void waitForAiExecution(GameEngine engine, boolean expectedPlay) throws Exception {
    long timeout = 2000;
    long start = System.currentTimeMillis();
    while (System.currentTimeMillis() - start < timeout) {
      WaitForAsyncUtils.waitForFxEvents();
      try {
        if (expectedPlay) {
          verify(engine, atLeastOnce()).play(any());
          return;
        } else {
          // If not expected, we wait a bit and if still not called, we assume success
          if (System.currentTimeMillis() - start > 1000) return;
        }
      } catch (AssertionError e) {
        // Continue waiting
      }
      Thread.sleep(50);
    }
    if (expectedPlay) {
      fail("AI move was expected but engine.play() was never called");
    }
  }

  @Test
  void testSetupAiTurnListener_Success() throws Exception {
    verifyAiTurnBranch(false, false, false, false, true, true, true);
  }

  @Test
  void testSetupAiTurnListener_NullMove() throws Exception {
    verifyAiTurnBranch(true, false, false, false, true, false, false);
  }

  @Test
  void testSetupAiTurnListener_GameOverDuringThink() throws Exception {
    verifyAiTurnBranch(false, true, false, false, true, false, false);
  }

  @Test
  void testSetupAiTurnListener_PausedDuringThink() throws Exception {
    verifyAiTurnBranch(false, false, true, false, true, false, false);
  }

  @Test
  void testSetupAiTurnListener_TurnChangedDuringThink() throws Exception {
    verifyAiTurnBranch(false, false, false, true, true, false, false);
  }

  @Test
  void testSetupAiTurnListener_PlayFailed() throws Exception {
    verifyAiTurnBranch(false, false, false, false, false, true, false);
  }
}
