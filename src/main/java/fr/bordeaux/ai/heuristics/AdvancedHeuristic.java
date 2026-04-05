package fr.bordeaux.ai.heuristics;

import fr.bordeaux.ai.base.HeuristicEvaluator;
import fr.bordeaux.core.Color;
import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Player;
import java.util.List;

/**
 * An advanced heuristic evaluator for the Quoridor game. *
 *
 * <p>This evaluator uses a combination of pathfinding (BFS), resource management, and tactical
 * analysis to score a game state. It goes beyond simple distance by calculating the actual shortest
 * path around walls and rewarding moves that force the opponent into long detours. *
 *
 * <p>The scoring pillars are:
 *
 * <ul>
 *   <li><b>Path Advantage:</b> Difference between the player's and opponent's actual distances to
 *       the goal.
 *   <li><b>Resource Management:</b> Evaluation of remaining walls, weighted by game progression.
 *   <li><b>Tactical Detours:</b> Bonuses for walls that significantly increase the opponent's path
 *       length compared to a free board.
 *   <li><b>Momentum:</b> A small bonus for physical advancement toward the goal.
 * </ul>
 */
public class AdvancedHeuristic implements HeuristicEvaluator {

  private static final int MAX_SCORE = 10_000;
  private static final int MIN_SCORE = -10_000;

  private static final int PATH_WEIGHT = 40;

  private static final int WALL_ON_PATH_BONUS = 80;

  private static final int WALL_BASE_VALUE = 8;

  private final Color aiColor;

  /**
   * Constructs a AdvancedHeuristic.
   *
   * @param aiColor The color of the AI player.
   */
  public AdvancedHeuristic(final Color aiColor) {
    this.aiColor = aiColor;
  }

  /**
   * Evaluates the current game state from the perspective of the AI player. * @param gameState The
   * current state of the game to evaluate.
   *
   * @return An integer score where higher values favor the AI and lower values favor the opponent.
   */
  @Override
  public int evaluate(final GameState gameState) {
    Player player = null;
    Player enemy = null;
    for (Player p : gameState.getPlayers()) {
      if (p.getColor() == aiColor) player = p;
      else enemy = p;
    }

    List<Integer> playerPath = bfsPath(gameState, player);
    List<Integer> enemyPath = bfsPath(gameState, enemy);

    if (playerPath.size() <= 1) return MAX_SCORE;
    if (enemyPath.size() <= 1) return MIN_SCORE;

    int playerDist = playerPath.size();
    int enemyDist = enemyPath.size();

    int totalWalls = player.getRemainingWalls() + enemy.getRemainingWalls();
    boolean pureRace = totalWalls == 0;

    if (pureRace) {
      int diff = enemyDist - playerDist;
      if (diff > 0) return MAX_SCORE - playerDist;
      if (diff < 0) return MIN_SCORE + enemyDist;
      return 0;
    }

    int scorePath = (enemyPath.size() - playerPath.size()) * PATH_WEIGHT;

    int boardSize = gameState.getBoard().getSize();
    double progress = 1.0 - (double) playerPath.size() / boardSize;
    double wallCoeff = 1.0 - 0.5 * progress;

    int scoreWalls =
        (int)
            ((player.getRemainingWalls() - enemy.getRemainingWalls())
                * WALL_BASE_VALUE
                * wallCoeff);

    int enemyDetour = enemyPath.size() - manhattanToGoal(gameState, enemy);
    int scoreDetour = enemyDetour * WALL_ON_PATH_BONUS;

    int progressBonus = getDistanceFromStart(gameState, player) * 5;

    return scorePath + scoreWalls + scoreDetour + progressBonus;
  }

  /**
   * Calculates the shortest path to the goal using Breadth-First Search (BFS). This accounts for
   * all walls currently placed on the board. * @param state The current game state.
   *
   * @param player The player for whom the path is being calculated.
   * @return A list of node IDs representing the shortest path. An empty list or size <= 1 indicates
   *     the goal has been reached.
   */
  private List<Integer> bfsPath(GameState state, Player player) {
    int startId = state.getBoard().id(player.getPosition().getX(), player.getPosition().getY());
    return state.getBoard().getGraph().findPathBFS(startId, state.getWinCondition(player));
  }

  /**
   * Calculates the Manhattan distance to the goal as if the board were empty. *
   *
   * <p>This serves as a "free-grid" baseline. By comparing this to the actual BFS path, the AI can
   * determine how much a player has been delayed by walls. * @param state The current game state.
   *
   * @param player The player to evaluate.
   * @return The minimum number of moves to the goal ignoring all walls.
   */
  private int manhattanToGoal(GameState state, Player player) {
    int size = state.getBoard().getSize();
    int x = player.getPosition().getX();
    int y = player.getPosition().getY();
    int startX = player.getStartingPosition().getX();
    int startY = player.getStartingPosition().getY();

    if (startX == 0) return (size - 1) - x;
    if (startX == size - 1) return x;
    if (startY == 0) return (size - 1) - y;
    return y;
  }

  /**
   * Measures the physical distance a player has traveled from their starting line. *
   *
   * <p>This is used to provide a "progress bonus," encouraging the AI to move forward even when the
   * shortest path length remains unchanged. * @param state The current game state.
   *
   * @param player The player to evaluate.
   * @return The number of steps taken from the starting edge.
   */
  private int getDistanceFromStart(GameState state, Player player) {
    int size = state.getBoard().getSize();
    int x = player.getPosition().getX();
    int y = player.getPosition().getY();
    int startX = player.getStartingPosition().getX();
    int startY = player.getStartingPosition().getY();

    if (startX == 0) return x;
    if (startX == size - 1) return (size - 1) - x;
    if (startY == 0) return y;
    return (size - 1) - y;
  }
}
