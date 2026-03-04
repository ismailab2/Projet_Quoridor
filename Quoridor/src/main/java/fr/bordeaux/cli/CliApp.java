package fr.bordeaux.cli;

import fr.bordeaux.core.Board;
import fr.bordeaux.core.GameEngine;
import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;
import fr.bordeaux.graph.Graph;
import fr.bordeaux.AI.MinimaxPlayer;
import fr.bordeaux.cli.command.*;
import fr.bordeaux.core.*;
import java.io.IOException;
import java.util.List;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.reader.Completer;
import org.jline.reader.EndOfFileException;
import org.jline.reader.impl.completer.StringsCompleter;

public class CliApp {

  private GameEngine engine;
  private boolean running = true;
  private Terminal terminal;
  private LineReader reader;
  private Completer completer;

    public CliApp(GameEngine e, Terminal t, LineReader r) {
        engine = e; terminal = t; reader = r;
        completer = new StringsCompleter("help", "quit", "new", "save", "load", "show", "hint", "pause", "undo", "redo", "set");
        reader = LineReaderBuilder.builder().terminal(terminal).completer(completer).build();
    }
    public CliApp() {
        this(new GameEngine(new GameState(new Board(9), List.of(new HumanPlayer("Human", new Position(0, 4)), new MinimaxPlayer("Bob", new Position(8, 4))))));
    }
    public boolean isRunning() { return running; }
    public GameEngine getEngine() { return engine; }

    public CliApp(GameEngine e) {
      try {
      terminal = TerminalBuilder.builder().system(true).build();
      completer =
        new StringsCompleter(
                "help", "quit", "new", "save", "load", "show", "hint", "pause", "undo", "redo",
                "set");
        reader = LineReaderBuilder.builder().terminal(terminal).completer(completer).build();
      } catch (Exception exc) {
        exc.printStackTrace();
      }
      this.engine = e;
    }

  public void run() {
    System.out.println("Launching Command Line interface...");
    while (running) {
      if (engine != null) printBoard();
      String line = reader.readLine(">> ");
      fetch(line);
    }
    try {
      terminal.close();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void fetch(String input) {
    String[] tokens = input.trim().split("\\s+");
    String command = tokens[0].toLowerCase();
    Command cmd = null;
    switch (command) {
      case "help":
        {
          cmd = new HelpCommand(tokens);
          break;
        }
      case "quit":
        {
          cmd = new QuitCommand(terminal, reader);
          this.running = false;
          break;
        }
      case "new":
        {
          cmd = new NewCommand(tokens, terminal, reader);
          break;
        }
      case "load":
        {
          cmd = new LoadCommand();
          break;
        }
      case "save":
        {
          cmd = new SaveCommand(tokens);
          break;
        }
      case "pause":
        {
          cmd = new PauseCommand();
          break;
        }
      case "undo":
        {
          cmd = new UndoCommand(tokens);
          break;
        }
      case "redo":
        {
          cmd = new RedoCommand(tokens);
          break;
        }
      case "show":
        {
          cmd = new ShowCommand();
          break;
        }
      case "set":
        {
          cmd = new SetCommand();
          break;
        }
      default:
        cmd = new MoveCommand(input);
    }
    if (cmd != null) engine = cmd.execute(engine);
    for (Move m : engine.getState().getMovesPlayed()) {
      System.out.println(m.toString());
    }
  }

  private void printBoard() {
    int boardSize = engine.getState().getBoard().getSize();
    Board b = engine.getState().getBoard();
    for (int i = 0; i < boardSize; i++) System.out.print(((char) ('A' + i)) + " ");
    System.out.println("");
    for (int i = 0; i < boardSize; i++) {
      boolean empty = true;
      for (int j = 0; j < boardSize; j++) {
        int id = 1;
        for (Player player : engine.getState().getPlayers()) {
          if ((player.getPosition()).equals(new Position(i, j))) {
            System.out.print(id + printWall(b, i, j, false));
            empty = false;
          }
          id += 1;
        }
        if (empty) System.out.print("_" + ((j != boardSize - 1) ? printWall(b, i, j, false) : " "));
        empty = true;
        if (j == boardSize - 1) System.out.print((i + 1));
      }
      if (i == boardSize - 1) break;
      System.out.println("");
      for (int j = 0; j < boardSize; j++) {
        System.out.print(printWall(b, i, j, true));
      }
      System.out.println("");
    }
    System.out.println("");
  }

  private String printWall(Board b, int i, int j, boolean under) {
    if (under) {
      if (i >= b.getSize() - 1) return "X ";
      if (b.getGraph().hasEdge(b.id(i, j), b.id(i + 1, j))) return ". ";
      else return "X ";
    } else {
      if (j >= b.getSize() - 1) return "X";
      if (b.getGraph().hasEdge(b.id(i, j), b.id(i, j + 1))) return " ";
      else return "X";
    }
  }
}
