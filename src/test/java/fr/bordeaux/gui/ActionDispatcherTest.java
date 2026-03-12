package fr.bordeaux.gui;

import static org.mockito.Mockito.*;

import fr.bordeaux.core.GameEngine;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/**
 * @class ActionDispatcherTest
 * @brief Unit tests for ActionDispatcher.
 *     <p>This test class verifies that each ActionDispatcher action correctly calls the
 *     corresponding method in the GameEngine or triggers the appropriate GUI dialog.
 *     <p>Each dispatchable action (NEW_GAME, LOAD_GAME, SAVE_GAME, UNDO, REDO, PAUSE, HINT,
 *     CONFIGURATION, INFO, QUIT) is tested to ensure proper delegation.
 */
class ActionDispatcherTest {

  private GameEngine engine;
  private ActionDispatcher dispatcher;

  @BeforeEach
  void setUp() {
    engine = mock(GameEngine.class);
    dispatcher = new ActionDispatcher(engine);
  }

  /**
   * @brief Test that NEW_GAME action calls GameEngine.newGame().
   */
  @Test
  void testNewGameCallsEngine() {
    dispatcher.dispatch(Action.NEW_GAME);
    verify(engine, times(1)).newGame();
  }

  /**
   * @brief Test that LOAD_GAME action calls GameEngine.loadGame("txt.txt").
   */
  @Test
  void testLoadGameCallsEngine() {
    dispatcher.dispatch(Action.LOAD_GAME);
    verify(engine, times(1)).loadGame("txt.txt");
  }

  /**
   * @brief Test that SAVE_GAME action calls GameEngine.saveGame("txt.txt").
   */
  @Test
  void testSaveGameCallsEngine() {
    dispatcher.dispatch(Action.SAVE_GAME);
    verify(engine, times(1)).saveGame("txt.txt");
  }

  /**
   * @brief Test that UNDO action calls GameEngine.undo().
   */
  @Test
  void testUndoCallsEngine() {
    dispatcher.dispatch(Action.UNDO);
    verify(engine, times(1)).undo();
  }

  /**
   * @brief Test that REDO action calls GameEngine.redo().
   */
  @Test
  void testRedoCallsEngine() {
    dispatcher.dispatch(Action.REDO);
    verify(engine, times(1)).redo();
  }

  /**
   * @brief Test that PAUSE action calls GameEngine.pause().
   */
  @Test
  void testPauseCallsEngine() {
    dispatcher.dispatch(Action.PAUSE);
    verify(engine, times(1)).pause();
  }

  /**
   * @brief Test that HINT action calls GameEngine.hint().
   */
  @Test
  void testHintCallsEngine() {
    dispatcher.dispatch(Action.HINT);
    verify(engine, times(1)).hint();
  }

  /**
   * @brief Test that CONFIGURATION action shows the ConfigDialog.
   */
  @Test
  void testConfigDialogShows() {
    try (MockedStatic<ConfigDialog> mockConfig = mockStatic(ConfigDialog.class)) {
      dispatcher.dispatch(Action.CONFIGURATION);
      mockConfig.verify(ConfigDialog::show, times(1));
    }
  }

  /**
   * @brief Test that INFO action shows the InfoDialog.
   */
  @Test
  void testInfoDialogShows() {
    try (MockedStatic<InfoDialog> mockInfo = mockStatic(InfoDialog.class)) {
      dispatcher.dispatch(Action.INFO);
      mockInfo.verify(InfoDialog::show, times(1));
    }
  }

  /**
   * @brief Test that QUIT action calls Platform.exit().
   */
  @Test
  void testQuitCallsPlatformExit() {
    try (MockedStatic<Platform> mockPlatform = mockStatic(Platform.class)) {
      dispatcher.dispatch(Action.QUIT);
      mockPlatform.verify(Platform::exit, times(1));
    }
  }
}
