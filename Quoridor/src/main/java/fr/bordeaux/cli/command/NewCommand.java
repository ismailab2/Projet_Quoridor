package fr.bordeaux.cli.command;

import fr.bordeaux.AI.MCTSPlayer;
import fr.bordeaux.AI.MinimaxPlayer;
import fr.bordeaux.AI.NeuralNetPlayer;
import fr.bordeaux.AI.RandomPlayer;
import fr.bordeaux.cli.CliApp;
import fr.bordeaux.cli.command.*;
import fr.bordeaux.core.Board;
import fr.bordeaux.core.GameEngine;
import fr.bordeaux.core.GameState;
import fr.bordeaux.core.HumanPlayer;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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
        if (tokens.length == 1) return new GameEngine(new GameState(new Board(size), List.of(new HumanPlayer("Human", new Position(0, 4)), new HumanPlayer("Bob", new Position(8, 4)))));
        else {
            int nbPlayer = tokens.length <= 2 ? 2:Integer.parseInt(tokens[2]);
            Board board = new Board(size);
            Player[] players = new Player[nbPlayer];
            
            for (int i = 0; i < nbPlayer; i++) {
                try {
                    System.out.println("Player's type available :");
                    System.out.println("- [h]uman  - [mcts] Monte Carlo ia  - [mini] Minimax ia  - [nn] NeuralNetwork ia  - [r]andom ia");
                    String typePlayer = reader.readLine("Player's type >> ");
                    if (typePlayer == null) throw new EndOfFileException();
                    if (!typePlayer.equals("h") && !typePlayer.equals("mcts") && !typePlayer.equals("mini") && !typePlayer.equals("nn") && !typePlayer.equals("r")) { 
                        System.out.println("Wrong type, please enter h / mcts / mini / nn / r"); i--;
                        continue;
                    }
                    String inputName = reader.readLine("Player's name >> ");
                    if (inputName == null) throw new EndOfFileException();
                    switch (typePlayer) {
                        case "h": {
                            players[i] = new HumanPlayer(inputName, positions[i]);
                            break;
                        }
                        case "mcts": {
                            players[i] = new MCTSPlayer(inputName, positions[i]);
                            break;
                        }
                        case "mini": {
                            players[i] = new MinimaxPlayer(inputName, positions[i], 1);
                            break;
                        }
                        case "nn": {
                            players[i] = new NeuralNetPlayer(inputName, positions[i]);
                            break;
                        }
                        default: {
                            players[i] = new RandomPlayer(inputName, positions[i]);
                            break;
                        }
                    }
                } 
                catch (EndOfFileException | UserInterruptException e) {
                    System.err.println("Tout va bien, je m'arrête !\n");
                    try {
                        terminal.close();
                    } catch (Exception exc) {
                        System.err.println("Terminal fermé");
                        return engine;
                    }
                }
            }
            engine = new GameEngine(new GameState(board, Arrays.stream(players).toList()));
        }
        return engine;
    }
    @Override
    public void helpText() {
        System.out.println("=== NEW Command ===\n");
        System.out.println("Usage:");
        System.out.println("  new");
        System.out.println("  new [boardSize] [nbPlayer]\n");
        System.out.println("Description:");
        System.out.println("  Starts a new game.\n");
        System.out.println("Arguments (optional):");
        System.out.println("  boardSize    : An odd integer greater than or equal to 3.");
        System.out.println("  playerCount  : Must be either 2 or 4.\n");
        System.out.println("If no arguments are provided, default settings are used.\n");
        System.out.println("Examples:");
        System.out.println("  new       -> 9x9 board with 2 players");
        System.out.println("  new 5     -> 5x5 board with 2 players");
        System.out.println("  new 7 4   -> 7x7 board with 4 players\n");
    }
}
