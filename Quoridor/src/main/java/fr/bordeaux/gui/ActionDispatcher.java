package fr.bordeaux.gui;

import fr.bordeaux.core.*;
import javafx.application.Platform;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

public class ActionDispatcher {

  private final GameEngine engine;

  public ActionDispatcher(GameEngine engine) {
    this.engine = engine;
  }

  public void dispatch(Action action) {
    switch (action) {
      case NEW_GAME -> engine.newGame();
      case LOAD_GAME -> engine.loadGame("save.txt");
      case SAVE_GAME -> engine.saveGame("save.txt");
      case PAUSE -> engine.pause();
      case UNDO -> engine.undo();
      case REDO -> engine.redo();
      case CONFIGURATION -> ConfigDialog.show();
      case INFO -> InfoDialog.show();
      case QUIT -> Platform.exit();
      case HINT -> engine.hint();
    }
  }

    public static class MainMenuBar extends MenuBar {

        private final ActionDispatcher dispatcher;

        public MainMenuBar(ActionDispatcher dispatcher) {
            this.dispatcher = dispatcher;
            getMenus().addAll(createFileMenu(), createGameMenu());
        }

        private Menu createFileMenu() {
            Menu file = new Menu("File");
            file.getItems()
                    .addAll(
                            menuItem("New Game", Action.NEW_GAME),
                            menuItem("Load Game", Action.LOAD_GAME),
                            menuItem("Save Game", Action.SAVE_GAME),
                            new SeparatorMenuItem(),
                            menuItem("Configuration", Action.CONFIGURATION),
                            menuItem("Info", Action.INFO),
                            new SeparatorMenuItem(),
                            menuItem("Quit", Action.QUIT));
            return file;
        }

        private Menu createGameMenu() {
            Menu game = new Menu("Game");
            game.getItems()
                    .addAll(
                            menuItem("Undo", Action.UNDO),
                            menuItem("Redo", Action.REDO),
                            new SeparatorMenuItem(),
                            menuItem("Pause", Action.PAUSE),
                            new SeparatorMenuItem(),
                            menuItem("Hint", Action.HINT));
            return game;
        }

        private MenuItem menuItem(String label, Action action) {
            MenuItem item = new MenuItem(label);
            item.setOnAction(e -> dispatcher.dispatch(action));
            item.setAccelerator(ShortcutManager.getInstance().getShortcut(action));
            return item;
        }
    }
}
