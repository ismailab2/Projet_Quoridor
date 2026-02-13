package main.java.fr.bordeaux.cli.command;

import main.java.fr.bordeaux.cli.command.*;

import java.io.StringReader;

import fr.bordeaux.cli.command.*;
import fr.bordeaux.core.GameEngine;

public class RedoCommand implements Command {

    private String[] tokens;

    public RedoCommand() { System.out.println("To implement"); }
    public RedoCommand(String[] t) { this.tokens = t; }

    @Override
    public GameEngine execute(GameEngine engine) { 
        if (tokens.length == 1) engine.getState().redo();
        else if (tokens.length >= 3) {
            System.out.println("Redo command should look like : redo [N] with N an integer");
        }
        for (int i = 0; i < Integer.parseInt(tokens[1]); i++) engine.getState().redo();
        return engine;
    }
    @Override
    public void helpText() { System.out.println("To implement"); }
}
