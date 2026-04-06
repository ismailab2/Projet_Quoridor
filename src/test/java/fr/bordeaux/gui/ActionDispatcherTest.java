package fr.bordeaux.gui;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import fr.bordeaux.core.GameEngine;
import java.io.IOException;
import javafx.application.Platform;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class ActionDispatcherTest {

  @Test
  void testNewGame() throws IOException {
    final GameEngine engine = mock(GameEngine.class);
    final FileHandler fileHandler = mock(FileHandler.class);
    final ActionDispatcher dispatcher = new ActionDispatcher(engine, fileHandler);

    dispatcher.dispatch(Action.NEW_GAME);

    verify(engine).newGame(anyInt(), anyList(), any());
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

  @Test
  void testServerStatusShowsCurrentState() throws IOException {
    GameEngine engine = mock(GameEngine.class);
    FileHandler fileHandler = mock(FileHandler.class);
    ActionDispatcher dispatcher = spy(new ActionDispatcher(engine, fileHandler));

    doNothing().when(dispatcher).showMessage(anyString(), anyString(), anyString());

    dispatcher.dispatch(Action.SERVER_STATUS);

    verify(dispatcher)
        .showMessage(eq("Network"), eq("Server Status"), contains("Connected clients"));
  }

  @Test
  void testServerListShowsDiscoveryMessage() throws IOException {
    GameEngine engine = mock(GameEngine.class);
    FileHandler fileHandler = mock(FileHandler.class);
    ActionDispatcher dispatcher = spy(new ActionDispatcher(engine, fileHandler));

    doNothing().when(dispatcher).showMessage(anyString(), anyString(), anyString());

    dispatcher.dispatch(Action.SERVER_LIST);

    verify(dispatcher)
        .showMessage(eq("Network"), eq("Server List"), contains("Server discovery started"));
  }

  @Test
  void testStartServerPromptsAndShowsConfirmation() throws IOException {
    GameEngine engine = mock(GameEngine.class);
    FileHandler fileHandler = mock(FileHandler.class);
    ActionDispatcher dispatcher = spy(new ActionDispatcher(engine, fileHandler));

    doReturn("12345")
        .when(dispatcher)
        .promptUser(anyString(), anyString(), anyString(), anyString());
    doNothing().when(dispatcher).showMessage(anyString(), anyString(), anyString());

    dispatcher.dispatch(Action.START_SERVER);

    verify(dispatcher)
        .showMessage(eq("Network"), eq("Server"), contains("Server started on port 12345"));
  }
}
