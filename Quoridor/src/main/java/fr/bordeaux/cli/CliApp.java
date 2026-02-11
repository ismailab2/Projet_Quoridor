package fr.bordeaux.cli;

import fr.bordeaux.core.Board;
import fr.bordeaux.core.GameEngine;
import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;
import main.java.fr.bordeaux.cli.command.Command;
import main.java.fr.bordeaux.cli.command.NewCommand;
import main.java.fr.bordeaux.cli.command.QuitCommand;
import fr.bordeaux.cli.command.HelpCommand;

import java.io.IOException;
import java.util.List;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class CliApp {

    private GameEngine engine;
    private boolean running = true;
    private Terminal terminal;
    private LineReader reader;

    public CliApp(GameEngine e) {
        try {
            terminal = TerminalBuilder.builder().system(true).build();
            reader = LineReaderBuilder.builder().terminal(terminal).build();
        }
        catch (Exception exc) { exc.printStackTrace(); }
        this.engine = e;
    }
    public CliApp() {
        this(null);
        // this(new GameEngine(new GameState(new Board(9), List.of(new Player("Alice", new Position(0, 4)), new Player("Bob", new Position(8, 4))))));
    }

    public void run() {
        System.out.println("Launching Command Line interface...");
        while (running) {
            System.out.println(engine.getState().getBoard().getSize());
            if (engine != null) printBoard();
            String line = reader.readLine(">> ");
            fetch(line);
        }
        try { terminal.close(); } 
        catch (Exception e) { e.printStackTrace(); }
    }

    private void fetch(String input) {
        System.out.println("Phrase reçu : \"" + input + "\"\n");
        String[] tokens = input.trim().split("\\s+");
        String command = tokens[0].toLowerCase();
        Command cmd = null;
        switch (command) {
            case "help": { cmd = new HelpCommand(tokens); break; }
            case "quit": { cmd = new QuitCommand(terminal, reader); this.running = false; break; }
            case "new": { cmd = new NewCommand(tokens, engine, terminal, reader); break; }
            default: System.out.println("Unknown command : " + command);
        }
        if (cmd != null) engine = cmd.execute(engine);
    }

    private void printBoard() {
        for (int i = 0; i < engine.getState().getBoard().getSize(); i++) {
            boolean empty = true;
            for (int j = 0; j < engine.getState().getBoard().getSize(); j++) {
                int id = 1;
                for (Player player : engine.getState().getPlayers()) {
                    if ((player.getPosition()).equals(new Position(i, j)))
                        { System.out.print(id + " "); empty = false; }
                    id += 1;
                } if (empty) System.out.print("_ ");
                empty = true;
            }
            if (i == engine.getState().getBoard().getSize()-1) break;
            System.out.println("");
            for (int j = 0; j < engine.getState().getBoard().getSize(); j++)
                System.out.print(". ");
            System.out.println("");
        }
        System.out.println("");
    }
}
