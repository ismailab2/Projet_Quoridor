package fr.bordeaux.gui;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MainMenuBarTest {
  /*
     @BeforeAll
     static void initJavaFX() {
         // Initialize JavaFX platform before creating GUI components
         new JFXPanel();
     }

     // Helper method to create a mocked ShortcutManager returning a dummy shortcut for every Action
     private ShortcutManager mockShortcutManager() {
         ShortcutManager manager = mock(ShortcutManager.class);
         for (Action a : Action.values()) {
             when(manager.getShortcut(a))
                     .thenReturn(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN));
         }
         return manager;
     }

     @Test
     void testMenusStructure() {
         ActionDispatcher dispatcher = mock(ActionDispatcher.class);
         ShortcutManager shortcutManager = mockShortcutManager();

         // Mock the static getInstance() method of ShortcutManager
         try (MockedStatic<ShortcutManager> mocked = mockStatic(ShortcutManager.class)) {
             mocked.when(ShortcutManager::getInstance).thenReturn(shortcutManager);

             ActionDispatcher.MainMenuBar menuBar = new ActionDispatcher.MainMenuBar(dispatcher);

             // Verify that there are exactly 2 menus: File and Game
             assertEquals(2, menuBar.getMenus().size());

             Menu file = menuBar.getMenus().get(0);
             Menu game = menuBar.getMenus().get(1);

             assertEquals("File", file.getText());
             assertEquals("Game", game.getText());

             // Verify the number of menu items
             assertEquals(8, file.getItems().size());
             assertEquals(6, game.getItems().size());

             // Verify that separators are at the correct positions
             assertTrue(file.getItems().get(3) instanceof SeparatorMenuItem);
             assertTrue(file.getItems().get(6) instanceof SeparatorMenuItem);
             assertTrue(game.getItems().get(2) instanceof SeparatorMenuItem);
             assertTrue(game.getItems().get(4) instanceof SeparatorMenuItem);
         }
     }

     @Test
     void testFileMenuActions() {
         ActionDispatcher dispatcher = mock(ActionDispatcher.class);
         ShortcutManager shortcutManager = mockShortcutManager();

         try (MockedStatic<ShortcutManager> mocked = mockStatic(ShortcutManager.class)) {
             mocked.when(ShortcutManager::getInstance).thenReturn(shortcutManager);

             ActionDispatcher.MainMenuBar menuBar = new ActionDispatcher.MainMenuBar(dispatcher);
             Menu file = menuBar.getMenus().get(0);

             // Simulate clicks on File menu items
             file.getItems().get(0).fire(); // New Game
             file.getItems().get(1).fire(); // Load Game
             file.getItems().get(2).fire(); // Save Game
             file.getItems().get(4).fire(); // Configuration
             file.getItems().get(5).fire(); // Info
             file.getItems().get(7).fire(); // Quit

             // Verify that the dispatcher was called with the correct Action
             verify(dispatcher).dispatch(Action.NEW_GAME);
             verify(dispatcher).dispatch(Action.LOAD_GAME);
             verify(dispatcher).dispatch(Action.SAVE_GAME);
             verify(dispatcher).dispatch(Action.CONFIGURATION);
             verify(dispatcher).dispatch(Action.INFO);
             verify(dispatcher).dispatch(Action.QUIT);
         }
     }

     @Test
     void testGameMenuActions() {
         ActionDispatcher dispatcher = mock(ActionDispatcher.class);
         ShortcutManager shortcutManager = mockShortcutManager();

         try (MockedStatic<ShortcutManager> mocked = mockStatic(ShortcutManager.class)) {
             mocked.when(ShortcutManager::getInstance).thenReturn(shortcutManager);

             ActionDispatcher.MainMenuBar menuBar = new ActionDispatcher.MainMenuBar(dispatcher);
             Menu game = menuBar.getMenus().get(1);

             // Simulate clicks on Game menu items
             game.getItems().get(0).fire();
             game.getItems().get(1).fire();
             game.getItems().get(3).fire();
             game.getItems().get(5).fire();

             // Verify that the dispatcher was called with the correct Action
             verify(dispatcher).dispatch(Action.UNDO);
             verify(dispatcher).dispatch(Action.REDO);
             verify(dispatcher).dispatch(Action.PAUSE);
             verify(dispatcher).dispatch(Action.HINT);
         }
     }

     @Test
     void testMenuItemAccelerators() {
         ActionDispatcher dispatcher = mock(ActionDispatcher.class);
         ShortcutManager shortcutManager = mockShortcutManager();

         try (MockedStatic<ShortcutManager> mocked = mockStatic(ShortcutManager.class)) {
             mocked.when(ShortcutManager::getInstance).thenReturn(shortcutManager);

             ActionDispatcher.MainMenuBar menuBar = new ActionDispatcher.MainMenuBar(dispatcher);

             // Verify that every MenuItem (except separators) has a keyboard shortcut assigned
             for (Menu menu : menuBar.getMenus()) {
                 for (MenuItem item : menu.getItems()) {
                     if (!(item instanceof SeparatorMenuItem)) {
                         assertNotNull(item.getAccelerator());
                     }
                 }
             }
         }
     }

  */
}
