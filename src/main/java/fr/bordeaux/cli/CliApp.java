package fr.bordeaux.cli;

import fr.bordeaux.cli.command.*;
import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.core.*;
import fr.bordeaux.util.Logger;
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
 * Manages the Quoridor CLI, handling the terminal loop, command registration, and JLine-based
 * auto-completion.
 */
public class CliApp {

  /** Constant representing a single unique match for command completion. */
  private static final int UNIQUE_MATCH = 1;

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

  /** Logger to display informations */
  private static final Logger LOGGER = Logger.getInstance();

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
    // Terminal initialisation
    this.terminal = TerminalBuilder.builder().system(true).build();
    this.engine = engine;
    this.running = true;

    // Create smart completer
    Completer smartCompleter =
        (reader, line, candidates) -> {
          String buffer = line.line();
          // Finding the current token
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
          List<String> helpOptions =
              List.of(
                  "help",
                  "hint",
                  "load",
                  "save",
                  "quit",
                  "pause",
                  "redo",
                  "undo",
                  "set",
                  "show board",
                  "move",
                  "show time",
                  "show configurations",
                  "show history");

          List<String> matches;

          if (tokens.length >= 1 && tokens[0].equals("show")) {
            String subToken = (tokens.length == 2) ? tokens[1] : "";
            matches = showOptions.stream().filter(opt -> opt.startsWith(subToken)).toList();
          } else if (tokens.length >= 1 && tokens[0].equals("help")) {
            String subToken = (tokens.length == 2) ? tokens[1] : "";
            matches = helpOptions.stream().filter(opt -> opt.startsWith(subToken)).toList();
          } else {
            matches = mainCommands.stream().filter(cmd -> cmd.startsWith(tokens[0])).toList();
          }

          // Three cases

          if (matches.isEmpty()) {
            // No match -> Bip
            beep();
          } else if (matches.size() == UNIQUE_MATCH) {
            // Only one match -> Completion
            candidates.add(new Candidate(matches.get(0)));
            firstTab = true;
          } else {
            // Several matchs
            if (firstTab) {
              // First Tab : bip to prevent there is multiple choices
              beep();
              firstTab = false;
            } else {
              // Second Tab : show candidates
              for (final String match : matches) {
                candidates.add(new Candidate(match));
              }
              firstTab = true;
            }
          }
        };

    // Create the LineReader with the smart Completer
    this.reader =
        LineReaderBuilder.builder().terminal(this.terminal).completer(smartCompleter).build();

    // init the command register
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
    commandRegistry.put("move", tokens -> new MoveCommand());
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
                    new HumanPlayer("Human", new Position(0, 4), Color.WHITE),
                    new HumanPlayer("Bob", new Position(8, 4), Color.BLACK))),
            Boolean.parseBoolean(ConfigManager.getInstance().getOption("blitz", "false")),
            Boolean.parseBoolean(ConfigManager.getInstance().getOption("verbose", "false")),
            Boolean.parseBoolean(ConfigManager.getInstance().getOption("contest", "false")),
            Boolean.parseBoolean(ConfigManager.getInstance().getOption("debug", "false")),
            Integer.parseInt(ConfigManager.getInstance().getOption("nb-walls", "10"))));
  }

  /** Triggers an audible alert (beep) through the terminal. */
  private void beep() {
    if (this.terminal != null) {
      this.terminal.writer().print("\007");
      this.terminal.flush();
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
   *
   * @throws IOException Possible if mcts with NN selection used
   */
  public void run() throws IOException {
    if (engine != null) {
      final ShowCommand show = new ShowCommand();
      show.execute(engine);
    }
    while (running) {

      if (!engine.getState().getCurrentPlayer().isAI()) {
        final String line = reader.readLine(">> ");
        fetch(line);
      } else {
        System.out.println("\n" + engine.getState().getCurrentPlayer().getName() + " réfléchit...");
        executeAiTurn();
        new ShowCommand(new String[] {"show", "board"}).execute(engine);
      }
      if (engine.getState().isGameOver()) {
        running = false;
      }
    }
    try {
      terminal.close();
    } catch (IOException e) {
      LOGGER.error("Terminal has been brutally closed...");
    }
  }

  private void executeAiTurn() {
    Player ai = engine.getState().getCurrentPlayer();

    Move move = ai.getNextMove(engine.getState());

    engine.play(move);

    LOGGER.info("L'IA a joué : " + move.toString());
  }

  /**
   * Parses the user input and executes the corresponding command. If the command is not recognized,
   * it is interpreted as a move.
   *
   * @param input the raw command entered by the user
   * @throws IOException Possible if mcts with NN selection used
   */
  public void fetch(final String input) throws IOException {
    firstTab = true;
    final String[] tokens = input.trim().split("\\s+");
    if (tokens.length == 0 || tokens[0].isEmpty()) {
      return;
    }

    final String command = tokens[0].toLowerCase(Locale.US);
    final Command cmd;

    if (commandRegistry.containsKey(command)) {
      cmd = commandRegistry.get(command).apply(tokens);
    } else {
      String suggestion = null;
      int bestDistance = 3; // Only suggest if the mistake is small (1 or 2 chars)

      for (final String registered : commandRegistry.keySet()) {
        final int dist = calculateDistance(command, registered);
        if (dist < bestDistance) {
          bestDistance = dist;
          suggestion = registered;
        }
      }

      if (suggestion != null) {
        LOGGER.debug("Unknown command: '" + command + "'");
        LOGGER.info("Did you mean: " + suggestion + " ? ");
      }

      // Default behavior (interpret as move)
      cmd = new MoveCommand(input);
    }
    engine = cmd.execute(engine);
  }

  private int calculateDistance(final String userCmd, final String command) {
    final int[][] distance = new int[userCmd.length() + 1][command.length() + 1];
    for (int i = 0; i <= userCmd.length(); i++) {
      for (int j = 0; j <= command.length(); j++) {
        if (i == 0) {
          distance[i][j] = j;
        } else if (j == 0) {
          distance[i][j] = i;
        } else {
          distance[i][j] =
              Math.min(
                  distance[i - 1][j - 1] + (userCmd.charAt(i - 1) == command.charAt(j - 1) ? 0 : 1),
                  Math.min(distance[i - 1][j] + 1, distance[i][j - 1] + 1));
        }
      }
    }
    return distance[userCmd.length()][command.length()];
  }
}
