package fr.bordeaux.cli.command;

import fr.bordeaux.cli.command.*;
import fr.bordeaux.core.GameEngine;

public class SaveCommand implements Command {

    private String[] tokens;

    public SaveCommand(String p) { this(new String[]{"save", p}); }
    public SaveCommand(String[] t) { this.tokens = t; }

    @Override
    public GameEngine execute(GameEngine engine) {
        /*engine.getState().save(this.tokens[1]);*/ return engine;
    }
    @Override
    public void helpText() { System.out.println("To implement"); }
}
