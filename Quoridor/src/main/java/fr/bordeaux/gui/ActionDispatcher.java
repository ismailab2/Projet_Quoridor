package fr.bordeaux.gui;

import fr.bordeaux.core.*;
import javafx.application.Platform;

public class ActionDispatcher {

  private final GameEngine engine;

  public ActionDispatcher(GameEngine engine) {
    this.engine = engine;
  }

  public void dispatch(Action action) {
    switch (action) {
      case NEW_GAME -> engine.newGame();
      case LOAD_GAME -> engine.loadGame("save.json");
      case SAVE_GAME -> engine.saveGame("save.json");
      case PAUSE -> engine.pause();
      case UNDO -> engine.undo();
      case REDO -> engine.redo();
      case CONFIGURATION -> ConfigDialog.show();
      case INFO -> InfoDialog.show();
      case QUIT -> Platform.exit();
      case HINT -> engine.hint();
    }
  }
}
