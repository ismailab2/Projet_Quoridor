package fr.bordeaux.cli.command;

import fr.bordeaux.cli.CliApp;
import fr.bordeaux.cli.command.Command;

import java.io.EOFException;
import java.io.IOException;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.ClosedException;

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
            if (line == null) throw new EndOfFileException();
            if (line.toLowerCase().equals("y") || line.toLowerCase().equals("yes")) {
                String save = reader.readLine("Path where you want to save it >> ");
                if (save == null) throw new EndOfFileException();
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
        catch (EndOfFileException | UserInterruptException e) {
            System.err.println("Aïe j'ai été brusquement arrêté...");
            try { terminal.close(); }
            catch (Exception exc) { exc.printStackTrace(); }
        } finally { return engine; }
    }

    @Override
    public void helpText() {
        System.out.println("Text for quit command");
    }
}
