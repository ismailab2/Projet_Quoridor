package fr.bordeaux.ai;

import fr.bordeaux.core.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * Represents an AI player using the Minimax algorithm for the Quorridor game. Features advanced
 * capabilities such as Alpha-Beta pruning, Iterative Deepening, and a configurable reflection time
 * limit. Note: AI are currently limited to 2-player scenarios. They may not function correctly if
 * the number of players is larger.
 */
public final class MinimaxPlayer extends AiPlayer {

  /** The maximum depth the AI will search if iterative deepening is disabled. */
  private final int searchDepth;

  /** The maximum reflection time allowed for the AI in seconds. */
  private final int reflexionTime;

  /** The operational mode function applied to find the next move. */
  private final BiFunction<GameState, Long, Move> mode;

  /** The heuristic evaluation strategy. */
  private final HeuristicEvaluator heuristicFunction;

  /** Stack used to keep track of moves for backtracking. */
  private final Deque<Move> aiStack = new ArrayDeque<>();

  /** Flag indicating whether iterative deepening is enabled. */
  private final boolean useIterativeD;

  /** The name of the heuristic algorithm to use. */
  private final String heuristicName;

  /**
   * Private constructor used by the Builder to instantiate the AI.
   *
   * @param builder The builder containing configuration settings.
   */
  public MinimaxPlayer(final MinimaxBuilder builder) {
    super(builder.getName(), builder.getStartingPosition(), builder.getColor(), builder.getWalls());
    this.searchDepth = builder.getSearchDepth();
    this.reflexionTime = builder.getLimitTime();
    this.useIterativeD = builder.isUseIterativeDeepening();
    this.heuristicName = builder.getHeuristicName();

    if (this.useIterativeD) {
      this.mode = this::iterativeDeepening;
    } else {
      this.mode =
          (final GameState state, final Long endTime) -> miniMax(state, this.searchDepth, endTime);
    }

    this.heuristicFunction = determineHeuristic(this.heuristicName);
  }

  /**
   * Private constructor used by the copy() method for cloning the AI player. This constructor
   * allows for a complete state transfer including Minimax specific configurations like search
   * depth and heuristic type.
   *
   * @param name The name of the AI player.
   * @param current The current position of the pawn.
   * @param start The starting position of the pawn.
   * @param walls The number of remaining walls.
   * @param score The current player score.
   * @param depth The maximum search depth for the Minimax algorithm.
   * @param timeLimit The maximum time allowed for reflection in seconds.
   * @param iterativeDeepVal Whether to use iterative deepening during search.
   * @param heuristicVal The name of the heuristic function to be used.
   */
  private MinimaxPlayer(
      final String name,
      final Position current,
      final Position start,
      final int walls,
      final int score,
      final int depth,
      final int timeLimit,
      final boolean iterativeDeepVal,
      final String heuristicVal,
      final Color color) {
    super(name, current, start, walls, score, color);
    this.searchDepth = depth;
    this.reflexionTime = timeLimit;
    this.useIterativeD = iterativeDeepVal;
    this.heuristicName = heuristicVal;

    if (this.useIterativeD) {
      this.mode = this::iterativeDeepening;
    } else {
      this.mode =
          (final GameState state, final Long endTime) -> miniMax(state, this.searchDepth, endTime);
    }
    this.heuristicFunction = determineHeuristic(this.heuristicName);
  }

  /**
   * Static factory method for the builder.
   *
   * @param name The player name.
   * @param pos The starting position.
   * @param color The AI's color on the board.
   * @return A new Builder instance.
   */
  public static MinimaxBuilder builder(final String name, final Position pos, final Color color) {
    return new MinimaxBuilder(name, pos, color);
  }

