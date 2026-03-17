package fr.bordeaux.ai;

import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;
import java.util.List;

/**
 * Intermediate heuristic that evaluates the relative advantage between players. This strategy
 * calculates the difference between the AI's progress and the opponent's progress to determine who
 * is closer to winning.
 */
public class MediumHeuristic implements HeuristicEvaluator {

  /** The identifier of the AI player. */
  private final String aiName;

  /**
   * Constructs a MediumHeuristic.
   *
   * @param aiNameParameter The name of the AI player.
   */
  public MediumHeuristic(final String aiNameParameter) {
    this.aiName = aiNameParameter;
  }

  /**
   * Calculates a relative score by subtracting the opponent's progress from the AI's progress.
   *
   * @param gameState The current game state.
   * @return A score representing the lead (or delay) relative to the opponent.
   * @throws IllegalStateException if a player is missing from the state.
   */
  @Override
  public int evaluate(final GameState gameState) {
    final List<Player> listPlayer = gameState.getPlayers();
    Player enemie = null;
    Player player = null;
    for (final Player otherPlayer : listPlayer) {
      if (otherPlayer.getName().equals(this.aiName)) {
        player = otherPlayer;
      } else {
        enemie = otherPlayer;
      }
    }

    if (enemie == null) {
      throw new IllegalStateException("No enemie found !");
    }
    final Position pStartingPosition = player.getStartingPosition();
    final Position pCurrentPosition = player.getPosition();
    final Position eStartingPosition = enemie.getStartingPosition();
    final Position eCurrentPosition = enemie.getPosition();
    final int evaluateHeuristic;

    if (pStartingPosition.getX() == 0) {
      evaluateHeuristic =
          pCurrentPosition.getX()
              - pStartingPosition.getX()
              - eStartingPosition.getX()
              - eCurrentPosition.getX();
    } else if (pStartingPosition.getX() == gameState.getBoard().getSize() - 1) {
      evaluateHeuristic =
          pStartingPosition.getX()
              - pCurrentPosition.getX()
              - eCurrentPosition.getX()
              - eStartingPosition.getX();
    } else if (pStartingPosition.getY() == 0) {
      evaluateHeuristic =
          pCurrentPosition.getY()
              - pStartingPosition.getY()
              - eStartingPosition.getY()
              - eCurrentPosition.getY();
    } else {
      evaluateHeuristic =
          pStartingPosition.getY()
              - pCurrentPosition.getY()
              - eCurrentPosition.getY()
              - eStartingPosition.getY();
    }
    return evaluateHeuristic;
  }
}
