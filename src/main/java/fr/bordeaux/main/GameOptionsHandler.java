package fr.bordeaux.main;

import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.util.Logger;
import org.apache.commons.cli.CommandLine;

/** Handles game rules, server settings, and time constraints from CLI. */
public final class GameOptionsHandler {

  private static final Logger LOGGER = Logger.getInstance();

  private GameOptionsHandler() {}

  /**
   * Processes game-related arguments using Commons CLI.
   *
   * @param commandLine The parsed command line.
   * @param config The configuration manager.
   */
  public static void handle(final CommandLine commandLine, final ConfigManager config) {
    if (commandLine.hasOption("b")) {
      config.setOption("blitz", "true");
    }

    if (commandLine.hasOption("t")) {
      final boolean blitz = config.getBoolean("blitz", false);
      if (!blitz) {
        if (LOGGER.isInfoEnabled()) {
          LOGGER.info("--time nécessite d'activer le mode blitz. Il a donc été ignoré.");
        }
      } else {
        final String value = commandLine.getOptionValue("t");
        try {
          if (Integer.parseInt(value) <= 0) {
            throw new IllegalArgumentException("--time should be higher than 0");
          }
          config.setOption("timeout", value);
        } catch (NumberFormatException exception) {
          throw new IllegalArgumentException("--time requires an integer");
        }
      }
    }

    if (commandLine.hasOption("s")) {
      final String value = commandLine.getOptionValue("s");
      try {
        final int argument = Integer.parseInt(value);
        if (argument < 16 && argument > 2) {
          if (argument % 2 == 0) {
            throw new IllegalArgumentException("--size must be an odd number");
          }
          config.setOption("board-size", value);
        } else if (argument >= 20 && argument <= 99999) {
          config.setOption("server", "true");
          config.setOption("server-port", value);
        }
      } catch (NumberFormatException exception) {
        throw new IllegalArgumentException("--server requires a integer or a port");
      }
    }

    if (commandLine.hasOption("size")) {
      config.setOption("board-size", commandLine.getOptionValue("size"));
    }

    if (commandLine.hasOption("server")) {
      config.setOption("server", "true");
      config.setOption("server-port", commandLine.getOptionValue("server"));
    }

    if (commandLine.hasOption("p")) {
      config.setOption("nb-players", commandLine.getOptionValue("p"));
    }

    if (commandLine.hasOption("w")) {
      final String value = commandLine.getOptionValue("w");
      try {
        if (Integer.parseInt(value) < 0) {
          throw new IllegalArgumentException("--walls requires to be higher than 0");
        } else {
          config.setOption("nb-walls", value);
        }
      } catch (NumberFormatException exception) {
        throw new IllegalArgumentException("--walls requires an integer");
      }
    }
  }
}
