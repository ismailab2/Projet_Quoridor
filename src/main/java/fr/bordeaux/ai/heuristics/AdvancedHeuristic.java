package fr.bordeaux.ai.heuristics;

import fr.bordeaux.ai.base.HeuristicEvaluator;
import fr.bordeaux.core.Board;
import fr.bordeaux.core.Color;
import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;
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

  /** Maximum possible score for a winning state. */
  private static final int MAX_SCORE = 10_000;

  /** Minimum possible score for a losing state. */
  private static final int MIN_SCORE = -10_000;

  /** Weight for the shortest path distance difference. */
  private static final int PATH_WEIGHT = 40;

  /** Bonus for placing a wall on the opponent's path. */
  private static final int WALL_ON_PATH_BONUS = 80;

  /** Base value of a single wall in resource scoring. */
  private static final int WALL_BASE_VALUE = 8;

  /** Multiplier for the progress bonus based on distance from start. */
  private static final int PROGRESS_MULTIPLIER = 5;

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
   * Evaluates the current game state from the perspective of the AI player.
   *
   * @param gameState The current state of the game to evaluate.
   * @return An integer score where higher values favor the AI and lower values favor the opponent.
   */
  @Override
  public int evaluate(final GameState gameState) {
    Player player = null;
    Player enemy = null;
    final List<Player> players = gameState.getPlayers();
    for (final Player eachPlayer : players) {
      if (eachPlayer.getColor() == aiColor) {
        player = eachPlayer;
      } else {
        enemy = eachPlayer;
      }
    }

    if (player == null || enemy == null) {
      return 0;
    }

    final List<Integer> playerPath = bfsPath(gameState, player);
    final List<Integer> enemyPath = bfsPath(gameState, enemy);

    final int result;
    if (playerPath.size() <= 1) {
      result = MAX_SCORE;
    } else if (enemyPath.size() <= 1) {
      result = MIN_SCORE;
    } else {
      final int playerDist = playerPath.size();
      final int enemyDist = enemyPath.size();
      final int remainingWalls = player.getRemainingWalls() + enemy.getRemainingWalls();

      if (remainingWalls == 0) {
        final int diff = enemyDist - playerDist;
        if (diff > 0) {
          result = MAX_SCORE - playerDist;
        } else if (diff < 0) {
          result = MIN_SCORE + enemyDist;
        } else {
          result = 0;
        }
      } else {
        final int scorePath = (enemyDist - playerDist) * PATH_WEIGHT;
        final Board board = gameState.getBoard();
        final int boardSize = board.getSize();
        final double progress = 1.0 - (double) playerDist / boardSize;
        final double wallCoeff = 1.0 - 0.5 * progress;

        final int scoreWalls =
            (int)
                ((player.getRemainingWalls() - enemy.getRemainingWalls())
                    * WALL_BASE_VALUE
                    * wallCoeff);

        final int enemyDetour = enemyDist - manhattanToGoal(gameState, enemy);
        final int scoreDetour = enemyDetour * WALL_ON_PATH_BONUS;
        final int progressBonus = getDistanceFromStart(gameState, player) * PROGRESS_MULTIPLIER;

        result = scorePath + scoreWalls + scoreDetour + progressBonus;
      }
    }
    return result;
  }

  /**
   * Calculates the shortest path to the goal using Breadth-First Search (BFS). This accounts for
   * all walls currently placed on the board.
   *
   * @param state The current game state.
   * @param player The player for whom the path is being calculated.
   * @return A list of node IDs representing the shortest path. An empty list or size <= 1 indicates
   *     the goal has been reached.
   */
  private List<Integer> bfsPath(final GameState state, final Player player) {
    final Board board = state.getBoard();
    final Position pos = player.getPosition();
    final int startId = board.getCellId(pos.getX(), pos.getY());
    return board.getGraph().findPathBFS(startId, state.getWinCondition(player));
  }

  /**
   * Calculates the Manhattan distance to the goal as if the board were empty.
   *
   * <p>This serves as a "free-grid" baseline. By comparing this to the actual BFS path, the AI can
   * determine how much a player has been delayed by walls.
   *
   * @param state The current game state.
   * @param player The player to evaluate.
   * @return The minimum number of moves to the goal ignoring all walls.
   */
  private int manhattanToGoal(final GameState state, final Player player) {
    final Board board = state.getBoard();
    final int boardSize = board.getSize();
    final Position currentPos = player.getPosition();
    final Position startPos = player.getStartingPosition();
    final int posX = currentPos.getX();
    final int posY = currentPos.getY();
    final int startX = startPos.getX();
    final int startY = startPos.getY();

    final int result;
    if (startX == 0) {
      result = (boardSize - 1) - posX;
    } else if (startX == boardSize - 1) {
      result = posX;
    } else if (startY == 0) {
      result = (boardSize - 1) - posY;
    } else {
      result = posY;
    }
    return result;
  }

  /**
   * Measures the physical distance a player has traveled from their starting line.
   *
   * <p>This is used to provide a "progress bonus," encouraging the AI to move forward even when the
   * shortest path length remains unchanged.
   *
   * @param state The current game state.
   * @param player The player to evaluate.
   * @return The number of steps taken from the starting edge.
   */
  private int getDistanceFromStart(final GameState state, final Player player) {
    final Board board = state.getBoard();
    final int boardSize = board.getSize();
    final Position currentPos = player.getPosition();
    final Position startPos = player.getStartingPosition();
    final int posX = currentPos.getX();
    final int posY = currentPos.getY();
    final int startX = startPos.getX();
    final int startY = startPos.getY();

    final int result;
    if (startX == 0) {
      result = posX;
    } else if (startX == boardSize - 1) {
      result = (boardSize - 1) - posX;
    } else if (startY == 0) {
      result = posY;
    } else {
      result = (boardSize - 1) - posY;
    }
    return result;
  }
}
