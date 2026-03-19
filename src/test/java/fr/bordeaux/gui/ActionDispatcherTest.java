package fr.bordeaux.gui;

import static org.mockito.Mockito.*;

import fr.bordeaux.core.GameEngine;
import javafx.application.Platform;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class ActionDispatcherTest {

  @Test
  void testNewGame() {
    GameEngine engine = mock(GameEngine.class);
    ActionDispatcher dispatcher = new ActionDispatcher(engine);

    dispatcher.dispatch(Action.NEW_GAME);

    verify(engine).newGame();
  }

  @Test
  void testLoadGame() {
    GameEngine engine = mock(GameEngine.class);
    ActionDispatcher dispatcher = new ActionDispatcher(engine);

    dispatcher.dispatch(Action.LOAD_GAME);

    verify(engine).loadGame("txt.txt");
  }

  @Test
  void testSaveGame() {
    GameEngine engine = mock(GameEngine.class);
    ActionDispatcher dispatcher = new ActionDispatcher(engine);

    dispatcher.dispatch(Action.SAVE_GAME);

    verify(engine).saveGame("txt.txt");
  }

  @Test
  void testUndo() {
    GameEngine engine = mock(GameEngine.class);
    ActionDispatcher dispatcher = new ActionDispatcher(engine);

    dispatcher.dispatch(Action.UNDO);

    verify(engine).undo();
  }

  @Test
  void testRedo() {
    GameEngine engine = mock(GameEngine.class);
    ActionDispatcher dispatcher = new ActionDispatcher(engine);

    dispatcher.dispatch(Action.REDO);

    verify(engine).redo();
  }

  @Test
  void testPause() {
    GameEngine engine = mock(GameEngine.class);
    ActionDispatcher dispatcher = new ActionDispatcher(engine);

    dispatcher.dispatch(Action.PAUSE);

    verify(engine).pause();
  }

  @Test
  void testHint() {
    GameEngine engine = mock(GameEngine.class);
    ActionDispatcher dispatcher = new ActionDispatcher(engine);

    dispatcher.dispatch(Action.HINT);

    verify(engine).hint();
  }

  @Test
  void testConfiguration() {
    GameEngine engine = mock(GameEngine.class);
    ActionDispatcher dispatcher = new ActionDispatcher(engine);

    try (MockedStatic<ConfigDialog> mocked = mockStatic(ConfigDialog.class)) {

      dispatcher.dispatch(Action.CONFIGURATION);

      mocked.verify(ConfigDialog::show);
    }
  }

  @Test
  void testInfo() {
    GameEngine engine = mock(GameEngine.class);
    ActionDispatcher dispatcher = new ActionDispatcher(engine);

    try (MockedStatic<InfoDialog> mocked = mockStatic(InfoDialog.class)) {

      dispatcher.dispatch(Action.INFO);

      mocked.verify(InfoDialog::show);
    }
  }

  @Test
  void testQuit() {
    GameEngine engine = mock(GameEngine.class);
    ActionDispatcher dispatcher = new ActionDispatcher(engine);

    try (MockedStatic<Platform> mocked = mockStatic(Platform.class)) {

      dispatcher.dispatch(Action.QUIT);

      mocked.verify(Platform::exit);
    }
  }
}
