package fr.bordeaux.AI;

import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Move;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;
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
   */
  public AiPlayer(String name, Position positionStart) {
    super(name, positionStart);
  }

  /**
   * Protected constructor used primarily for cloning or restoring an AI player state.
   *
   * @param name The name of the AI player.
   * @param current The current position of the AI.
   * @param start The starting position of the AI.
   * @param walls The number of walls remaining for this AI.
   * @param score The current score of the AI.
   */
  protected AiPlayer(String name, Position current, Position start, int walls, int score) {
    super(name, current, start, walls, score);
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
