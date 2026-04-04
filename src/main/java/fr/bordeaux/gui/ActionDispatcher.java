package fr.bordeaux.gui;

import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.core.GameEngine;
import java.io.IOException;
import javafx.application.Platform;

/**
 * Dispatches actions from the GUI to the GameEngine or other dialogs.
 *
 * <p>This class serves as a bridge between GUI events (menu items, shortcuts) and the underlying
 * game logic or dialogs. Each action corresponds to a method in the GameEngine or a GUI dialog.
 */
public class ActionDispatcher {

  /** Reference to the game engine to execute game-related actions */
  private final GameEngine engine;

  private final FileHandler fileHandler;

  private final ConfigManager configManager = ConfigManager.getInstance();

  /**
   * Constructs an ActionDispatcher with a given GameEngine.
   *
   * @param engine The GameEngine instance to control the game.
   * @param fileHandler The FileHandler used to save and load.
   */
  public ActionDispatcher(GameEngine engine, FileHandler fileHandler) {
    this.engine = engine;
    this.fileHandler = fileHandler;
  }

  /**
   * Dispatches a given action to the appropriate handler.
   *
   * <p>Depending on the action type, this method may: - Call GameEngine methods (newGame, loadGame,
   * saveGame, undo, redo, pause, hint) - Show GUI dialogs (configuration, info) - Exit the
   * application
   *
   * @param action The Action enum representing the user command.
   * @throws IOException If an I/O error occurs during command execution.
   */
  public void dispatch(Action action) throws IOException {
    switch (action) {
      case NEW_GAME -> engine.newGame(configManager);
      case LOAD_GAME -> {
        String path = fileHandler.chooseLoadFile();
        if (path != null) engine.loadGame(path);
      }
      case SAVE_GAME -> {
        String path = fileHandler.chooseSaveFile();
        if (path != null) engine.saveGame(path);
      }
      case CONFIGURATION -> ConfigDialog.show();
      case INFO -> InfoDialog.show();
      case REDO -> engine.redo();
      case UNDO -> engine.undo();
      case PAUSE -> engine.pause();
      case HINT -> engine.hint();
      case QUIT -> Platform.exit();
    }
  }
}
