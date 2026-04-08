package fr.bordeaux.main;

import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.i18n.I18n;
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
          throw new IllegalArgumentException(
              "--contest " + I18n.get("require", I18n.get("filePath")));
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
          throw new IllegalArgumentException(I18n.get("invalidOpt") + arg);
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

  /** Prints usage and help information to the console. */
  public static void printHelp() {
    logInfo("Usage: quoridor [OPTIONS]");
    logInfo("");
    logInfo(I18n.get("geneOpt"));
    logInfo("  -h, --help        " + I18n.get("showThis"));
    logInfo("  -V, --version     " + I18n.get("displayVersion"));
    logInfo("  -v, --verbose     " + I18n.get("verboseMode"));
    logInfo("  -d, --debug       " + I18n.get("debugMode"));
    logInfo("  -D, --daemon      " + I18n.get("headless"));
    logInfo("");
    logInfo("Modes:");
    logInfo("  --contest, -c <file> " + I18n.get("contest"));
    logInfo("  -g, --gui         " + I18n.get("guiMode"));
    logInfo("  -n, --net         " + I18n.get("networkMode"));
    logInfo("  -p, --players N   " + I18n.get("numberPlayers"));
    logInfo("");
    logInfo(I18n.get("blitzOpt"));
    logInfo("  -b, --blitz       " + I18n.get("enableBlitz"));
    logInfo("  -t, --time N      " + I18n.get("timeout"));
  }
}
