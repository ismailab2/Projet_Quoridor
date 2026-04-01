package fr.bordeaux.ai;

import fr.bordeaux.core.*;
import java.util.Map;

/**
 * Abstract base class representing an Artificial Intelligence player in the game. Extends the core
 * {@link Player} class.
 */
public abstract class AiPlayer extends Player {

  /**
   * Constructs a new AI player with a given name and starting position.
   *
   * @param name The name of the AI player.
   * @param positionStart The starting position of the AI on the board.
   * @param color The color of the player.
   * @param walls The amount of wall the player has.
   */
  public AiPlayer(final String name, final Position positionStart, Color color, int walls) {
    super(name, positionStart, color, walls);
  }

  /**
   * Protected constructor used primarily for cloning or restoring an AI player state.
   *
   * @param name The name of the AI player.
   * @param current The current position of the AI.
   * @param start The starting position of the AI.
   * @param walls The number of walls remaining for this AI.
   * @param score The current score of the AI.
   * @param color The color of the AI.
   */
  protected AiPlayer(
      final String name,
      final Position current,
      final Position start,
      final int walls,
      final int score,
      final Color color) {
    super(name, current, start, walls, score, color);
  }

  /**
   * Indicates whether this player is controlled by an AI.
   *
   * @return {@code true} since this class represents an AI player.
   */
  @Override
  public Boolean isAI() {
    return true;
  }

  /**
   * Creates and returns a deep copy of this AI player.
   *
   * @return A new instance of {@link Player} with the same state.
   */
  @Override
  public abstract Player copy();

  @Override
  public abstract Move getNextMove(GameState state);

  /**
   * Retrieves the specific configuration settings of the AI (e.g., depth, heuristic, reflection
   * time).
   *
   * @return A map containing the AI's settings as key-value pairs.
   */
  public abstract Map<String, Object> getSettings();
}
