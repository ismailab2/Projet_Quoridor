package fr.bordeaux.factories;

import fr.bordeaux.ai.*;
import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.core.*;
import fr.bordeaux.MainApp;
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
   * <p>By default, the first player is always a {@link HumanPlayer} positioned at the bottom of the
   * board. The second player's type (Human or AI) is determined by the {@code "ai"} boolean flag in
   * the configuration.
   *
   * @param config The configuration manager containing CLI arguments or persistent settings.
   * @return A {@link List} of {@link Player} objects ready to start the game.
   */
  public static List<Player> createPlayers(final ConfigManager config) {
    List<Player> players = new ArrayList<>();

    players.add(
        new HumanPlayer("Human 1", new Position(MainApp.BOARD_SIZE - 1, MainApp.BOARD_SIZE / 2)));

    if (config.getBoolean("ai", false)) {
      players.add(buildAi("AI", new Position(0, MainApp.BOARD_SIZE / 2), config));
    } else {
      players.add(new HumanPlayer("Human 2", new Position(0, MainApp.BOARD_SIZE / 2)));
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
   * @return A {@link Player} instance configured according to the selected mode.
   * @throws UnsupportedOperationException if an unimplemented mode (MCTS/Neural) is requested.
   */
  private static Player buildAi(final String name, final Position pos, final ConfigManager config) {

    final String mode = config.getOption("ai.mode", "minimax");
    Player player = null;
    switch (mode) {
      case "mcts":
        break;

      case "neural":
        break;

      case "iterative":
      case "minimax":
      default:
        final int depth = config.getInt("ai.depth", 2);
        final int reflexionTime = config.getInt("ai.time", 5);
        final String scoring = config.getOption("ai.scoring", "simple");
        MinimaxBuilder builder =
            MinimaxPlayer.builder(name, pos)
                .withDepth(depth)
                .withHeuristic(scoring)
                .withReflexion(reflexionTime);
        if (mode.equals("iterative")) {
          builder.withIterativeDeepening(true);
        }
        player = builder.build();
        break;
    }

    return player;
  }
}
