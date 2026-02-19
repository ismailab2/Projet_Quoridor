package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;

public interface Command {
    GameEngine execute(GameEngine engine);
    void helpText();
}
