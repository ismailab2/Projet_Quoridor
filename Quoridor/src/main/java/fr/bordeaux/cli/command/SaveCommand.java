package main.java.fr.bordeaux.cli.command;

import main.java.fr.bordeaux.cli.command.*;
import fr.bordeaux.cli.command.*;
import fr.bordeaux.core.GameEngine;

public class SaveCommand implements Command {

    private String path;

    public SaveCommand() { this(null); }
    public SaveCommand(String p) { this.path = p; }

    @Override
    public GameEngine execute(GameEngine engine) { System.out.println("To implement"); return engine; }
    @Override
    public void helpText() { System.out.println("To implement"); }
}
