package fr.bordeaux.AI;

import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Move;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;
import main.java.fr.bordeaux.AI.AdvancedHeuristic;
import main.java.fr.bordeaux.AI.HeuristicEvaluator;
import main.java.fr.bordeaux.AI.MediumHeuristic;
import main.java.fr.bordeaux.AI.SimpleHeuristic;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToIntFunction;

/**
 * Represents an AI player using the Minimax algorithm for the Quorridor game.
 * Features advanced capabilities such as Alpha-Beta pruning, 
 * Iterative Deepening, and a configurable reflection time limit. 
 * Note: AI are currently limited to 2-player scenarios.
 * They may not function correctly if the number of players is larger.
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
  private final boolean useIterativeDeepening;

  /** The name of the heuristic algorithm to use. */
  private final String heuristicName;

  /**
   * Private constructor used by the Builder to instantiate the AI.
   *
   * @param builder The builder containing configuration settings.
   */
  private MinimaxPlayer(Builder builder) {
    super(builder.name, builder.positionStart);
    this.searchDepth = builder.searchDepth;
    this.reflexionTime = builder.reflexionTime;
    this.useIterativeDeepening = builder.useIterativeDeepening;
    this.heuristicName = builder.heuristicName;

    if (this.useIterativeDeepening) {
      this.mode = this::iterativeDeepening;
    } else {
      this.mode = (final GameState state, final Long endTime) -> miniMax(state, this.searchDepth, endTime);
    }
    
    this.heuristicFunction = determineHeuristic(this.heuristicName, this.getName());
  }

    /** Private constructor used by the copy() method for cloning the AI player. */
  private MinimaxPlayer(
      final String name,
      final Position current,
      final Position start,
      final int walls,
      final int score,
      final int depth,
      final int timeLimit,
      final boolean iterativeDeepeningVal,
      final String heuristicVal) {
    super(name, current, start, walls, score);
    this.searchDepth = depth;
    this.reflexionTime = timeLimit;
    this.useIterativeDeepening = iterativeDeepeningVal;
    this.heuristicName = heuristicVal;

    if (this.useIterativeDeepening) {
      this.mode = this::iterativeDeepening;
    } else {
      this.mode = (final GameState state, final Long endTime) -> miniMax(state, this.searchDepth, endTime);
    }
    this.heuristicFunction = determineHeuristic(this.heuristicName, this.getName());
  }

  /**
   * Utility class implementing the Builder design pattern to configure and instantiate a
   * MinimaxPlayer.
   */
  public static class Builder {
    private final String name;
    private final Position positionStart;
    private int searchDepth = 2;
    private int reflexionTime = 5;
    private boolean useIterativeDeepening = false;
    private String heuristicName = "h1";

    /**
     * Initializes the Builder with mandatory parameters.
     *
     * @param name The AI's name.
     * @param pos The starting position on the board.
     */
    public Builder(String name, Position pos) {
      this.name = name;
      this.positionStart = pos;
    }
    /**
     * Sets the depth of the Minimax search.
     * * @param depth The chosen depth.
     * @return The builder instance.
     */
    public Builder withDepth(int depth) {
      this.searchDepth = depth;
      return this;
    }

    /**
     * Sets the reflection time limit in seconds.
     * * @param t The chosen time limit.
     * @return The builder instance.
     */
    public Builder withReflexion(int timeLimit) {
      this.reflexionTime = timeLimit;
      return this;
    }

    /**
     * Sets the heuristic name to be used.
     * * @param h The name of the heuristic.
     * @return The builder instance.
     */
    public Builder withHeuristic(String heuristic) {
      this.heuristicName = heuristic;
      return this;
    }

    /**
     * Enables or disables iterative deepening.
     * * @param useDeepening The boolean flag.
     * @return The builder instance.
     */
    public Builder isIterativeDeepening(boolean useDeepening) {
      this.useIterativeDeepening = useDeepening;
      return this;
    }

    /**
     * Validates if the given heuristic name is known to the system.
     * * @param heuristic The heuristic name to check.
     * @return True if valid, false otherwise.
     */
    private boolean validHeuristic(String heuristic) {
      boolean isValid = false;
      final List<String> validNames = new ArrayList<>(List.of("h1", "h2", "h3"));
      for (final String element : validNames) {
        if (heuristic.equals(element)) {
          isValid = true;
          break;
        }
      }
      return isValid;
    }

    /**
     * Builds the final MinimaxPlayer instance.
     *
     * @return A new MinimaxPlayer instance.
     * @throws IllegalArgumentException if the configuration is invalid.
     */
    public MinimaxPlayer build() {
      if (!validHeuristic(heuristicName)) {
        throw new IllegalArgumentException(
            "Incorrect name for the heuristic, the "
                + heuristicName
                + " heuristic does not exist.");
      }
      if (searchDepth < 0) {
        throw new IllegalArgumentException(
            "Illegal depth value ! Depth is greater than or equal to 0");
      }

      if (reflexionTime <= 0) {
        throw new IllegalArgumentException(
            "Illegal reflexionTime value ! reflexionTime is grater than 0");
      }
      return new MinimaxPlayer(this);
    }
  }

  /**
   * Static factory method for the builder.
   * * @param name The player name.
   * @param pos  The starting position.
   * @return A new Builder instance.
   */
  public static Builder builder(String name, Position pos) {
    return new Builder(name, pos);
  }

  /**
   * Sets the heuristic function based on the provided name.
   *
   * @param heuristicNameP The name of the heuristic ("h1", "h2", or "h3").
   * @return The corresponding heuristic class.
   */
  private HeuristicEvaluator determineHeuristic(final String heuristicNameParam, final String aiName) {
    HeuristicEvaluator chosenHeuristic;
    switch (heuristicNameParam) {
      case "h2":
        chosenHeuristic = new MediumHeuristic(aiName);
        break;
      case "h3":
        chosenHeuristic = new AdvancedHeuristic(aiName);
        break;
      case "h1":
      default:
        chosenHeuristic = new SimpleHeuristic(aiName);
        break;
    }
    return chosenHeuristic;
  }

  /**
   * Computes and returns the best possible move within the allocated time limit.
   * @param state The current game state.
   * @return The best move found.
   */
  @Override
  public Move getNextMove(GameState state) {

    final long deadlineTime = System.currentTimeMillis() + (this.reflexionTime * 1000L);
    Move bestMove = this.mode.apply(state, deadlineTime);
    
    if (bestMove == null) {
      bestMove = randomMove(state);
    }
    return bestMove;
  }

  /**
   * Implements Iterative Deepening search safely within the main thread.
   * @param state    The current game state.
   * @param endTime  The timestamp when the search must abort.
   * @return The best move found at the maximum depth reached before timeout.
   */
  private Move iterativeDeepening(GameState state, long endTime) {
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
   * @param gameState The current game state.
   * @param maxDepth  The search depth.
   * @param endTime   The deadline for the search.
   * @return The best evaluated move.
   */
  private Move miniMax(GameState gameState, int maxDepth, long endTime) {

    Move bestMove = null;
    int bestScore = Integer.MIN_VALUE;
    GameState searchState = gameState.copy();
    for (Move currentMove : searchState.generateLegalMoves()) {
      if (System.currentTimeMillis() >= endTime) {
        break;
      }
      push(searchState, currentMove);
      int score = minMax(searchState, maxDepth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, endTime);
      if (score > bestScore || bestMove == null) {
        bestScore = score;
        bestMove = currentMove;
      }
      pop(searchState);
    }
    return bestMove;
  }

  /**
   * MAX node of the Minimax algorithm with Alpha-Beta pruning.
   * @param gameState The game state.
   * @param currentDepth     The remaining depth.
   * @param alpha     The best value guaranteed for MAX.
   * @param beta      The best value guaranteed for MIN.
   * @param endTime   The search deadline.
   * @return The maximized score.
   */
  private int maxMin(final GameState gameState, final int currentDepth, final int alpha, final int beta, final long endTime) {
    int resultScore;
    if (gameState.isGameOver() || currentDepth == 0 || System.currentTimeMillis() >= endTime) {
      resultScore = this.heuristicFunction.evaluate(gameState);
    } else {
      int currentAlpha = alpha;
      for (final Move move : gameState.generateLegalMoves()) {
        push(gameState, move);
        currentAlpha = Math.max(currentAlpha, minMax(gameState, currentDepth - 1, currentAlpha, beta, endTime));
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
   * MIN node of the Minimax algorithm with Alpha-Beta pruning.
   * @param gameState The game state.
   * @param currentDepth     The remaining depth.
   * @param alpha     The best value guaranteed for MAX.
   * @param beta      The best value guaranteed for MIN.
   * @param endTime   The search deadline.
   * @return The minimized score.
   */
  private int minMax(final GameState gameState, final int currentDepth, final int alpha, final int beta, final long endTime) {
    int resultScore;

    if (gameState.isGameOver() || currentDepth == 0 || System.currentTimeMillis() >= endTime) {
      resultScore = this.heuristicFunction.evaluate(gameState);
    } else {
      int currentBeta = beta;
      for (final Move move : gameState.generateLegalMoves()) {
        push(gameState, move);
        currentBeta = Math.min(currentBeta, maxMin(gameState, currentDepth - 1, alpha, currentBeta, endTime));
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
   * @param gameState The current game state.
   * @param move      The move to simulate.
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
        this.useIterativeDeepening,
        this.heuristicName);
  }


  @Override
  public Map<String, Object> getSettings() {
    Map<String, Object> settings = new HashMap<>();
    if (this.useIterativeDeepening) {
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
