package fr.bordeaux.gui;

import static org.mockito.Mockito.*;

import fr.bordeaux.core.GameEngine;
import java.io.IOException;
import javafx.application.Platform;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class ActionDispatcherTest {

  @Test
  void testNewGame() throws IOException {
    GameEngine engine = mock(GameEngine.class);
    FileHandler fileHandler = mock(FileHandler.class);
    ActionDispatcher dispatcher = new ActionDispatcher(engine, fileHandler);

    dispatcher.dispatch(Action.NEW_GAME);

    verify(engine).newGame();
  }

  @Test
  void testLoadGame() throws IOException {
    GameEngine engine = mock(GameEngine.class);
    FileHandler fileHandler = mock(FileHandler.class);
    when(fileHandler.chooseLoadFile()).thenReturn("test.txt");

    ActionDispatcher dispatcher = new ActionDispatcher(engine, fileHandler);

    dispatcher.dispatch(Action.LOAD_GAME);

    verify(engine).loadGame("test.txt");
  }

  @Test
  void testSaveGame() throws IOException {
    GameEngine engine = mock(GameEngine.class);
    FileHandler fileHandler = mock(FileHandler.class);
    when(fileHandler.chooseSaveFile()).thenReturn("test.txt");

    ActionDispatcher dispatcher = new ActionDispatcher(engine, fileHandler);

    dispatcher.dispatch(Action.SAVE_GAME);

    verify(engine).saveGame("test.txt");
  }

  @Test
  void testUndo() throws IOException {
    GameEngine engine = mock(GameEngine.class);
    FileHandler fileHandler = mock(FileHandler.class);
    ActionDispatcher dispatcher = new ActionDispatcher(engine, fileHandler);

    dispatcher.dispatch(Action.UNDO);

    verify(engine).undo();
  }

  @Test
  void testRedo() throws IOException {
    GameEngine engine = mock(GameEngine.class);
    FileHandler fileHandler = mock(FileHandler.class);
    ActionDispatcher dispatcher = new ActionDispatcher(engine, fileHandler);

    dispatcher.dispatch(Action.REDO);

    verify(engine).redo();
  }

  @Test
  void testPause() throws IOException {
    GameEngine engine = mock(GameEngine.class);
    FileHandler fileHandler = mock(FileHandler.class);
    ActionDispatcher dispatcher = new ActionDispatcher(engine, fileHandler);

    dispatcher.dispatch(Action.PAUSE);

    verify(engine).pause();
  }

  @Test
  void testHint() throws IOException {
    GameEngine engine = mock(GameEngine.class);
    FileHandler fileHandler = mock(FileHandler.class);
    ActionDispatcher dispatcher = new ActionDispatcher(engine, fileHandler);

    dispatcher.dispatch(Action.HINT);

    verify(engine).hint();
  }

  @Test
  void testConfiguration() throws IOException {
    GameEngine engine = mock(GameEngine.class);
    FileHandler fileHandler = mock(FileHandler.class);
    ActionDispatcher dispatcher = new ActionDispatcher(engine, fileHandler);

    try (MockedStatic<ConfigDialog> mocked = mockStatic(ConfigDialog.class)) {
      dispatcher.dispatch(Action.CONFIGURATION);
      mocked.verify(ConfigDialog::show);
    }
  }

  @Test
  void testInfo() {
    GameEngine engine = mock(GameEngine.class);
    FileHandler fileHandler = mock(FileHandler.class);
    ActionDispatcher dispatcher = new ActionDispatcher(engine, fileHandler);

    try (MockedStatic<InfoDialog> mocked = mockStatic(InfoDialog.class)) {
      dispatcher.dispatch(Action.INFO);
      mocked.verify(InfoDialog::show);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void testQuit() throws IOException {
    GameEngine engine = mock(GameEngine.class);
    FileHandler fileHandler = mock(FileHandler.class);
    ActionDispatcher dispatcher = new ActionDispatcher(engine, fileHandler);

    try (MockedStatic<Platform> mocked = mockStatic(Platform.class)) {
      dispatcher.dispatch(Action.QUIT);
      mocked.verify(Platform::exit);
    }
  }
}
