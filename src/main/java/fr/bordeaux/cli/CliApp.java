package fr.bordeaux.cli;

import fr.bordeaux.cli.command.*;
import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.core.*;
import fr.bordeaux.i18n.I18n;
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

/** Manages Quoridor CLI loop and commands. */
public class CliApp {

  /** Help command literal. */
  private static final String CMD_HELP = "help";

  /** Quit command literal. */
  private static final String CMD_QUIT = "quit";

  /** New game command literal. */
  private static final String CMD_NEW = "new";

  /** Load game command literal. */
  private static final String CMD_LOAD = "load";

  /** Save game command literal. */
  private static final String CMD_SAVE = "save";

  /** Pause command literal. */
  private static final String CMD_PAUSE = "pause";

  /** Hint command literal. */
  private static final String CMD_HINT = "hint";

  /** Undo command literal. */
  private static final String CMD_UNDO = "undo";

  /** Redo command literal. */
  private static final String CMD_REDO = "redo";

  /** Show command literal. */
  private static final String CMD_SHOW = "show";

  /** Set command literal. */
  private static final String CMD_SET = "set";

  /** Board option literal. */
  private static final String OPT_BOARD = "board";

  /** Literals to avoid duplication. */
  private static final String STR_FALSE = "false";

  /** Constant representing a single unique match for command completion. */
  private static final int UNIQUE_MATCH = 1;

  /** The core engine managing the game logic, board state, and Quoridor rules. */
  private GameEngine engine;

  /** System terminal. */
  private Terminal terminal;

  /** The line reader responsible for text editing */
  private LineReader reader;

  /** State flag that controls the main execution loop of the CLI application. */
  private boolean running = true;

  /** Internal state used to track the "Tab" key behavior */
  private boolean firstTab = true;

  /** Logger to display informations */
  private static final Logger LOGGER = Logger.getInstance();

  /** A registry mapping command names */
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

