package fr.bordeaux.cli;

import fr.bordeaux.cli.command.*;
import fr.bordeaux.core.*;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import org.jline.reader.*;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class CliApp {

  private GameEngine engine;
  private Terminal terminal;
  private LineReader reader;
  private boolean running = true;
  private boolean firstTab = true;
  private Map<String, Function<String[], Command>> commandRegistry;

  /**
   * Creates a CLI application with a specific game engine, terminal and reader.
   *
   * @param engine the game engine used to run the game
   * @param terminal the terminal used for input/output
   * @param reader the line reader used to read user commands
   */
  public CliApp(final GameEngine engine, final Terminal terminal, final LineReader reader) {
    this.engine = engine;
    this.terminal = terminal;
    this.reader = reader;
    commandRegistry = new HashMap<>();

    // Register commands
    commandRegistry.put("help", HelpCommand::new);
    commandRegistry.put(
        "quit",
        tokens -> {
          this.running = false;
          return new QuitCommand(reader);
        });
    commandRegistry.put("new", tokens -> new NewCommand(tokens, terminal, reader));
    commandRegistry.put("load", LoadCommand::new);
    commandRegistry.put("save", SaveCommand::new);
    commandRegistry.put("pause", tokens -> new PauseCommand());
    commandRegistry.put("hint", tokens -> new HintCommand());
    commandRegistry.put("undo", UndoCommand::new);
    commandRegistry.put("redo", RedoCommand::new);
    commandRegistry.put("show", ShowCommand::new);
    commandRegistry.put("set", tokens -> new SetCommand());
  }

  /**
   * Creates a CLI application using the provided game engine. Initializes the terminal, command
   * auto-completion and line reader.
   *
   * @param engine the game engine used to run the game
   */
  public CliApp(final GameEngine engine) throws IOException {
    // Initialisation du terminal
    this.terminal = TerminalBuilder.builder().system(true).build();
    this.engine = engine;
    this.running = true;

    // Création du Completer intelligent
    Completer smartCompleter =
        (reader, line, candidates) -> {
          String buffer = line.line();
          String[] tokens = buffer.trim().split("\\s+");
          if (tokens.length == 0 || buffer.isEmpty()) {
            firstTab = true; // reset si vide
            return;
          }

          List<String> mainCommands =
              List.of(
                  "help", "quit", "new", "save", "load", "show", "hint", "pause", "undo", "redo",
                  "set");
          List<String> showOptions = List.of("board", "time", "configurations", "history");

          List<String> matches;

          // Sous-commandes "show"
          if (tokens[0].equals("show") && tokens.length == 2) {
            matches = showOptions.stream().filter(opt -> opt.startsWith(tokens[1])).toList();
          } else {
            matches = mainCommands.stream().filter(cmd -> cmd.startsWith(tokens[0])).toList();
          }

          if (matches.isEmpty()) {
            try {
              reader.getTerminal().writer().print("\007"); // bip
              reader.getTerminal().flush();
            } catch (Exception ignored) {
            }
            firstTab = true;
          } else if (matches.size() == 1) {
            candidates.add(new Candidate(matches.get(0)));
            firstTab = true;
          } else {
            if (firstTab) {
              try {
                Toolkit.getDefaultToolkit().beep(); // bip cross-platform
              } catch (Exception ignored) {
              }
              firstTab = false;
            } else {
              // Second tab → proposer la liste
              matches.forEach(m -> candidates.add(new Candidate(m)));
              firstTab = true; // reset après affichage
            }
          }
        };

    // Création du LineReader avec le Completer
    this.reader =
        LineReaderBuilder.builder().terminal(this.terminal).completer(smartCompleter).build();

    // Initialisation du registre de commandes
    this.commandRegistry = new HashMap<>();
    commandRegistry.put("help", HelpCommand::new);
    commandRegistry.put(
        "quit",
        tokens -> {
          this.running = false;
          return new QuitCommand(reader);
        });
    commandRegistry.put("new", tokens -> new NewCommand(tokens, terminal, reader));
    commandRegistry.put("load", LoadCommand::new);
    commandRegistry.put("save", SaveCommand::new);
    commandRegistry.put("pause", tokens -> new PauseCommand());
    commandRegistry.put("hint", tokens -> new HintCommand());
    commandRegistry.put("undo", UndoCommand::new);
    commandRegistry.put("redo", RedoCommand::new);
    commandRegistry.put("show", ShowCommand::new);
    commandRegistry.put("set", tokens -> new SetCommand());
  }

  /**
   * Creates a CLI application with a default game configuration. Initializes a board and two human
   * players positioned on it.
   */
  public CliApp() throws IOException {
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
    }
  }

  /**
   * Parses the user input and executes the corresponding command. If the command is not recognized,
   * it is interpreted as a move.
   *
   * @param input the raw command entered by the user
   */
  public void fetch(String input) {
    firstTab = true; // reset Tab state
    String[] tokens = input.trim().split("\\s+");
    String command = tokens[0].toLowerCase(Locale.US);
    Command cmd;
    if (commandRegistry.containsKey(command)) {
      cmd = commandRegistry.get(command).apply(tokens);
    } else {
      cmd = new MoveCommand(input);
    }
    engine = cmd.execute(engine);
  }
}