  /**
   * Sets the heuristic function based on the provided name.
   *
   * @param heuristicNameP The name of the heuristic ("simple", "intermediate", or "advanced").
   * @return The corresponding heuristic class.
   */
  private HeuristicEvaluator determineHeuristic(final String heuristicNameP) {
    final HeuristicEvaluator chosenHeuristic;
    switch (heuristicNameP) {
      case "intermediate":
        chosenHeuristic = new MediumHeuristic(this.getColor());
        break;
      case "advanced":
        chosenHeuristic = new AdvancedHeuristic(this.getColor());
        break;
      case "simple":
      default:
        chosenHeuristic = new SimpleHeuristic(this.getColor());
        break;
    }
    return chosenHeuristic;
  }

  /**
   * Computes and returns the best possible move within the allocated time limit.
   *
   * @param state The current game state.
   * @return The best move found.
   */
  @Override
  public Move getNextMove(final GameState state) {

    final long deadlineTime = System.currentTimeMillis() + (this.reflexionTime * 1000L);
    Move bestMove = this.mode.apply(state, deadlineTime);

    if (bestMove == null) {
      bestMove = randomMove(state);
    }
    return bestMove;
  }

  /**
   * Implements Iterative Deepening search safely within the main thread.
   *
   * @param state The current game state.
   * @param endTime The timestamp when the search must abort.
   * @return The best move found at the maximum depth reached before timeout.
   */
  private Move iterativeDeepening(final GameState state, final long endTime) {
    int currentDepth = 1;
    Move lastBestMove = null;

    while (System.currentTimeMillis() < endTime) {
      final Move tempMove = miniMax(state, currentDepth, endTime);

      if (System.currentTimeMillis() >= endTime) {
        break;
      }
      if (tempMove != null) {
        lastBestMove = tempMove;
      }
      currentDepth++;
    }
    return lastBestMove;
  }

  /**
   * Entry point for the Minimax algorithm. Explores the first level of legal moves.
   *
   * @param gameState The current game state.
   * @param maxDepth The search depth.
   * @param endTime The deadline for the search.
   * @return The best evaluated move.
   */
  private Move miniMax(final GameState gameState, final int maxDepth, final long endTime) {

    Move bestMove = null;
    int bestScore = Integer.MIN_VALUE;
    final GameState searchState = gameState.copy();
    for (final Move currentMove : searchState.generateLegalMoves()) {
      if (System.currentTimeMillis() >= endTime) {
        break;
      }
      push(searchState, currentMove);
      final int score =
          minMax(searchState, maxDepth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, endTime);
      if (score > bestScore || bestMove == null) {
        bestScore = score;
        bestMove = currentMove;
      }
      pop(searchState);
    }
    return bestMove;
  }

  /**
   * @param gameState The game state.
   * @param currentDepth The remaining depth.
   * @param alpha The best value guaranteed for MAX.
   * @param beta The best value guaranteed for MIN.
   * @param endTime The search deadline.
   * @return The maximized score.
   */
  private int maxMin(
      final GameState gameState,
      final int currentDepth,
      final int alpha,
      final int beta,
      final long endTime) {
    final int resultScore;
    if (gameState.isGameOver() || currentDepth == 0 || System.currentTimeMillis() >= endTime) {
      resultScore = this.heuristicFunction.evaluate(gameState);
    } else {
      int currentAlpha = alpha;
      for (final Move move : gameState.generateLegalMoves()) {
        push(gameState, move);
        currentAlpha =
            Math.max(
                currentAlpha, minMax(gameState, currentDepth - 1, currentAlpha, beta, endTime));
        pop(gameState);

        if (currentAlpha >= beta) {
          currentAlpha = beta;
          break;
        }
      }
      resultScore = currentAlpha;
    }
    return resultScore;
  }

