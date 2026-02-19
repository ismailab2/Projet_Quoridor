package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;
import fr.bordeaux.cli.command.Command;
import fr.bordeaux.cli.command.QuitCommand;
import fr.bordeaux.cli.command.NewCommand;
import fr.bordeaux.cli.command.LoadCommand;
import fr.bordeaux.cli.command.SaveCommand;
import fr.bordeaux.cli.command.PauseCommand;
import fr.bordeaux.cli.command.UndoCommand;
import fr.bordeaux.cli.command.RedoCommand;
import fr.bordeaux.cli.command.ShowCommand;
import fr.bordeaux.cli.command.SetCommand;
import fr.bordeaux.cli.command.*;

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
        if (this.tokens.length <= 1) System.out.println("Un truc à expliquer");
        else {
            Command cmd = null;
            switch (tokens[1]) {
                case "help": { System.out.println("Help command to implement.");; break; }
                case "quit": { cmd = new QuitCommand(); break; }
                case "new": { cmd = new NewCommand(); break; }
                case "load": { cmd = new LoadCommand(); break; }
                case "save": { cmd = new SaveCommand(tokens); break; }
                case "pause": { cmd = new PauseCommand(); break; }
                case "undo": { cmd = new UndoCommand(); break; }
                case "redo": { cmd = new RedoCommand(); break; }
                case "show": { cmd = new ShowCommand(); break; }
                case "set": { cmd = new SetCommand(); break; }
                default: { cmd = null; }
            }
            if (cmd != null) { cmd.helpText(); }
        }
    }
}
