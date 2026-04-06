package fr.bordeaux.main;

import fr.bordeaux.ai.base.*;
import fr.bordeaux.ai.mcts.*;
import fr.bordeaux.ai.minimax.*;
import fr.bordeaux.ai.random.*;
import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.core.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory class responsible for player instantiation in the Quoridor game.
 *
 * <p>This class acts as a "Composition Root," bridging the gap between the game's {@code core}
 * logic, the various {@code ai} implementations, and the {@link ConfigManager} without polluting
 * the core domain with external dependencies. * @see Player
 *
 * @see HumanPlayer
 * @see MinimaxPlayer
 */
public final class PlayerFactory {

  /** Private constructor to prevent instantiation of this utility class. */
  private PlayerFactory() {}

  /**
   * Creates and initializes the list of players for a game session based on the provided
   * configuration.
   *
   * <p>Players are created without walls; the {@link fr.bordeaux.core.GameEngine} is responsible
   * for applying the wall count from the configuration after initialization.
   *
   * @param config The configuration manager containing CLI arguments or persistent settings.
   * @return A {@link List} of {@link Player} objects ready to start the game.
   */
  public static List<Player> createPlayers(final ConfigManager config) {
    final List<Player> players = new ArrayList<>();
    final int size = ConfigManager.getInstance().getInt("board-size", 9);
    if (config.getBoolean("ai1", false)) {
      players.add(buildAi("AI WHITE", new Position(0, size / 2), config, Color.WHITE));
    } else {
      players.add(new HumanPlayer("Human White", new Position(0, size / 2), Color.WHITE));
    }

    if (config.getBoolean("ai2", false)) {
      players.add(buildAi("AI BLACK", new Position(size - 1, size / 2), config, Color.BLACK));
    } else {
      players.add(new HumanPlayer("Human Black", new Position(size - 1, size / 2), Color.BLACK));
    }

    final int nbPlayers = Integer.parseInt(config.getOption("nb-players", "2"));
    if (nbPlayers > 2 && nbPlayers <= 4) {
      players.add(new HumanPlayer("Human Blue", new Position(size / 2, 0), Color.BLUE));
      if (nbPlayers == 4) {
        players.add(new HumanPlayer("Human Red", new Position(size / 2, size - 1), Color.RED));
      }
    }

    return players;
  }

  /**
   * Configures and builds an Artificial Intelligence player instance.
   *
   * <p>This method extracts specific AI parameters from the configuration:
   *
   * <ul>
   *   <li>{@code ai.mode}: The algorithm type (minimax, iterative, mcts, neural).
   *   <li>{@code ai.depth}: Search depth for tree-based algorithms (defaults to 2).
   *   <li>{@code ai.time}: Reflection time limit in seconds (defaults to 5).
   *   <li>{@code ai.scoring}: The heuristic evaluation function to use (defaults to "simple").
   * </ul>
   *
   * @param name The name assigned to the AI player.
   * @param pos The starting position on the board.
   * @param config The configuration source for AI parameters.
   * @param color The color of the AI player.
   * @return A {@link Player} instance configured according to the selected mode.
   * @throws UnsupportedOperationException if an unimplemented mode (MCTS/Neural) is requested.
   */
  private static Player buildAi(
      final String name, final Position pos, final ConfigManager config, final Color color) {

    // Convert the Color enum to its ID string (e.g., "0" or "1") to match config keys
    final String colorId = String.valueOf(color.getId());
    final String prefix = "ai" + colorId;

    final String mode = config.getOption(prefix + ".mode", "minimax");
    Player player = null;

    switch (mode) {
      case "mcts":
        // Get reflection time and convert to milliseconds
        final long reflexionTimeMs = config.getInt(prefix + ".time", 5) * 1000L;
        final String selection = config.getOption(prefix + ".selection", "UCT");
        final boolean useNN = selection.equalsIgnoreCase("ML");

        try {
          player = new MctsPlayer(name, pos, reflexionTimeMs, useNN, color);
        } catch (java.io.IOException e) {
          throw new IllegalStateException(
              "Error initializing MCTS AI (" + selection + "): " + e.getMessage(), e);
        }
        break;

      case "iterative":
      case "minimax":
      default:
        final int reflexionTime = config.getInt(prefix + ".time", 5);
        final int depth = config.getInt(prefix + ".depth", depthBasedOnTime(reflexionTime));
        final String scoring = config.getOption(prefix + ".scoring", "simple");

        final MinimaxBuilder builder =
            MinimaxPlayer.builder(name, pos, color)
                .withDepth(depth)
                .withHeuristic(scoring)
                .withReflexion(reflexionTime);

        if ("iterative".equals(mode)) {
          builder.withIterativeDeepening(true);
        }
        player = builder.build();
        break;
    }

    return player;
  }

  /**
   * Determines the AI search depth based on the allocated reflection time.
   *
   * @param reflexionTime the time allowed for the AI to think, in seconds.
   * @return the calculated search depth (from 2 to 5).
   */
  private static int depthBasedOnTime(final int reflexionTime) {
    final int result;
    if (reflexionTime <= 5) {
      result = 2;
    } else if (reflexionTime <= 7) {
      result = 3;
    } else if (reflexionTime <= 10) {
      result = 4;
    } else {
      result = 5;
    }

    return result;
  }
}
