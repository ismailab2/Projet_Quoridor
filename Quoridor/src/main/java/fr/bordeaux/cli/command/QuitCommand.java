package main.java.fr.bordeaux.cli.command;

import main.java.fr.bordeaux.cli.command.Command;

import java.io.IOException;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class QuitCommand implements Command {

    private Terminal terminal;
    private LineReader reader;
    
    public QuitCommand(Terminal t, LineReader r) { terminal = t; reader = r; }

    @Override
    public void execute() {
        String line = reader.readLine("Save the game before quitting? [y/N] >> ");
        if (line.toLowerCase().equals("y") || line.toLowerCase().equals("yes")) {
            String save = reader.readLine("Path where you want to save it >> ");
            return;
        } else {
            System.out.println("Unrecognizable answer... Game not saved.");
            try { terminal.close(); }
            catch (IOException e) { e.printStackTrace(); }
            return;
        }
    }
}
