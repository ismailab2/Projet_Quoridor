package main.java.fr.bordeaux.cli.command;

import main.java.fr.bordeaux.cli.command.*;
import fr.bordeaux.cli.command.*;
import fr.bordeaux.core.Board;
import fr.bordeaux.core.GameEngine;
import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;

import java.util.Arrays;
<<<<<<< HEAD
import java.util.List;
=======
>>>>>>> 367e03691fa6ff5ce404f541f003c8b4f07feecd

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class NewCommand implements Command {

    private String[] tokens;
<<<<<<< HEAD
    private Terminal terminal;
    private LineReader reader;

    public NewCommand(String[] t, Terminal term, LineReader r) {
        this.tokens = t;
=======
    private GameEngine engine;
    private Terminal terminal;
    private LineReader reader;

    public NewCommand(String[] t, GameEngine e, Terminal term, LineReader r) {
        this.tokens = t;
        this.engine = e;
>>>>>>> 367e03691fa6ff5ce404f541f003c8b4f07feecd
        this.terminal = term;
        this.reader = r;
    }
    public NewCommand(Terminal t, LineReader r) {
<<<<<<< HEAD
        this(new String[]{"9", "2"}, t, r);
    }
    public NewCommand() { this(null, null, null); }

    @Override
    public GameEngine execute(GameEngine engine) {
        if (tokens.length > 5) { System.out.println("wrong amount of arguments..."); }

        int size = Integer.parseInt(tokens[1]);
        Position[] positions = {
            new Position(0, (size-1)/2), new Position(size-1, (size-1)/2),
            new Position((size-1)/2, 0), new Position((size-1)/2, size-1)
        };
        if (tokens.length == 1) return new GameEngine(new GameState(new Board(9), List.of(new Player("alice", positions[0]), new Player("bob", positions[1]))));
        else {
            Board board = new Board(size);
            Player[] players = new Player[Integer.parseInt(tokens[2])];
            
=======
        this(new String[]{"9", "2"}, null, t, r);
    }
    public NewCommand() { System.out.println("To implement"); }

    @Override
    public GameEngine execute(GameEngine engine) {

        if (tokens.length <= 2) { System.out.println("wrong amount of arguments..."); }
        else {
            int size = Integer.parseInt(tokens[1]);
            Board board = new Board(size);
            Player[] players = new Player[Integer.parseInt(tokens[2])];
            Position[] positions = {
                new Position(0, (size-1)/2), new Position(size-1, (size-1)/2),
                new Position((size-1)/2, 0), new Position((size-1)/2, size-1)
            };
>>>>>>> 367e03691fa6ff5ce404f541f003c8b4f07feecd
            for (int i = 0; i < Integer.parseInt(tokens[2]); i++) {
                String inputName = reader.readLine("Player's name >> ");
                players[i] = new Player(inputName, positions[i]);
            }
            engine = new GameEngine(new GameState(board, Arrays.stream(players).toList()));
        }
        return engine;
    }
    @Override
    public void helpText() { System.out.println("To implement"); }
}
