package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;
import main.java.fr.bordeaux.cli.command.Command;

public class HelpCommand implements Command {

    private String[] tokens;
    
    public HelpCommand() { this.tokens = null; }
    public HelpCommand(String[] t) { this.tokens = t; }

    @Override
    public GameEngine execute(GameEngine engine) {
        if (tokens == null || tokens.length == 1) { System.out.println("Pas de commande\n"); }
        else {
            for (int i = 1; i < tokens.length; i++) {
                System.out.println(i + ": " + tokens[i]);
            }
        }
        return engine;
    }

    @Override
    public void helpText() {
        
    }
}
