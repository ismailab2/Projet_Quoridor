package fr.bordeaux.gui;

import fr.bordeaux.core.GameEngine;
import javafx.application.Platform;

/**
 * @class ActionDispatcher
 * @brief Dispatches actions from the GUI to the GameEngine or other dialogs.
 *     <p>This class serves as a bridge between GUI events (menu items, shortcuts) and the
 *     underlying game logic or dialogs. Each action corresponds to a method in the GameEngine or a
 *     GUI dialog.
 */
public class ActionDispatcher {

  /** Reference to the game engine to execute game-related actions */
  private final GameEngine engine;

  /**
   * @brief Constructs an ActionDispatcher with a given GameEngine.
   * @param engine The GameEngine instance to control the game.
   */
  public ActionDispatcher(GameEngine engine) {
    this.engine = engine;
  }

  /**
   * @brief Dispatches a given action to the appropriate handler.
   *     <p>Depending on the action type, this method may: - Call GameEngine methods (newGame,
   *     loadGame, saveGame, undo, redo, pause, hint) - Show GUI dialogs (configuration, info) -
   *     Exit the application
   * @param action The Action enum representing the user command.
   */
  public void dispatch(Action action) {
    switch (action) {
      case NEW_GAME -> engine.newGame();
      case LOAD_GAME -> engine.loadGame("txt.txt");
      case SAVE_GAME -> engine.saveGame("txt.txt");
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
