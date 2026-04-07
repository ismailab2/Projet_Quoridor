package fr.bordeaux.gui;

import java.io.IOException;

import fr.bordeaux.i18n.I18n;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

/** Represents the main menu bar of the application. */
public class MainMenuBar extends MenuBar {
  /** Dispatcher used to execute selected menu actions. */
  private final ActionDispatcher dispatcher;

  /**
   * Creates the main menu bar and binds its actions to the dispatcher.
   *
   * @param dispatcher dispatcher used to handle menu actions
   */
  public MainMenuBar(ActionDispatcher dispatcher) {
    this.dispatcher = dispatcher;
    getMenus().addAll(createFileMenu(), createGameMenu());
  }

  private Menu createFileMenu() {
    Menu file = new Menu(I18n.get("file"));
    file.getItems()
        .addAll(
            menuItem(I18n.get("newGame"), Action.NEW_GAME),
            menuItem(I18n.get("loadGame"), Action.LOAD_GAME),
            menuItem(I18n.get("saveGame"), Action.SAVE_GAME),
            new SeparatorMenuItem(),
            menuItem("Configuration", Action.CONFIGURATION),
            menuItem("Info", Action.INFO),
            new SeparatorMenuItem(),
            menuItem(I18n.get("menuQuit"), Action.QUIT));
    return file;
  }

  private Menu createGameMenu() {
    Menu game = new Menu(I18n.get("menuGame"));
    game.getItems()
        .addAll(
            menuItem(I18n.get("menuUndo"), Action.UNDO),
            menuItem(I18n.get("menuRedo"), Action.REDO),
            new SeparatorMenuItem(),
            menuItem("Pause", Action.PAUSE),
            menuItem(I18n.get("menuHint"), Action.HINT));
    return game;
  }

  private MenuItem menuItem(String text, Action action) {
    MenuItem item = new MenuItem(text);
    item.setOnAction(
        event -> {
          try {
            dispatcher.dispatch(action);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });

    item.setAccelerator(ShortcutManager.getInstance().getShortcut(action));
    return item;
  }
}
