package fr.bordeaux.main;

import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.i18n.I18n;
import fr.bordeaux.util.Logger;
import java.util.ArrayList;
import java.util.List;

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

      switch (arg) {
        case "-h", "--help" -> {
          printHelp();
          // Exit if not in test mode
          if (System.getProperty("quoridor.test") == null) {
            System.exit(0);
          }
        }

        case "-V", "--version" -> {
          logInfo("Quoridor version 1.0.0");
          // Exit if not in test mode
          if (System.getProperty("quoridor.test") == null) {
            System.exit(0);
          }
        }

        case "--contest", "-c" -> {
          if (i + 1 < args.length) {
            i++;
            final String file = args[i];
            config.enableContest(file);

          } else {
            throw new IllegalArgumentException("--contest "+I18n.get("needsFile"));
          }
        }

        case "-v", "--verbose" -> config.setOption("verbose", "true");

        case "-d", "--debug" -> config.setOption("debug", "true");

        case "-D", "--daemon" -> config.setOption("daemon", "true");

        case "-b", "--blitz" -> config.setOption("blitz", "true");

        case "-t", "--time" -> {
          handleTimeArgument(args, ++i, config);
        }

        case "-a", "--ai" -> {
          String aiOption = "";
          if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
            aiOption = args[++i].toLowerCase(java.util.Locale.ROOT);
          }

          if (aiOption.isEmpty()) {
            config.setOption("ai2", "true");
          } else if ("a".equals(aiOption)) {
            config.setOption("ai1", "true");
            config.setOption("ai2", "true");
          } else if ("blanc".equals(aiOption) || "1".equals(aiOption)) {
            config.setOption("ai1", "true");
          } else if ("noir".equals(aiOption) || "2".equals(aiOption)) {
            config.setOption("ai2", "true");
          } else {
            throw new IllegalArgumentException(
                "--ai value must be: {blanc, noir, 1, 2, a} or empty (defaults to noir)");
          }
        }

        case "--ai-mode" -> {
          checkAiString(
              args[++i], "mode", new ArrayList<>(List.of("minimax", "mcts", "iterative")));
          applyAiOption(args[i], "mode", config);
        }

        case "--ai-minimax-depth" -> {
          checkAiInt(args[++i], "minimax-depth");
          applyAiOption(args[i], "depth", config);
        }

        case "--ai-time" -> {
          checkAiInt(args[++i], "time");
          applyAiOption(args[i], "time", config);
        }

        case "--ai-minimax-scoring" -> {
          checkAiString(
              args[++i], "scoring", new ArrayList<>(List.of("simple", "intermediate", "advanced")));
          applyAiOption(args[i], "scoring", config);
        }

        case "--ai-mcts-selection" -> applyMctsOption(args[++i], config);

        case "-s" -> {
          if (i + 1 < args.length) {
            final int argument = Integer.parseInt(args[i + 1]);
            if (argument < 16 && argument > 2) {
              if (argument % 2 == 0) {
                throw new IllegalArgumentException("--size must be an odd number");
              }
              config.setOption("board-size", args[++i]);
            } else if (argument >= 10 && argument <= 9999) {
              config.setOption("server", "true");
              config.setOption("server-port", args[++i]);
            }
          } else {
            throw new IllegalArgumentException("--server "+I18n.get("requiresIntPort"));
          }
        }

        case "--server" -> {
          if (i + 1 < args.length) {
            config.setOption("server", "true");
            config.setOption("server-port", args[++i]);
          } else {
            throw new IllegalArgumentException("--server "+I18n.get("requirePort"));
          }
        }

        case "-p", "--players" -> {
          if (i + 1 < args.length) {
            config.setOption("nb-players", args[++i]);
          } else {
            throw new IllegalArgumentException("--players "+I18n.get("requireInter"));
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
            throw new IllegalArgumentException("--walls "+I18n.get("requireInt"));
          }
        }

        case "--size" -> {
          if (i + 1 < args.length) {
            config.setOption("board-size", args[++i]);
          } else {
            throw new IllegalArgumentException("--walls "+I18n.get("requireInt"));
          }
        }

        case "-g", "--gui", "--cli", "-n", "--net" -> {
          // Handled elsewhere (AppModeResolver)
        }

        default -> {
          if (arg.startsWith("-")) {
            throw new IllegalArgumentException(I18n.get("invalidOpt", arg));
          }
          config.setOption("load-file", arg);
        }
      }
      i++;
    }
    validateAiPlayerCount(config);
  }

  /**
   * Validates that AI is only enabled for 2-player games.
   *
   * @param config The configuration manager instance.
   * @throws IllegalArgumentException If AI is active with more than 2 players.
   */
  private static void validateAiPlayerCount(final ConfigManager config) {
    final int nbPlayers = Integer.parseInt(config.getOption("nb-players", "2"));
    final boolean hasAi = config.getBoolean("ai1", false) || config.getBoolean("ai2", false);

    if (hasAi && nbPlayers > 2) {
      throw new IllegalArgumentException(
          I18n.get("notSupported") + nbPlayers + ")");
    }
  }

  /**
   * Validates that a comma-separated string contains only positive integers.
   *
   * @param value The raw string to validate (e.g., "3,5").
   * @param parameter The parameter name for the error message.
   * @throws IllegalArgumentException If value is null or not a positive integer.
   */
  private static void checkAiInt(final String value, final String parameter) {
    if (value == null) {
      throw new IllegalArgumentException(I18n.get("ai", parameter) + I18n.get("requireInt"));
    }
    final String[] values = value.split(",");
    for (final String val : values) {
      if (Integer.parseInt(val) <= 0) {
        throw new IllegalArgumentException("--ai-" + parameter + I18n.get("higher"));
      }
    }
  }

  /**
   * Validates that a comma-separated string contains only allowed keywords.
   *
   * @param value The raw string to validate (e.g., "minimax,mcts,iterative").
   * @param parameter The parameter name for the error message.
   * @param accpetedMode List of permitted string values.
   * @throws IllegalArgumentException If value is null or contains an invalid mode.
   */
  private static void checkAiString(
      final String value, final String parameter, final List<String> accpetedMode) {
    if (value == null) {
      throw new IllegalArgumentException(
          "AI " + parameter + " require an argument in " + accpetedMode);
    }
    final String[] values = value.split(",");
    for (final String val : values) {
      if (!accpetedMode.contains(val.toLowerCase())) {
        throw new IllegalArgumentException(
            "The only valid AI " + parameter + " are " + accpetedMode);
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
      if (!"UCT".equals(trimmed) && !"ML".equals(trimmed)) {
        throw new IllegalArgumentException(I18n.get("invalidOpt", trimmed, I18n.get("mctsSelect")));
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
      logInfo("--time "+I18n.get("needsBlitz"));
    } else if (index < args.length) {
      final String value = args[index];
      if (Integer.parseInt(value) <= 0) {
        throw new IllegalArgumentException("--time "+I18n.get("higher"));
      }
      config.setOption("timeout", value);
    } else {
      throw new IllegalArgumentException("--time "+I18n.get("requireVal"));
    }
  }

  /** Prints usage and help information to the console. */
  public static void printHelp() {
    logInfo("Usage: quoridor [OPTIONS]");
    logInfo("");
    logInfo(I18n.get("geneOpt"));
    logInfo("  -h, --help        "+I18n.get("showThis"));
    logInfo("  -V, --version     "+I18n.get("displayVersion"));
    logInfo("  -v, --verbose     "+I18n.get("verboseMode"));
    logInfo("  -d, --debug       "+I18n.get("debugMode"));
    logInfo("  -D, --daemon      "+I18n.get("headless"));
    logInfo("");
    logInfo("Modes:");
    logInfo("  --contest, -c <file> "+I18n.get("contest"));
    logInfo("  -g, --gui         "+I18n.get("guiMode"));
    logInfo("  -n, --net         "+I18n.get("networkMode"));
    logInfo("  -p, --players N   "+I18n.get("numberPlayers"));
    logInfo("");
    logInfo(I18n.get("blitzOpt"));
    logInfo("  -b, --blitz       "+I18n.get("enableBlitz"));
    logInfo("  -t, --time N      "+I18n.get("timeout"));
  }
}
