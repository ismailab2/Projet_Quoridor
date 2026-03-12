package fr.bordeaux.ai;

import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Move;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;
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
import java.util.function.Function;

/**
 * Represents an AI player using the Minimax algorithm for the Quorridor game. Features advanced
 * capabilities such as Alpha-Beta pruning, Iterative Deepening, and a configurable reflection time
 * limit. * Note: Minimax and Iterative Deepening are currently limited to 2-player scenarios. They
 * may not function correctly if the number of players is larger.
 */
public class MinimaxPlayer extends AiPlayer {

  private int depth;
  private int reflexionTime = 5; // In seconds
  private final ExecutorService aiExecutor = Executors.newSingleThreadExecutor();
  private volatile Move saveMove = null;
  private Function<GameState, Move> mode;
  private Function<GameState, Integer> heuristic; // h1 Default heuristic
  private final Deque<Move> IAStack = new ArrayDeque<>();
  private final boolean isIterativeDeepening;
  private final String heuristicName;

  /**
   * Private constructor used by the Builder to instantiate the AI.
   *
   * @param builder The builder containing configuration settings.
   */
  private MinimaxPlayer(Builder builder) {
    super(builder.name, builder.positionStart);
    this.depth = builder.depth;
    this.reflexionTime = builder.reflexionTime;
    this.isIterativeDeepening = builder.isIterativeDeepening;
    this.heuristicName = builder.heuristicName;
    if (isIterativeDeepening) {
      mode = (state) -> IterativeDeepening(state);
    } else {
      mode = (state) -> MiniMax(state, this.depth);
    }
    setHeuristic(heuristicName);
  }

  /**
   * Utility class implementing the Builder design pattern to configure and instantiate a
   * MinimaxPlayer.
   */
  public static class Builder {
    private final String name;
    private final Position positionStart;
    private int depth = 2;
    private int reflexionTime = 5;
    private boolean isIterativeDeepening = false;
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

    public Builder withDepth(int d) {
      this.depth = d;
      return this;
    }

    public Builder withReflexion(int t) {
      this.reflexionTime = t;
      return this;
    }

    public Builder withHeuristic(String h) {
      this.heuristicName = h;
      return this;
    }

    public Builder isIterativeDeepening(boolean val) {
      this.isIterativeDeepening = val;
      return this;
    }

    private boolean validHeuristic(String h) {
      List<String> valideName = new ArrayList<>(List.of("h1", "h2", "h3"));
      for (String elt : valideName) {
        if (h.equals(elt)) {
          return true;
        }
      }
      return false;
    }

