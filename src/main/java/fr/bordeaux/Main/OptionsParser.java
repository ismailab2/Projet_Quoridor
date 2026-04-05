package fr.bordeaux.Main;

import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.util.Logger;

/**
 * This class interprets CLI arguments and configures the application accordingly. Supported options
 * include general settings (help, version, verbose, debug), game modes (GUI, CLI, network), and
 * blitz mode settings.
 *
 * <p>The parser also validates argument values and throws {@link IllegalArgumentException} for
 * invalid or missing parameters.
 */
public final class OptionsParser {

  private static final Logger LOGGER = Logger.getInstance();

  private OptionsParser() {}

  /**
   * Recognized arguments:
   *
   * <ul>
   *   <li><b>-h, --help</b>: Display help information.
   *   <li><b>-V, --version</b>: Display the current version of the application.
   *   <li><b>-v, --verbose</b>: Enable verbose logging.
   *   <li><b>-d, --debug</b>: Enable debug mode.
   *   <li><b>-b, --blitz</b>: Enable blitz mode.
   *   <li><b>-t, --time N</b>: Set the timeout in minutes (default 30).
   *   <li><b>-g, --gui</b>: Launch GUI mode.
   *   <li><b>-c, --cli</b>: Launch CLI mode (default).
   *   <li><b>-n, --net</b>: Launch network mode.
   * </ul>
   *
   * @param args Command-line arguments.
   * @param config Configuration manager instance to apply parsed options.
   * @throws IllegalArgumentException if an invalid argument or missing value is provided.
   */
  public static void parse(String[] args, ConfigManager config) {
    // Reset session-specific flags to their default values
    config.setOption("blitz", "false");
    config.setOption("contest", "false");
    config.setOption("ai1", "false");
    config.setOption("ai2", "false");

    for (int i = 0; i < args.length; i++) {
      String arg = args[i];

      switch (arg) {
        case "-h", "--help" -> {
          printHelp();
        }

        case "-V", "--version" -> {
          logInfo("Quoridor version 1.0.0");
        }

          case "--contest", "-c" -> {
              if (i + 1 < args.length) {
                  String file = args[++i];
                  config.enableContest(file);

              } else {
                  throw new IllegalArgumentException("--contest requires a file path");
              }
          }

        case "-v", "--verbose" -> config.setOption("verbose", "true");

        case "-d", "--debug" -> config.setOption("debug", "true");

        case "--daemon" -> config.setOption("daemon", "true");

        case "-b", "--blitz" -> config.setOption("blitz", "true");

        case "-t", "--time" -> {
          i++;
          handleTimeArgument(args, i, config);
        }

        case "-a", "--ai" -> {
          String aiOption = "";
          if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
            aiOption = args[++i].toLowerCase();
          }

          if ("a".equals(aiOption)) {
            config.setOption("ai1", "true");
            config.setOption("ai2", "true");
          } else if ("blanc".equals(aiOption) || "1".equals(aiOption)) {
            config.setOption("ai1", "true");
          } else {
            // Default to Player 2
            config.setOption("ai2", "true");
          }
        }

        case "--ai-mode" -> applyAiOption(args[++i], "mode", config);

        case "--ai-minimax-depth" -> applyAiOption(args[++i], "depth", config);

        case "--ai-time" -> applyAiOption(args[++i], "time", config);

        case "--ai-minimax-scoring" -> applyAiOption(args[++i], "scoring", config);

        case "--ai-mcts-selection" -> applyMctsOption(args[++i], config);

        case "-s" -> {
          if (i + 1 < args.length) {
            int argument = Integer.parseInt(args[i + 1]);
            if (argument < 16 && argument > 2) {
              config.setOption("board-size", args[++i]);
            } else if (argument >= 1000 && argument <= 9999) {
              config.setOption("server", "true");
              config.setOption("server-port", args[++i]);
            }
          } else {
            throw new IllegalArgumentException("--server requires a integer or a port");
          }
        }

        case "--server" -> {
          if (i + 1 < args.length) {
            config.setOption("server", "true");
            config.setOption("server-port", args[++i]);
          } else {
            throw new IllegalArgumentException("--server requires a port");
          }
        }

        case "-p", "--players" -> {
          if (i + 1 < args.length) {
            config.setOption("nb-players", args[++i]);
          } else {
            throw new IllegalArgumentException("--players requires an integer between 2 and 4");
          }
        }

        case "-w", "--walls" -> {
          if (i + 1 < args.length) {
            if (Integer.parseInt(args[i + 1]) < 0) {
              config.setOption("nb-walls", "1000");
            } else {
              config.setOption("nb-walls", args[++i]);
            }
          } else {
            throw new IllegalArgumentException("--walls requires an integer");
          }
        }

        case "--size" -> {
          if (i + 1 < args.length) {
            config.setOption("board-size", args[++i]);
          } else {
            throw new IllegalArgumentException("--walls requires an integer");
          }
        }

        case "-g", "--gui", "--cli", "-n", "--net" -> {
          // Handled elsewhere (AppModeResolver)
        }

        default -> throw new IllegalArgumentException("Invalid option: " + arg);
      }
    }
  }

  /** Sets AI options only for players currently flagged as AI. */
  private static void applyAiOption(
      final String value, final String suffix, final ConfigManager config) {
    final String[] values = value.split(",");

    // Apply to Player 1 if AI is enabled
    if (config.getBoolean("ai1", false)) {
      config.setOption("ai1." + suffix, values[0].trim());
    }

    // Apply to Player 2 if AI is enabled
    if (config.getBoolean("ai2", false)) {
      // Use the second value if provided, otherwise fallback to the first
      final String valToSet = (values.length > 1) ? values[1] : values[0];
      config.setOption("ai2." + suffix, valToSet.trim());
    }
  }

  /** Specialized helper for MCTS selection with active AI check. */
  private static void applyMctsOption(final String value, final ConfigManager config) {
    final String[] values = value.split(",");
    for (final String v : values) {
      final String trimmed = v.trim();
      if (!trimmed.equals("UCT") && !trimmed.equals("ML")) {
        throw new IllegalArgumentException("Invalid option: " + trimmed + " for MCTS selection.");
      }
    }

    if (config.getBoolean("ai1", false)) {
      config.setOption("ai1.selection", values[0].trim());
    }
    if (config.getBoolean("ai2", false)) {
      final String valToSet = (values.length > 1) ? values[1] : values[0];
      config.setOption("ai2.selection", valToSet.trim());
    }
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

  private static void handleTimeArgument(
      final String[] args, final int index, final ConfigManager config) {

    final boolean blitz = config.getBoolean("blitz", false);
    if (!blitz) {
      logInfo("--time nécessite d'activer le mode blitz. Il a donc été ignoré.");
    }
    if (index < args.length) {
      final String value = args[index];
      config.setOption("timeout", value);
    } else {
      throw new IllegalArgumentException("--time requires a value");
    }
  }

  /**
   * @brief Prints usage and help information to the console.
   */

    private static void printHelp() {
        logInfo("Usage: quoridor [OPTIONS]");
        logInfo("");
        logInfo("General options:");
        logInfo("  -h, --help        Show this help message");
        logInfo("  -V, --version     Display version");
        logInfo("  -v, --verbose     Enable verbose mode");
        logInfo("  -d, --debug       Enable debug mode");
        logInfo("");
        logInfo("Modes:");
        logInfo("  --contest, -c <file> Launch contest mode from file");
        logInfo("  -g, --gui         Launch GUI mode");
        logInfo("  -n, --net         Launch network mode");
        logInfo("");
        logInfo("Blitz options:");
        logInfo("  -b, --blitz       Enable blitz mode");
        logInfo("  -t, --time N      Timeout in minutes (default 30)");
        System.exit(0);
    }
}
