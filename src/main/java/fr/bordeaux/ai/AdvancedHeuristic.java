package fr.bordeaux.ai;

import fr.bordeaux.core.Color;
import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Player;
import java.util.List;

/**
 * Advanced heuristic using pathfinding and resource management. It utilizes Breadth-First Search
 * (BFS) to find the actual shortest path to the goal (accounting for walls) and factors in the
 * number of remaining walls for both players.
 */
public class AdvancedHeuristic implements HeuristicEvaluator {

  /** Score assigned for an immediate win. */
  private static final int MAX_SCORE = 10_000;

  /** Score assigned for an immediate loss. */
  private static final int MIN_SCORE = -10_000;

  /** The identifier of the AI player. */
  private final Color aiColor;

  /**
   * Constructs an AdvancedHeuristic.
   *
   * @param aiColorParameter The color of the AI player.
   */
  public AdvancedHeuristic(final Color aiColorParameter) {
    this.aiColor = aiColorParameter;
  }

  /**
   * Evaluates the state using BFS path lengths and weighted wall counts.
   *
   * @param gameState The current game state.
   * @return A comprehensive score representing the tactical advantage.
   */
  @Override
  public int evaluate(final GameState gameState) {
    final int evaluateHeuristic;
    final float coeff1 = 2.0f;
    final float coeff2 = 1.0f;

    final List<Player> listPlayer = gameState.getPlayers();
    Player enemie = null;
    Player player = null;
    for (final Player otherPlayer : listPlayer) {
      if (otherPlayer.getColor() == this.aiColor) {
        player = otherPlayer;
      } else {
        enemie = otherPlayer;
      }
    }

    final List<Integer> lMax =
        gameState
            .getBoard()
            .getGraph()
            .findPathBFS(
                gameState.getBoard().id(player.getPosition().getX(), player.getPosition().getY()),
                gameState.getWinCondition(player));

    final List<Integer> lMin =
        gameState
            .getBoard()
            .getGraph()
            .findPathBFS(
                gameState.getBoard().id(enemie.getPosition().getX(), enemie.getPosition().getY()),
                gameState.getWinCondition(enemie));

    final int nbWallPlayer = player.getRemainingWalls();
    final int nbWallEnemie = enemie.getRemainingWalls();
    final int criticalSize = 1;
    if (lMax.size() <= criticalSize) {
      evaluateHeuristic = MAX_SCORE;
    } else if (lMin.size() <= criticalSize) {
      evaluateHeuristic = MIN_SCORE;
    } else {
      final int scoreBFS = (lMin.size() - lMax.size()) * 40;
      final int scoreWall = (int) (nbWallPlayer * coeff1 - nbWallEnemie * coeff2);
      evaluateHeuristic = scoreBFS + scoreWall;
    }

    return evaluateHeuristic;
  }
}