    /**
     * Builds the final MinimaxPlayer instance.
     *
     * @return A new MinimaxPlayer instance.
     * @throws IllegalArgumentException if the heuristic name, depth, or reflection time is invalid.
     */
    public MinimaxPlayer build() {
      if (!validHeuristic(heuristicName)) {
        throw new IllegalArgumentException(
            "Incorrect name for the heuristic, the "
                + heuristicName
                + " heuristic does not exist.");
      }
      if (depth < 0) {
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

  public static Builder builder(String name, Position pos) {
    return new Builder(name, pos);
  }

  /**
   * Sets the heuristic function used by the AI to evaluate game states.
   *
   * @param nameH The name of the heuristic ("h1", "h2", or "h3").
   */
  public void setHeuristic(String nameH) {
    switch (nameH) {
      case "h2":
        this.heuristic = (state) -> h2(state);
        break;
      case "h3":
        this.heuristic = (state) -> h3(state);
        break;
      case "h1":
        this.heuristic = (state) -> h1(state);
    }
  }

  /**
   * Computes and returns the best possible move within the allocated time. Uses an ExecutorService
   * to interrupt the search if the time limit is reached.
   *
   * @param state The current game state.
   * @return The best move found, or a random(alpha-beta pruning)/saved(Iterative Deepening) move if
   *     interrupted.
   */
  @Override
  public Move getNextMove(GameState state) {

    saveMove = null;

    Future<Move> futureMove = aiExecutor.submit(() -> mode.apply(state));

    try {
      return futureMove.get(reflexionTime, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
      futureMove.cancel(true);
      System.out.println("Fin du temps de réflexion");
      if (saveMove != null) {
        return saveMove;
      } else {
        return randomMove(state);
      }
    } catch (InterruptedException | ExecutionException e) {
      futureMove.cancel(true);
      System.out.println("Anomalie détecter !");
      e.printStackTrace();
      if (saveMove != null) {
        return saveMove;
      } else {
        return randomMove(state);
      }
    }
  }

  /**
   * Implements Iterative Deepening search. Repeatedly calls Minimax with increasing depth (starting
   * from 1) until the thread is interrupted by a timeout.
   *
   * @param state The current game state.
   * @return The best move found at the maximum depth reached.
   */
  private Move IterativeDeepening(GameState state) {
    int currentdepth = 1;
    while (!Thread.currentThread().isInterrupted()) {

      Move temp = MiniMax(state, currentdepth);
      if (Thread.currentThread().isInterrupted()) {
        break;
      }

      if (temp != null) {
        saveMove = temp;
      }
      currentdepth++;
    }

    return saveMove;
  }

  /**
   * Entry point for the Minimax algorithm. Explores the first level of legal moves.
   *
   * @param g The current game state.
   * @param d The search depth.
   * @return The best evaluated move.
   */
  private Move MiniMax(GameState g, int d) {

    Move best_move = null;
    int best_score = Integer.MIN_VALUE;
    GameState state = g.copy();
    for (Move s : state.generateLegalMoves()) {

      if (Thread.currentThread().isInterrupted()) return null;

      push(state, s);
      int score = MinMax(state, d - 1, Integer.MIN_VALUE, Integer.MAX_VALUE);
      if (score > best_score || best_move == null) {
        best_score = score;
        best_move = s;
      }
      pop(state);
    }

    return best_move;
  }

  /**
   * MAX node of the Minimax algorithm with Alpha-Beta pruning. Maximize the score for the AI
   * player.
   *
   * @param g The game state.
   * @param d The remaining depth.
   * @param alpha The best value guaranteed for MAX.
   * @param beta The best value guaranteed for MIN.
   * @return The maximized score.
   */
  private int MaxMin(GameState g, int d, int alpha, int beta) {

    if (g.isGameOver() || d == 0 || Thread.currentThread().isInterrupted()) {
      return heuristic.apply(g);
    }

    for (Move s : g.generateLegalMoves()) {
      push(g, s);
      alpha = Math.max(alpha, MinMax(g, d - 1, alpha, beta));
      pop(g);
      if (alpha >= beta) {
        return beta;
      }
    }
    return alpha;
  }

  /**
   * MIN node of the Minimax algorithm with Alpha-Beta pruning. Minimize the AI's score
   * (representing the opponent's best response).
   *
   * @param g The game state.
   * @param d The remaining depth.
   * @param alpha The best value guaranteed for MAX.
   * @param beta The best value guaranteed for MIN.
   * @return The minimized score.
   */
  private int MinMax(GameState g, int d, int alpha, int beta) {

    if (g.isGameOver() || d == 0 || Thread.currentThread().isInterrupted()) {
      return heuristic.apply(g);
    }

    for (Move s : g.generateLegalMoves()) {
      push(g, s);
      beta = Math.min(beta, MaxMin(g, d - 1, alpha, beta));
      pop(g);
      if (alpha >= beta) {
        return alpha;
      }
    }
    return beta;
  }

  /**
   * Plays a random move. Used by the Minimax algorithm with Alpha-Beta pruning when reflexion time
   * is over
   *
   * @param g The game state.
   * @return A random legal move.
   */
  private Move randomMove(GameState g) {
    List<Move> l = g.generateLegalMoves();
    Random random = new Random();
    return l.get(random.nextInt(l.size()));
  }

  /**
   * Heuristic 1 (Simple): Calculates the direct distance covered by the AI. Compares rows or
   * columns based on the player's starting side.
   *
   * @param g The game state to evaluate.
   * @return An integer score based on spatial progression.
   */
  private int h1(GameState g) {

    List<Player> lp = g.getPlayers();
    Player me = null;
    for (Player p : lp) {
      if (p.getName().equals(this.getName())) {
        me = p;
        break;
      }
    }

    Position startingPosition = me.getStartingPosition();
    Position actualPosition = me.getPosition();

    if (startingPosition.getX() == 0) {
      return actualPosition.getX() - startingPosition.getX();
    } else if (startingPosition.getX() == g.getBoard().getSize() - 1) {
      return startingPosition.getX() - actualPosition.getX();
    } else if (startingPosition.getY() == 0) {
      return actualPosition.getY() - startingPosition.getY();
    } else {
      return startingPosition.getY() - actualPosition.getY();
    }
  }

  /**
   * Heuristic 2 (Intermediate): Considers both the AI's and the opponent's positions. Calculates
   * the difference between the AI's progress and the opponent's progress.
   *
   * @param g The game state to evaluate.
   * @return A relative score favoring the AI's lead over the opponent.
   */
  private int h2(GameState g) {
    List<Player> lp = g.getPlayers();
    Player enemie = null;
    Player me = null;
    for (Player p : lp) {
      if (p.getName().equals(this.getName())) {
        me = p;
      } else {
        enemie = p;
      }
    }

    Position playerStartingPosition = me.getStartingPosition();
    Position playerActualPosition = me.getPosition();
    if (enemie == null) {
      throw new IllegalStateException("No enemie found !");
    }
    Position enemieStartingPosition = enemie.getStartingPosition();
    Position enemieActualPosition = enemie.getPosition();

    if (playerStartingPosition.getX() == 0) {
      return (playerActualPosition.getX() - playerStartingPosition.getX())
          - (enemieStartingPosition.getX() - enemieActualPosition.getX());
    } else if (playerStartingPosition.getX() == g.getBoard().getSize() - 1) {
      return (playerStartingPosition.getX() - playerActualPosition.getX())
          - (enemieActualPosition.getX() - enemieStartingPosition.getX());
    } else if (playerStartingPosition.getY() == 0) {
      return (playerActualPosition.getY() - playerStartingPosition.getY())
          - (enemieStartingPosition.getY() - enemieActualPosition.getY());
    } else {
      return (playerStartingPosition.getY() - playerActualPosition.getY())
          - (enemieActualPosition.getY() - enemieStartingPosition.getY());
    }
  }

  /**
   * Heuristic 3 (Advanced): Uses Breadth-First Search (BFS) to find the actual shortest path to the
   * goal and incorporates remaining wall counts weighted by coefficients K1 and K2. We will use a
   * neuronal network to determine them.
   *
   * @param g The game state to evaluate.
   * @return A comprehensive score combining pathfinding (BFS) and material advantage (walls).
   */
  private int h3(GameState g) {

    float k1 = 1.2f;
    float k2 = 0.8f;

    List<Player> lp = g.getPlayers();
    Player enemie = null;
    Player me = null;
    for (Player p : lp) {
      if (p.getName().equals(this.getName())) {
        me = p;
      } else {
        enemie = p;
      }
    }

    List<Integer> l_max =
        g.getBoard()
            .getGraph()
            .findPathBFS(
                g.getBoard().id(me.getPosition().getX(), me.getPosition().getY()),
                g.getWinCondition(me));

    List<Integer> l_min =
        g.getBoard()
            .getGraph()
            .findPathBFS(
                g.getBoard().id(enemie.getPosition().getX(), enemie.getPosition().getY()),
                g.getWinCondition(enemie));

    int nb_wall_player = me.getRemainingWalls();
    int nb_wall_enemie = enemie.getRemainingWalls();

    if (l_max.size() <= 1) {
      return 10000;
    }
    if (l_min.size() <= 1) {
      return -10000;
    }

    int scoreBFS = (l_min.size() - l_max.size()) * 10;
    int scoreWall = (int) (nb_wall_player * k1 * 10 - nb_wall_enemie * k2 * 10);
    return scoreBFS + scoreWall;
  }

  /**
   * Simulates a move (pawn movement or wall placement) and updates the game state. The move is
   * pushed onto IAStack to allow for backtracking.
   *
   * @param g The current game state.
   * @param move The move to simulate.
   */
  private void push(GameState g, Move move) {
    Player player = g.getCurrentPlayer();
    if (move.isPawn()) {
      player.setPosition(move.getTo());
    } else {
      g.getBoard().addWall(move.getTo(), move.getOrientation());
      player.useWall();
    }

    IAStack.push(move);
    g.getNextPlayer();
  }

  /**
   * Backtracks the last simulated move by popping from IAStack, restoring the previous game state.
   *
   * @param g The game state to restore.
   */
  private void pop(GameState g) {
    if (IAStack.isEmpty()) return;

    Move last = IAStack.pop();
    g.previousPlayer();
    Player player = g.getCurrentPlayer();
    if (last.isPawn()) {
      player.setPosition(last.getFrom());
    } else {
      g.getBoard().removeWall(last.getTo(), last.getOrientation());
      player.giveBackWall();
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
        this.depth,
        this.reflexionTime,
        this.isIterativeDeepening,
        this.heuristicName);
  }

  /** Private constructor used by the copy() method for cloning the AI player. */
  private MinimaxPlayer(
      String name,
      Position current,
      Position start,
      int walls,
      int score,
      int depth,
      int reflexionTime,
      boolean isIterativeDeepening,
      String h) {
    super(name, current, start, walls, score);
    this.depth = depth;
    this.reflexionTime = reflexionTime;
    this.isIterativeDeepening = isIterativeDeepening;
    this.heuristicName = h;

    if (isIterativeDeepening) {
      mode = (state) -> IterativeDeepening(state);
    } else {
      mode = (state) -> MiniMax(state, this.depth);
    }
    setHeuristic(h);
  }

  @Override
  public Map<String, Object> getSettings() {
    Map<String, Object> settings = new HashMap<>();
    if (isIterativeDeepening) {
      settings.put("mode", "minimax-iterative-deepening");
    } else {
      settings.put("mode", "minimax");
      settings.put("depth", depth);
    }
    settings.put("reflexion", reflexionTime);
    settings.put("heuristic", heuristicName);
    return settings;
  }
}
