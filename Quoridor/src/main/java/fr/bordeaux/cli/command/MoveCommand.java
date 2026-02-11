package main.java.fr.bordeaux.cli.command;

import main.java.fr.bordeaux.cli.command.*;
import fr.bordeaux.cli.command.*;
import fr.bordeaux.core.GameEngine;

public class MoveCommand implements Command {

    public MoveCommand() { System.out.println("To implement"); }

    @Override
    public GameEngine execute(GameEngine engine) { System.out.println("To implement"); return engine; }
    @Override
    public void helpText() { System.out.println("To implement"); }
}
