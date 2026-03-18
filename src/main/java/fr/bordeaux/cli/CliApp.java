package fr.bordeaux.cli;

import fr.bordeaux.cli.command.*;
import fr.bordeaux.core.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import org.jline.reader.*;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

/**
 * Main application class for the Command Line Interface (CLI) of the Quoridor game. This class
 * handles the terminal interaction loop, command registration, and auto-completion features using
 * the JLine library.
 */
public class CliApp {

  /** The core engine managing the game logic, board state, and Quoridor rules. */
  private GameEngine engine;

  /** The JLine terminal abstraction used to handle system-level input, output, and signals. */
  private Terminal terminal;

  /**
   * The line reader responsible for text editing, history management, and tab-completion
   * interaction.
   */
  private LineReader reader;

  /** State flag that controls the main execution loop of the CLI application. */
  private boolean running = true;

  /**
   * Internal state used to track the "Tab" key behavior, toggling between an error beep and showing
   * suggestions.
   */
  private boolean firstTab = true;

  /**
   * A registry mapping command names to functions that instantiate specific Command objects based
   * on user input tokens.
   */
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
    commandRegistry.put("help", tokens -> new HelpCommand(tokens, commandRegistry));
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
   * @throws IOException if the terminal cannot be initialized
   */
  public CliApp(final GameEngine engine) throws IOException {
    // Initialisation du terminal
    this.terminal = TerminalBuilder.builder().system(true).build();
    this.engine = engine;
    this.running = true;

    // Création du Completer intelligent
    // Création du Completer intelligent
    Completer smartCompleter =
        (reader, line, candidates) -> {
          String buffer = line.line();
          // On utilise line.word() ou on split pour trouver le token actuel
          String[] tokens = buffer.trim().split("\\s+");

          if (buffer.isEmpty()) {
            firstTab = true;
            return;
          }

          List<String> mainCommands =
              List.of(
                  "help", "quit", "new", "save", "load", "show", "hint", "pause", "undo", "redo",
                  "set");
          List<String> showOptions = List.of("board", "time", "configurations", "history");

          List<String> matches;

          // Logique de filtrage
          if (tokens.length >= 1 && tokens[0].equals("show")) {
            String subToken = (tokens.length == 2) ? tokens[1] : "";
            matches = showOptions.stream().filter(opt -> opt.startsWith(subToken)).toList();
          } else {
            matches = mainCommands.stream().filter(cmd -> cmd.startsWith(tokens[0])).toList();
          }

          // --- LOGIQUE DES 3 CAS ---

          if (matches.isEmpty()) {
            // CAS 1 : Aucun match -> Bip
            beep(reader);
          } else if (matches.size() == 1) {
            // CAS 2 : Un seul match -> Complétion directe
            candidates.add(new Candidate(matches.get(0)));
            firstTab = true;
          } else {
            // CAS 3 : Plusieurs matchs
            if (firstTab) {
              // Premier Tab : on bip pour prévenir qu'il y a plusieurs choix
              beep(reader);
              firstTab = false;
            } else {
              // Deuxième Tab : on remplit les candidates pour que JLine les affiche
              for (String match : matches) {
                candidates.add(new Candidate(match));
              }
              firstTab = true;
            }
          }
        };

    // Création du LineReader avec le Completer
    this.reader =
        LineReaderBuilder.builder().terminal(this.terminal).completer(smartCompleter).build();

    // Initialisation du registre de commandes
    this.commandRegistry = new HashMap<>();
    commandRegistry.put("help", tokens -> new HelpCommand(tokens, commandRegistry));
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
    commandRegistry.put("set", SetCommand::new);
  }

  /**
   * Creates a CLI application with a default game configuration. Initializes a 9x9 board with two
   * human players.
   *
   * @throws IOException if the terminal setup fails
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
   * Make the beep from the terminal
   *
   * @param reader the line reader used to read user commands
   */
  private void beep(LineReader reader) {
    try {
      reader.getTerminal().writer().print("\007");
      reader.getTerminal().flush();
    } catch (Exception ignored) {
    }
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
