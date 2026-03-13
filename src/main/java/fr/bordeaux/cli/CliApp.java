package fr.bordeaux.cli;

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

public class CliApp {

  private GameEngine engine;
  private boolean running = true;
  private Terminal terminal;
  private LineReader reader;
  private Completer completer;

  public CliApp(GameEngine e, Terminal t, LineReader r) {
    engine = e;
    terminal = t;
    reader = r;
    completer =
        new StringsCompleter(
            "help", "quit", "new", "save", "load", "show", "hint", "pause", "undo", "redo", "set");
    reader = LineReaderBuilder.builder().terminal(terminal).completer(completer).build();
  }

  public CliApp() {
    this(
        new GameEngine(
            new GameState(
                new Board(9),
                List.of(
                    new HumanPlayer("Human", new Position(0, 4)),
                    new HumanPlayer("Bob", new Position(8, 4))))));
  }

  public boolean isRunning() {
    return running;
  }

  public GameEngine getEngine() {
    return engine;
  }

  public CliApp(GameEngine e) {
    try {
      terminal = TerminalBuilder.builder().system(true).build();
      completer =
          new StringsCompleter(
              "help",
              "quit",
              "new",
              "save",
              "load",
              "show board",
              "show time",
              "show configurations",
              "show history",
              "hint",
              "pause",
              "undo",
              "redo",
              "set");
      reader = LineReaderBuilder.builder().terminal(terminal).completer(completer).build();
    } catch (Exception exc) {
      exc.printStackTrace();
    }
    this.engine = e;
  }

  public void run() {
    if (engine != null) {
      ShowCommand show = new ShowCommand(new String[] {"show", "board"});
      show.execute(engine);
    }
    while (running) {
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
          cmd = new QuitCommand(reader);
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
          cmd = new LoadCommand(tokens);
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
      case "hint":
        {
          cmd = new HintCommand();
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
          cmd = new ShowCommand(tokens);
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
  }
}
