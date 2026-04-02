package fr.bordeaux.ai;

import fr.bordeaux.core.Color;
import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;

/**
 * Basic heuristic implementation focusing on direct forward progress. It calculates the score based
 * on the distance the AI has traveled from its starting side toward the opposite goal line.
 */
public class SimpleHeuristic implements HeuristicEvaluator {

  /** The identifier of the AI player to evaluate. */
  private final Color aiColor;

  /**
   * Constructs a new SimpleHeuristic.
   *
   * @param aiColorParameter The color used to identify the AI player in the game state.
   */
  public SimpleHeuristic(final Color aiColorParameter) {
    this.aiColor = aiColorParameter;
  }

  /**
   * Evaluates progress by comparing the current position with the starting row or column.
   *
   * @param gameState The current game state.
   * @return An integer representing the steps taken toward the goal.
   * @throws IllegalStateException if the AI player is not found in the game state.
   */
  @Override
  public int evaluate(final GameState gameState) {

    Player player = null;
    for (final Player otherPlayer : gameState.getPlayers()) {
      if (otherPlayer.getColor() == this.aiColor) {
        player = otherPlayer;
        break;
      }
    }
    if (player == null) {
      throw new IllegalStateException("AI player not found");
    }

    final Position startPos = player.getStartingPosition();
    final Position actualPos = player.getPosition();
    final int evaluateHeuristic;
    if (startPos.getX() == 0) {
      evaluateHeuristic = actualPos.getX() - startPos.getX();
    } else {
      evaluateHeuristic = startPos.getX() - actualPos.getX();
    }
    return evaluateHeuristic;
  }
}
