package fr.bordeaux.gui;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import fr.bordeaux.core.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class ActionDispatcherTest {

  @BeforeAll
  static void initJavaFX() {
    new JFXPanel();
  }

  private ShortcutManager mockShortcutManager() {
    ShortcutManager manager = mock(ShortcutManager.class);
    for (Action a : Action.values()) {
      when(manager.getShortcut(a))
          .thenReturn(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN));
    }
    return manager;
  }

  @Test
  void testDispatchActions() throws InterruptedException {
    GameEngine engine = mock(GameEngine.class);

    // Mock statique des dialogues et Platform.exit
    try (MockedStatic<ConfigDialog> cfg = mockStatic(ConfigDialog.class);
        MockedStatic<InfoDialog> info = mockStatic(InfoDialog.class);
        MockedStatic<Platform> platform = mockStatic(Platform.class)) {

      ActionDispatcher dispatcher = new ActionDispatcher(engine);

      dispatcher.dispatch(Action.NEW_GAME);
      verify(engine).newGame();

      dispatcher.dispatch(Action.LOAD_GAME);
      verify(engine).loadGame("save.txt");

      dispatcher.dispatch(Action.SAVE_GAME);
      verify(engine).saveGame("save.txt");

      dispatcher.dispatch(Action.PAUSE);
      verify(engine).pause();

      dispatcher.dispatch(Action.UNDO);
      verify(engine).undo();

      dispatcher.dispatch(Action.REDO);
      verify(engine).redo();

      dispatcher.dispatch(Action.CONFIGURATION);
      cfg.verify(ConfigDialog::show);

      dispatcher.dispatch(Action.INFO);
      info.verify(InfoDialog::show);

      dispatcher.dispatch(Action.QUIT);
      platform.verify(Platform::exit);

      dispatcher.dispatch(Action.HINT);
      verify(engine).hint();
    }
  }

  @Test
  void testMainMenuBar() throws InterruptedException {
    GameEngine engine = mock(GameEngine.class);
    ActionDispatcher dispatcher = new ActionDispatcher(engine);
    ShortcutManager shortcutManager = mockShortcutManager();

    try (MockedStatic<ShortcutManager> mocked = mockStatic(ShortcutManager.class)) {
      mocked.when(ShortcutManager::getInstance).thenReturn(shortcutManager);

      ActionDispatcher.MainMenuBar menuBar = new ActionDispatcher.MainMenuBar(dispatcher);

      assertEquals(2, menuBar.getMenus().size());
      Menu file = menuBar.getMenus().get(0);
      Menu game = menuBar.getMenus().get(1);
      assertEquals("File", file.getText());
      assertEquals("Game", game.getText());

      CountDownLatch latch = new CountDownLatch(1);
      Platform.runLater(
          () -> {
            for (MenuItem item : file.getItems()) {
              if (!(item instanceof SeparatorMenuItem)) item.fire();
            }
            for (MenuItem item : game.getItems()) {
              if (!(item instanceof SeparatorMenuItem)) item.fire();
            }
            latch.countDown();
          });
      latch.await(10, TimeUnit.SECONDS); // attend la fin sur FX thread

      verify(engine).newGame();
      verify(engine).loadGame("save.txt");
      verify(engine).saveGame("save.txt");
      verify(engine).undo();
      verify(engine).redo();
      verify(engine).pause();
      verify(engine).hint();

      for (Menu menu : menuBar.getMenus()) {
        for (MenuItem item : menu.getItems()) {
          if (!(item instanceof SeparatorMenuItem)) {
            assertNotNull(item.getAccelerator());
          }
        }
      }
    }
  }
}