    registerAllCommands();
  }

  /** Initialises the command registry to avoid code duplication in constructors. */
  private void registerAllCommands() {
    commandRegistry.put(CMD_HELP, tokens -> new HelpCommand(tokens, commandRegistry));
    commandRegistry.put(
        CMD_QUIT,
        tokens -> {
          this.running = false;
          return new QuitCommand(reader);
        });
    commandRegistry.put(CMD_NEW, tokens -> new NewCommand(tokens, terminal, reader));
    commandRegistry.put(CMD_LOAD, LoadCommand::new);
    commandRegistry.put(CMD_SAVE, SaveCommand::new);
    commandRegistry.put(CMD_PAUSE, tokens -> new PauseCommand());
    commandRegistry.put(CMD_HINT, tokens -> new HintCommand());
    commandRegistry.put(CMD_UNDO, UndoCommand::new);
    commandRegistry.put(CMD_REDO, RedoCommand::new);
    commandRegistry.put(CMD_SHOW, ShowCommand::new);
    commandRegistry.put(CMD_SET, tokens -> new SetCommand());
  }

  /**
   * Creates a CLI application using the provided game engine.
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
    final Completer smartCompleter = new SmartCompleter();

    // Create the LineReader with the smart Completer
    this.reader =
        LineReaderBuilder.builder().terminal(this.terminal).completer(smartCompleter).build();

    // init the command register
    this.commandRegistry = new HashMap<>();
    registerAllCommands();
  }

  /**
   * Creates a CLI application with a default game configuration.
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
            Boolean.parseBoolean(ConfigManager.getInstance().getOption("blitz", STR_FALSE)),
            Boolean.parseBoolean(ConfigManager.getInstance().getOption("verbose", STR_FALSE)),
            Boolean.parseBoolean(ConfigManager.getInstance().getOption("contest", STR_FALSE)),
            Boolean.parseBoolean(ConfigManager.getInstance().getOption("debug", STR_FALSE)),
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
   * Starts the CLI loop. Displays the board, reads commands until the application is stopped.
   *
   * @throws IOException Possible if mcts with NN selection used
   */
  public void run() throws IOException {
    if (engine != null) {
      final ShowCommand show = new ShowCommand(new String[] {CMD_SHOW, OPT_BOARD});
      show.execute(engine);
    }
    while (running) {

      if (engine.getState().getCurrentPlayer().isAI()) {
        if (terminal != null && terminal.writer() != null) {
          terminal
              .writer()
              .println("\n" + engine.getState().getCurrentPlayer().getName() + " réfléchit...");
          terminal.flush();
        }
        executeAiTurn();
        new ShowCommand(new String[] {CMD_SHOW, OPT_BOARD}).execute(engine);
      } else {
        final String line = reader.readLine(">> ");
        fetch(line);
      }
      if (engine.getState().isGameOver()) {
        running = false;
      }
    }
    try {
      terminal.close();
    } catch (IOException e) {
      LOGGER.error(I18n.get("brutalClose"));
    }
  }

  void executeAiTurn() {
    final Player ai = engine.getState().getCurrentPlayer();
    final Move move = ai.getNextMove(engine.getState());
    engine.play(move);

    LOGGER.info(I18n.get("iaPlayed") + move.toString());
  }

  /**
   * Parses user input and executes the command. If unrecognized, treats as a move. it is
   * interpreted as a move.
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
        LOGGER.debug(I18n.get("unknownCmd") + command);
        LOGGER.info(I18n.get("meant", suggestion, "?"));
      }

      // Default behavior (interpret as move)
      cmd = new MoveCommand(input);
    }
    engine = cmd.execute(engine);
  }

  private int calculateDistance(final String userCmd, final String command) {
    final int userLen = userCmd.length();
    final int cmdLen = command.length();
    final int[][] distance = new int[userLen + 1][cmdLen + 1];
    for (int i = 0; i <= userLen; i++) {
      for (int j = 0; j <= cmdLen; j++) {
        if (i == 0) {
          distance[i][j] = j;
        } else if (j == 0) {
          distance[i][j] = i;
        } else {
          final int substitutionCost = userCmd.charAt(i - 1) == command.charAt(j - 1) ? 0 : 1;
          distance[i][j] =
              Math.min(
                  distance[i - 1][j - 1] + substitutionCost,
                  Math.min(distance[i - 1][j] + 1, distance[i][j - 1] + 1));
        }
      }
    }
    return distance[userLen][cmdLen];
  }

  /** Package-private inner class to handle smart tab-completion for the CLI. */
  class SmartCompleter implements Completer {
    @Override
    public void complete(
        final LineReader reader, final ParsedLine line, final List<Candidate> candidates) {
      final String buffer = line.line();
      // Finding the current token
      final String[] tokens = buffer.trim().split("\\s+");

      if (buffer.isEmpty()) {
        firstTab = true;
        return;
      }

      final List<String> mainCommands =
          List.of(
              CMD_HELP, CMD_QUIT, CMD_NEW, CMD_SAVE, CMD_LOAD, CMD_SHOW, CMD_HINT, CMD_PAUSE,
              CMD_UNDO, CMD_REDO, CMD_SET);
      final List<String> showOptions = List.of(OPT_BOARD, "time", "configurations", "history");
      final List<String> helpOptions =
          List.of(
              CMD_HELP,
              CMD_HINT,
              CMD_LOAD,
              CMD_SAVE,
              CMD_QUIT,
              CMD_PAUSE,
              CMD_REDO,
              CMD_UNDO,
              CMD_SET,
              CMD_SHOW + " " + OPT_BOARD,
              "move",
              CMD_SHOW + " time",
              CMD_SHOW + " configurations",
              CMD_SHOW + " history");

      final List<String> matches;

      if (tokens.length >= 1 && CMD_SHOW.equals(tokens[0])) {
        final String subToken = (tokens.length == 2) ? tokens[1] : "";
        matches = showOptions.stream().filter(opt -> opt.startsWith(subToken)).toList();
      } else if (tokens.length >= 1 && CMD_HELP.equals(tokens[0])) {
        final String subToken = (tokens.length == 2) ? tokens[1] : "";
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
        // Several matches
        if (firstTab) {
          // First Tab : bip to indicate multiple choices
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
    }
  }
}
