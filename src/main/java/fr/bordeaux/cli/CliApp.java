package fr.bordeaux.cli;

import fr.bordeaux.cli.command.*;
import fr.bordeaux.core.*;
import java.io.IOException;
import java.util.List;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class CliApp {

  private GameEngine engine;
  private Terminal terminal;
  private LineReader reader;
  private boolean running = true;

  /**
   * Creates a CLI application with a specific game engine, terminal and reader.
   *
   * @param e the game engine used to run the game
   * @param t the terminal used for input/output
   * @param r the line reader used to read user commands
   */
  public CliApp(GameEngine engine, Terminal terminal, LineReader reader) {
    this.engine = engine;
    this.terminal = terminal;
    this.reader = reader;
    reader =
        LineReaderBuilder.builder()
            .terminal(terminal)
            .completer(
                new StringsCompleter(
                    "help", "quit", "new", "save", "load", "show", "hint", "pause", "undo", "redo",
                    "set"))
            .build();
  }

  /**
   * Creates a CLI application with a default game configuration. Initializes a board and two human
   * players positioned on it.
   */
  public CliApp() {
    this(
        new GameEngine(
            new GameState(
                new Board(9),
                List.of(
                    new HumanPlayer("Human", new Position(0, 4)),
                    new HumanPlayer("Bob", new Position(8, 4))))));
  }

  /**
   * Indicates whether the CLI application is still running.
   *
   * @return true if the application loop is active, false otherwise
   */
  public boolean isRunning() {
    return running;
  }

  /**
   * Returns the current game engine used by the CLI.
   *
   * @return the current GameEngine instance
   */
  public GameEngine getEngine() {
    return engine;
  }

  /**
   * Creates a CLI application using the provided game engine. Initializes the terminal, command
   * auto-completion and line reader.
   *
   * @param e the game engine used to run the game
   */
  public CliApp(GameEngine engine) {
    try {
      terminal = TerminalBuilder.builder().system(true).build();
      reader =
          LineReaderBuilder.builder()
              .terminal(terminal)
              .completer(
                  new StringsCompleter(
                      "help", "quit", "new", "save", "load", "show", "hint", "pause", "undo",
                      "redo", "set"))
              .build();
    } catch (Exception exc) {
      exc.printStackTrace();
    }
    this.engine = engine;
  }

  /**
   * Starts the CLI loop. Displays the board at startup and continuously reads user commands until
   * the application is stopped.
   */
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

  /**
   * Parses the user input and executes the corresponding command. If the command is not recognized,
   * it is interpreted as a move.
   *
   * @param input the raw command entered by the user
   */
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
