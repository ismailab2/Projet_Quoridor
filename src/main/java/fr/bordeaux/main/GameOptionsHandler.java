package fr.bordeaux.main;

import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.util.Logger;

/** Handles game rules, server settings, and time constraints from CLI. */
public final class GameOptionsHandler {

  private static final Logger LOGGER = Logger.getInstance();

  private GameOptionsHandler() {}

  /**
   * Processes game-related arguments.
   *
   * @param arg    The current argument.
   * @param args   The full array of arguments.
   * @param index  The current index in the array.
   * @param config The configuration manager.
   * @return The new index after consuming arguments, or -1 if not handled.
   */
  public static int handle(final String arg, final String[] args, final int index, final ConfigManager config) {
    int i = index;
    switch (arg) {
      case "-b", "--blitz" -> config.setOption("blitz", "true");
      case "-t", "--time" -> {
        final boolean blitz = config.getBoolean("blitz", false);
        if (!blitz) {
          if (LOGGER.isInfoEnabled()) {
            LOGGER.info("--time nécessite d'activer le mode blitz. Il a donc été ignoré.");
          }
        } else if (i + 1 < args.length) {
          final String value = args[++i];
          if (Integer.parseInt(value) <= 0) {
            throw new IllegalArgumentException("--time should be higher than 0");
          }
          config.setOption("timeout", value);
        } else {
          throw new IllegalArgumentException("--time requires a value");
        }
      }
      case "-s" -> {
        if (i + 1 < args.length) {
          final int argument = Integer.parseInt(args[i + 1]);
          if (argument < 16 && argument > 2) {
            if (argument % 2 == 0) {
              throw new IllegalArgumentException("--size must be an odd number");
            }
            config.setOption("board-size", args[++i]);
          } else if (argument >= 20 && argument <= 99999) {
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
            i++;
            throw new IllegalArgumentException("--walls requires to be higher than 0");
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
          throw new IllegalArgumentException("--size requires an integer");
        }
      }
      default -> {
        return -1; // Not a game option
      }
    }
    return i;
  }
}