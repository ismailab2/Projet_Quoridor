package fr.bordeaux.Main;

import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.util.Logger;
import java.util.ArrayList;
import java.util.List;

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

    List<String> aiPrefix = new ArrayList<>();

    for (int i = 0; i < args.length; i++) {
      String arg = args[i];

      switch (arg) {
        case "-h", "--help" -> {
          printHelp();
        }

        case "-V", "--version" -> {
          logInfo("Quoridor version 1.0.0");
        }

        case "-v", "--verbose" -> config.setOption("verbose", "true");

        case "-d", "--debug" -> config.setOption("debug", "true");

        case "--daemon" -> config.setOption("daemon", "true");

        case "-b", "--blitz" -> config.setOption("blitz", "true");

        case "-t", "--time" -> {
          i++;
          handleTimeArgument(args, i, config);
        }

        case "--ai" -> {
          i++;
          if (i < args.length) {
            String aiColors = args[i].toLowerCase();
            System.out.println(aiColors);
            String[] colors = aiColors.split(",");
            for (int x = 0; x < colors.length; x++) {

              String myColor = colors[x];
              String playerId;

              if (myColor.equals("blanc") || myColor.equals("1")) {
                playerId = "1";
              } else if (myColor.equals("noir") || myColor.equals("2")) {
                playerId = "2";
              } else {
                throw new IllegalArgumentException(
                    "Couleur IA non reconnue. Utilisez 'blanc' (1) ou 'noir' (2).");
              }
              config.setOption("ai" + playerId, "true");
              aiPrefix.add(playerId);
            }
          }
          break;
        }

        case "--ai-mode" -> {
          i++;
          if (i < args.length) {
            String[] mode = args[i].split(",");
            for (int x = 0; x < mode.length; x++) {
              config.setOption("ai" + aiPrefix.get(x) + ".mode", mode[x]);
            }
          }
          break;
        }

        case "--ai-minimax-depth" -> {
          i++;
          if (i < args.length) {
            String[] depth = args[i].split(",");
            for (int x = 0; x < depth.length; x++) {
              config.setOption("ai" + aiPrefix.get(x) + ".depth", depth[x]);
            }
          }
          break;
        }

        case "--ai-time" -> {
          i++;
          if (i < args.length) {
            String[] time = args[i].split(",");
            for (int x = 0; x < time.length; x++) {
              config.setOption("ai" + aiPrefix.get(x) + ".time", time[x]);
            }
          }
          break;
        }

        case "--ai-minimax-scoring" -> {
          i++;
          if (i < args.length) {
            String[] scoring = args[i].split(",");
            for (int x = 0; x < scoring.length; x++) {
              config.setOption("ai" + aiPrefix.get(x) + ".scoring", scoring[x]);
            }
          }
          break;
        }

        case "--ai-mcts-selection" -> {
          i++;
          if (i < args.length) {
            String[] selection = args[i].split(",");
            for (int x = 0; x < selection.length; x++) {
              if (x >= aiPrefix.size()) {
                throw new IllegalArgumentException(
                    "Invalid option : "
                        + selection[x]
                        + " too many options compared to AIs number");
              }
              if (!selection[x].equals("UCT") && !selection[x].equals("ML")) {
                throw new IllegalArgumentException(
                    "Invalid option: " + selection[x] + " for mcts selection.");
              }
              config.setOption("ai" + aiPrefix.get(x) + ".selection", selection[x]);
            }
          }
        }

        case "-s", "--server" -> {
          if (i + 1 < args.length) {
            config.setOption("server", "true");
            config.setOption("server-port", args[++i]);
          } else {
            throw new IllegalArgumentException("--server requires a port");
          }
        }

        case "-g", "--gui", "-c", "--cli", "-n", "--net" -> {
          // Handled elsewhere (AppModeResolver)
        }

        default -> throw new IllegalArgumentException("Invalid option: " + arg);
      }
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
    logInfo("  -g, --gui         Launch GUI mode");
    logInfo("  -c, --cli         Launch CLI mode (default)");
    logInfo("  -n, --net         Launch network mode");

    logInfo("");
    logInfo("Blitz options:");
    logInfo("  -b, --blitz       Enable blitz mode");
    logInfo("  -t, --time N      Timeout in minutes (default 30)");
  }
}
