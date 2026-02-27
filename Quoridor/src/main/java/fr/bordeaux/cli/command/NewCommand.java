package fr.bordeaux.cli.command;

import fr.bordeaux.cli.CliApp;
import fr.bordeaux.cli.command.*;
import fr.bordeaux.core.Board;
import fr.bordeaux.core.GameEngine;
import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;
import fr.bordeaux.core.TypePlayer;

import java.util.Arrays;
import java.util.List;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class NewCommand implements Command {

    private String[] tokens;
    private Terminal terminal;
    private LineReader reader;

    public NewCommand(String[] t, Terminal term, LineReader r) {
        this.tokens = t;
        this.terminal = term;
        this.reader = r;
    }
    public NewCommand(Terminal t, LineReader r) {
        this(new String[]{"9", "2"}, t, r);
    }
    public NewCommand() { this(null, null, null); }

    @Override
    public GameEngine execute(GameEngine engine) {
        if (tokens.length > 5) { System.out.println("wrong amount of arguments..."); }
        int size = tokens.length >= 2 ? Integer.parseInt(tokens[1]):9;
        Position[] positions = {
            new Position(0, (size-1)/2), new Position(size-1, (size-1)/2),
            new Position((size-1)/2, 0), new Position((size-1)/2, size-1)
        };
        if (tokens.length == 1) return new GameEngine(new GameState(new Board(size), List.of(new Player("Human", new Position(0, 4), TypePlayer.HUMAN), new Player("Bob", new Position(8, 4), TypePlayer.HUMAN))));
        else {
            int nbPlayer = tokens.length <= 2 ? 2:Integer.parseInt(tokens[2]);
            Board board = new Board(size);
            Player[] players = new Player[nbPlayer];
            
            for (int i = 0; i < nbPlayer; i++) {
                try {
                    String typePlayer = reader.readLine("Player's type ([h]uman/ia) >> ");
                    if (typePlayer == null) throw new EndOfFileException();
                    if (!typePlayer.equals("h".getBytes()) || !typePlayer.equals("ia".getBytes())) { 
                        System.out.println("Wrong type, please enter h or ia"); i--;
                        continue;
                    }
                    String inputName = reader.readLine("Player's name >> ");
                    if (inputName == null) throw new EndOfFileException();
                    players[i] = new Player(inputName, positions[i], typePlayer.equals("h")?TypePlayer.HUMAN:TypePlayer.IA);
                } 
                catch (EndOfFileException | UserInterruptException e) {
                    System.err.println("Tout va bien, je m'arrête !\n");
                    try {
                        terminal.close();
                    } catch (Exception exc) {
                        System.err.println("Terminal fermé");
                    }
                } finally { return engine; }
            }
            engine = new GameEngine(new GameState(board, Arrays.stream(players).toList()));
        }
        return engine;
    }
    @Override
    public void helpText() { System.out.println("To implement"); }
}
