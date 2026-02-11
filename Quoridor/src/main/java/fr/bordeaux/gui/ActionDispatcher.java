package fr.bordeaux.gui;


import fr.bordeaux.core.GameEngine;
import javafx.application.Platform;

public final class ActionDispatcher {
    private ActionDispatcher() {}

    public static void dispatch(Action action) {
        switch (action) {
            case NEW_GAME -> GameEngine.startGame();
            case LOAD_GAME -> GameEngine.loadGame();
            case SAVE_GAME -> GameEngine.saveGame();
            case UNDO -> GameEngine.undo();
            case REDO -> GameEngine.redo();
            case PAUSE -> GameEngine.pause();
            case HINT -> GameEngine.hint();
            case CONFIGURATION -> ConfigDialog.show();
            case INFO -> InfoDialog.show();
            case QUIT -> Platform.exit();
        }
    }
}
