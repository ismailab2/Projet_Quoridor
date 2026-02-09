package fr.bordeaux.cli;

import fr.bordeaux.core.Player;
import main.java.fr.bordeaux.cli.command.Command;
import main.java.fr.bordeaux.cli.command.QuitCommand;
import fr.bordeaux.cli.command.HelpCommand;

import java.io.IOException;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class CliApp {

    private boolean running = true;
    private Terminal terminal;
    private LineReader reader;

    public void run() {
        try {
            terminal = TerminalBuilder.builder().system(true).build();
            reader = LineReaderBuilder.builder().terminal(terminal).build();
            while (running) {
                String line = reader.readLine(">> ");
                fetch(line);
            }
            terminal.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void fetch(String input) {
        System.out.println("Phrase reçu : \"" + input + "\"\n");
        String[] tokens = input.trim().split("\\s+");
        String command = tokens[0].toLowerCase();
        Command cmd = null;
        switch (command) {
            case "help": { cmd = new HelpCommand(tokens); break; }
            case "quit": { cmd = new QuitCommand(terminal, reader); this.running = false; break; }
            default: ;
        }
        cmd.execute();
    }
}