  /**
   * @param gameState The game state.
   * @param currentDepth The remaining depth.
   * @param alpha The best value guaranteed for MAX.
   * @param beta The best value guaranteed for MIN.
   * @param endTime The search deadline.
   * @return The minimized score.
   */
  private int minMax(
      final GameState gameState,
      final int currentDepth,
      final int alpha,
      final int beta,
      final long endTime) {
    final int resultScore;

    if (gameState.isGameOver() || currentDepth == 0 || System.currentTimeMillis() >= endTime) {
      resultScore = this.heuristicFunction.evaluate(gameState);
    } else {
      int currentBeta = beta;
      for (final Move move : gameState.generateLegalMoves()) {
        push(gameState, move);
        currentBeta =
            Math.min(currentBeta, maxMin(gameState, currentDepth - 1, alpha, currentBeta, endTime));
        pop(gameState);

        if (alpha >= currentBeta) {
          currentBeta = alpha;
          break;
        }
      }
      resultScore = currentBeta;
    }
    return resultScore;
  }

  /**
   * Plays a random move if time limit expires before any valid evaluation.
   *
   * @param gameState The game state.
   * @return A random legal move.
   */
  private Move randomMove(final GameState gameState) {
    final List<Move> legalMoves = gameState.generateLegalMoves();
    final Random randomGenerator = new Random();
    return legalMoves.get(randomGenerator.nextInt(legalMoves.size()));
  }

  /**
   * Simulates a move and updates the game state, pushing it to the stack for backtracking.
   *
   * @param gameState The current game state.
   * @param move The move to simulate.
   */
  private void push(final GameState gameState, final Move move) {
    final Player currentPlayer = gameState.getCurrentPlayer();
    if (move.isPawn()) {
      currentPlayer.setPosition(move.getTo());
    } else {
      gameState.getBoard().addWall(move.getTo(), move.getOrientation());
      currentPlayer.useWall();
    }

    this.aiStack.push(move);
    gameState.getNextPlayer();
  }

  /**
   * Backtracks the last simulated move by restoring the previous game state.
   *
   * @param gameState The game state to restore.
   */
  private void pop(final GameState gameState) {
    if (!this.aiStack.isEmpty()) {
      final Move lastMove = this.aiStack.pop();
      gameState.previousPlayer();
      final Player currentPlayer = gameState.getCurrentPlayer();

      if (lastMove.isPawn()) {
        currentPlayer.setPosition(lastMove.getFrom());
      } else {
        gameState.getBoard().removeWall(lastMove.getTo(), lastMove.getOrientation());
        currentPlayer.giveBackWall();
      }
    }
  }

  /**
   * Get the search depth of the MiniMax Player.
   *
   * @return The depth search value of Minimax
   */
  public int getSearchDepth() {
    return this.searchDepth;
  }

  /**
   * Get the limit time reflexion of the MiniMax Player.
   *
   * @return The reflexion time value of Minimax
   */
  public int getLimitTime() {
    return this.reflexionTime;
  }

  /**
   * Get if the MiniMax Player use iterative deepening.
   *
   * @return The boolean representing if Minimax use Iterative Deepening
   */
  public boolean isUseIterativeDeepening() {
    return this.useIterativeD;
  }

  /**
   * Get the heuristicname of the MiniMax Player.
   *
   * @return The name of the heuristic selected
   */
  public String getHeuristicName() {
    return this.heuristicName;
  }

  @Override
  public String toString() {
    return "MiniMax AI " + super.toString();
  }

  @Override
  public Player copy() {
    return new MinimaxPlayer(
        this.getName(),
        this.getPosition(),
        this.getStartingPosition(),
        this.getRemainingWalls(),
        this.getScore(),
        this.searchDepth,
        this.reflexionTime,
        this.useIterativeD,
        this.heuristicName,
        this.getColor());
  }

  @Override
  public Map<String, Object> getSettings() {
    final Map<String, Object> settings = new ConcurrentHashMap<>();
    if (this.useIterativeD) {
      settings.put("mode", "minimax-iterative-deepening");
    } else {
      settings.put("mode", "minimax");
      settings.put("depth", this.searchDepth);
    }
    settings.put("reflexion", this.reflexionTime);
    settings.put("heuristic", this.heuristicName);
    return settings;
  }
}
