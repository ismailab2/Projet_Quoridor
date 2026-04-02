package fr.bordeaux.ai;

import fr.bordeaux.core.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Implementation of an AI player that selects moves randomly from the list of legal moves. */
public class RandomPlayer extends AiPlayer {

  /**
   * Constructs a RandomPlayer with a name and a starting position.
   *
   * @param name The name of the player.
   * @param positionStart The initial starting position on the board.
   * @param color The AI's color on the board.
   */
  public RandomPlayer(final String name, final Position positionStart, final Color color) {
    super(name, positionStart, color);
  }

  /**
   * Determines the next move by picking one at random from all legal moves.
   *
   * @param state The current state of the game.
   * @return A randomly selected legal {@link Move}.
   */
  @Override
  public Move getNextMove(final GameState state) {
    List<Move> l = state.generateLegalMoves();
    Random random = new Random();
    return l.get(random.nextInt(l.size()));
  }

  /**
   * Returns a string representation of the Random AI player.
   *
   * @return A string containing the AI type and player details.
   */
  @Override
  public String toString() {
    return "Random AI " + super.toString();
  }

  /**
   * Creates a deep copy of this RandomPlayer.
   *
   * @return A new instance of {@link RandomPlayer} with the same state.
   */
  @Override
  public Player copy() {
    return new RandomPlayer(
        this.getName(),
        this.getPosition(),
        this.getStartingPosition(),
        this.getRemainingWalls(),
        this.getScore(),
        this.getColor());
  }

  /**
   * Private constructor used for internal state management and cloning.
   *
   * @param name The name of the player.
   * @param current The current position of the player.
   * @param start The initial starting position.
   * @param walls The number of remaining walls.
   * @param score The current score of the player.
   */
  private RandomPlayer(
      final String name,
      final Position current,
      final Position start,
      final int walls,
      final int score,
      final Color color) {
    super(name, current, start, walls, score, color);
  }

  /**
   * Indicates that this player is controlled by an AI.
   *
   * @return {@code true} since this is a RandomPlayer AI.
   */
  @Override
  public Boolean isAI() {
    return true;
  }

  /**
   * Retrieves the specific settings for this AI player.
   *
   * @return A map containing the AI mode configuration.
   */
  @Override
  public Map<String, Object> getSettings() {
    Map<String, Object> settings = new HashMap<>();
    settings.put("mode", "random");
    return settings;
  }
}
