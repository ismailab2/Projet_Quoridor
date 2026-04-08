package fr.bordeaux.main;

import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.util.Logger;

/** Interprets CLI arguments (GUI, CLI, Network) and game settings. */
public final class OptionsParser {

  private static final Logger LOGGER = Logger.getInstance();

  private OptionsParser() {}

  /**
   * Parses CLI arguments into configuration.
   *
   * @param args Command-line arguments.
   * @param config Config manager instance.
   */
  public static void parse(final String[] args, final ConfigManager config) {
    int i = 0;
    while (i < args.length) {
      final String arg = args[i];

      int newIndex = AiOptionsHandler.handle(arg, args, i, config);
      if (newIndex != -1) {
        i = newIndex + 1;
        continue;
      }

      newIndex = GameOptionsHandler.handle(arg, args, i, config);
      if (newIndex != -1) {
        i = newIndex + 1;
        continue;
      }

      i = handleSystemOption(arg, args, i, config);
      i++;
    }
    AiOptionsHandler.validateAiPlayerCount(config);
  }

  private static int handleSystemOption(
      final String arg, final String[] args, final int index, final ConfigManager config) {
    int i = index;
    switch (arg) {
      case "-h", "--help" -> {
        printHelp();
        if (System.getProperty("quoridor.test") == null) {
          System.exit(0);
        }
      }
      case "-V", "--version" -> {
        logInfo("Quoridor version 1.0.0");
        if (System.getProperty("quoridor.test") == null) {
          System.exit(0);
        }
      }
      case "--contest", "-c" -> {
        if (i + 1 < args.length) {
          config.enableContest(args[++i]);
        } else {
          throw new IllegalArgumentException("--contest requires a file path");
        }
      }
      case "-v", "--verbose" -> config.setOption("verbose", "true");
      case "-d", "--debug" -> config.setOption("debug", "true");
      case "-D", "--daemon" -> config.setOption("daemon", "true");
      case "-g", "--gui", "--cli", "-n", "--net" -> {
        // Handled elsewhere (AppModeResolver)
      }
      default -> {
        if (arg.startsWith("-")) {
          throw new IllegalArgumentException("Invalid option: " + arg);
        }
        config.setOption("load-file", arg);
      }
    }
    return i;
  }

  /**
   * @brief Logs an informational message if info logging is enabled.
   * @param message Message to log.
   */
  private static void logInfo(final String message) {
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info(message);
    }
  }

    /** Prints usage and help information to the console, including game options, defaults, and CLI arguments. */
    public static void printHelp() {
        ConfigManager config = ConfigManager.getInstance();

        logInfo("Usage: quoridor [OPTIONS] [LOAD_FILE]");
        logInfo("");

        logInfo("General options:");
        logInfo("  -h, --help             Show this help message");
        logInfo("  -V, --version          Display version");
        logInfo("  -v, --verbose          Enable verbose logging (default: " + config.getBoolean("verbose", true) + ")");
        logInfo("  -d, --debug            Enable debug logging (default: " + config.getBoolean("debug", true) + ")");
        logInfo("  -D, --daemon           Launch server in headless mode (default: " + config.getBoolean("daemon", false) + ")");
        logInfo("");

        logInfo("Modes:");
        logInfo("  -g, --gui              Launch GUI mode");
        logInfo("  -c, --cli              Launch CLI mode");
        logInfo("  -n, --net, --server, -s Launch network mode");
        logInfo("  --contest, -C <file>   Launch contest mode from file");
        logInfo("");

        logInfo("Game options:");
        logInfo("  -p, --players N        Number of players (2-4) (default: " + config.getInt("nb-players", 2) + ")");
        logInfo("  -b, --blitz            Enable blitz mode (default: " + config.getBoolean("blitz", false) + ")");
        logInfo("  -t, --time N           Timeout in minutes (default: " + config.getInt("timeout", 30) + ")");
        logInfo("  -w, --walls N          Number of walls per player (default: " + config.getInt("nb-walls", 20) + ")");
        logInfo("  --size N               Board size, e.g., 9 for 9x9 (default: " + config.getInt("board-size", 9) + ")");
        logInfo("");

        logInfo("AI options:");
        logInfo("  -a, --ai [MODE]        Enable AI player(s):");
        logInfo("                         blanc | 1   -> AI for player 1");
        logInfo("                         noir  | 2   -> AI for player 2 (default)");
        logInfo("                         a            -> AI for both players");
        logInfo("                         (no value)   -> AI for player 2");
        logInfo("");
        logInfo("  --ai-mode MODE         AI algorithm: minimax, mcts, iterative");
        logInfo("  --ai-minimax-depth N   Depth for minimax algorithm (>0)");
        logInfo("  --ai-time N            Time limit for AI in seconds (>0)");
        logInfo("  --ai-minimax-scoring S Scoring method: simple, intermediate, advanced");
        logInfo("  --ai-mcts-selection S  MCTS selection: UCT, ML");
        logInfo("");

        logInfo("Load/Save:");
        logInfo("  [LOAD_FILE]            Optional path to a saved game to load at startup");
        logInfo("");

        logInfo("Shortcut and configuration options:");
        logInfo("  --shortcut-config      Open GUI to configure keyboard shortcuts");
        logInfo("  --config <file>        Load custom configuration file");
        logInfo("");

        logInfo("Examples:");
        logInfo("  ./quoridor -g -p 4 -w 20 --size 9");
        logInfo("  ./quoridor -a a");
        logInfo("  ./quoridor -g -a blanc");
        logInfo("  ./quoridor -a a --ai-mode minimax --ai-minimax-depth 3");
        logInfo("  ./quoridor -n --daemon -p 2");
        logInfo("  ./quoridor --contest contest_file.txt");
    }
}
