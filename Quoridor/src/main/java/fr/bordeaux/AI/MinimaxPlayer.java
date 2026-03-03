package fr.bordeaux.AI;

import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Move;
import fr.bordeaux.core.Move.Type;
import fr.bordeaux.core.Orientation;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/** Represents an Ai player with Minimax */
public class MinimaxPlayer
    extends Player { // MiniMax and IterativeDeepening are limited because it works with only 2
  // players. They will not work if the number of players is larger.

  private int depth;
  private int reflexionTime = 5; // In seconds
  private final ExecutorService aiExecutor = Executors.newSingleThreadExecutor();
  private volatile Move saveMove = null;
  private Function<GameState, Move> mode;
  private Function<GameState, Integer> heuristic; // h1 Default heuristic
  private final Deque<Move> IAStack = new ArrayDeque<>();
  private final boolean isIterativeDeepening;
  private final String heuristicName;

  
  public MinimaxPlayer(
       String name,
       Position positionStart,
       int depth,
       int reflexionTime,
       boolean isIterativeDeepening,
       String h) {
    super(name, positionStart);
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

  public MinimaxPlayer(String name, Position positionStart, int depth){
    this(name, positionStart, depth, 5, false, "h1");
  }

  public void setHeuristic(String nameH) {
    if(nameH == null){
      this.heuristic = (state) -> h1(state);
    }
    else{
      switch(nameH) {
        case "h2": this.heuristic = (state) -> h2(state); break;
        case "h3": this.heuristic = (state) -> h3(state); break;
        case "h1":   this.heuristic = (state) -> h1(state);
      }
    }
  }

  @Override
  public Move getNextMove(GameState state) {

    if (state.getPlayers().size() != 2) {
      throw new IllegalArgumentException(" IA can only be used in a 1v1");
    }

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

  private Move randomMove(
      GameState
          g) { // It is not planned to be able to launch it in the game, it will just be used for
    // testing
    List<Move> l = g.generateLegalMoves();
    Random random = new Random();
    return l.get(random.nextInt(l.size()));
  }

  // Simple heuristic that count the difference between the column current position and the column
  // start position.
  // If the player start at the first row, this heuristic compare row instead of columns
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
    }
    else if(startingPosition.getX() == g.getBoard().getSize()-1){
      return startingPosition.getX() - actualPosition.getX();
    }
    else if (startingPosition.getY() == 0) {
      return actualPosition.getY() - startingPosition.getY();
    }
    else {
      return startingPosition.getY() - actualPosition.getY();
    }
  }

  // Very similar than the previous one but now we also make importance at the enemie position
  private int h2(GameState g) {
    List<Player> lp = g.getPlayers();
    Player enemie = null;
    Player me = null;
    for (Player p : lp) {
      if (p.getName().equals(this.getName())) {
        me = p;
      }
      else{
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
          - (enemieStartingPosition.getX() - enemieActualPosition.getX() );
    }
    else if (playerStartingPosition.getX() == g.getBoard().getSize()-1) {
      return (playerStartingPosition.getX() - playerActualPosition.getX())
          - (enemieActualPosition.getX() - enemieStartingPosition.getX());
    }
    else if (playerStartingPosition.getY() == 0) {
      return (playerActualPosition.getY() - playerStartingPosition.getY())
          - (enemieStartingPosition.getY() - enemieActualPosition.getY());
    }
    else {
      return (playerStartingPosition.getY() - playerActualPosition.getY())
          - (enemieActualPosition.getY() - enemieStartingPosition.getY());
    }
  }

  private int h3(GameState g) {
    // K1 and K2 are the coefficients of wall remaining for the player and his enemie, at the end of
    // the project we will use a neuronal network to determine them.
    float k1 = 1.2f;
    float k2 = 0.8f;

    List<Player> lp = g.getPlayers();
    Player enemie = null;
    Player me = null;
    for (Player p : lp) {
      if (p.getName().equals(this.getName())) {
        me = p;
      }
      else{
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
    int scoreWall = (int) (nb_wall_player * k1*10 - nb_wall_enemie * k2*10);
    return scoreBFS + scoreWall;
  }

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
        this.heuristicName
      );
  }

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
  public Boolean isAI() {
    return true;
  }
}
