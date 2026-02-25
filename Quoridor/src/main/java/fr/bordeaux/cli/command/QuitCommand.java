package fr.bordeaux.cli.command;

import fr.bordeaux.cli.command.Command;

import java.io.EOFException;
import java.io.IOException;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import fr.bordeaux.core.GameEngine;

public class QuitCommand implements Command {

    private Terminal terminal;
    private LineReader reader;
    
    public QuitCommand(Terminal t, LineReader r) { terminal = t; reader = r; }
    public QuitCommand() { this(null, null); } 


    @Override
    public GameEngine execute(GameEngine engine) {
        try {
            String line = reader.readLine("Save the game before quitting? [y/N] >> ");
            if (line.toLowerCase().equals("y") || line.toLowerCase().equals("yes")) {
                String save = reader.readLine("Path where you want to save it >> ");
                SaveCommand cmd = new SaveCommand(save);
                return cmd.execute(engine);
            } else {
                if (line.toLowerCase().equals("n") || line.toLowerCase().equals("no")) {
                    System.out.println("You choose to not save...");
                }
                else { System.out.println("Unrecognizable answer... Game not saved."); }
                return engine;
            }
        }
        catch (UserInterruptException e) {
            System.err.println("Tout va bien, je m'arrête !\n");
            return engine;
        }
    }

    @Override
    public void helpText() {
        System.out.println("Text for quit command");
    }
}
