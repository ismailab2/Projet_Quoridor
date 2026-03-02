package fr.bordeaux.core;

import java.util.*;
import java.util.function.Predicate;

public class GameState {

  private final Board board;
  private final List<Player> players;
  private int currentIndexPlayer;
  private boolean paused;
  private boolean gameOver;
  private final Deque<Move> undoStack;
  private final Deque<Move> redoStack;

   private transient List<Runnable> listeners = new ArrayList<>();

  public GameState(
      Board board,
      List<Player> players,
      int currentIndexPlayer) {
    if (players.size() != 2 && players.size() != 4)
      throw new IllegalArgumentException(
          "Number of players must be 2 or 4, but was: " + players.size());

    this.board = board;
    this.players = players;
    this.currentIndexPlayer = currentIndexPlayer;
    this.paused = false;
    this.gameOver = false;
    this.undoStack = new ArrayDeque<>();
    this.redoStack = new ArrayDeque<>();
  }

  public GameState(Board board, List<Player> players) {
    this(board, players, 0);
  }

  public void addListener(Runnable listener) {
    listeners.add(listener);
  }

  private void notifyListeners() {
    for (Runnable l : listeners) l.run();
  }

  //Apply a move, needs to be a valid move because it won't be checked in this function.
  //Check victory is not done here as well
  public void applyMove(Move move) {
    Player player = getCurrentPlayer();
    if (move.isPawn()) {
      player.setPosition(move.getTo());
    } else {
      board.addWall(move.getTo(), move.getOrientation());
      player.useWall();
    }

    undoStack.push(move);
    redoStack.clear();
    getNextPlayer();
    notifyListeners();
  }

  private void applyMoveWithoutClearingRedo(Move move) {
    Player player = getCurrentPlayer();
    if (move.isPawn()) {
      player.setPosition(move.getTo());
    } else {
      board.addWall(move.getTo(), move.getOrientation());
      player.useWall();
    }
    undoStack.push(move);
    getNextPlayer();
  }

  public void undo() {
    if (paused || gameOver) return;
    if (undoStack.isEmpty()) return;

    do {
      Move last = undoStack.pop();
      redoStack.push(last);
      previousPlayer();
      Player player = getCurrentPlayer();
      if (last.isPawn()) {
        player.setPosition(last.getFrom());
      } else {
        board.removeWall(last.getTo(), last.getOrientation());
        player.giveBackWall();
      }
      gameOver = false;
    } while (!undoStack.isEmpty() && getCurrentPlayer().isAI());
    notifyListeners();
  }

  public void redo() {
    if (paused || gameOver) return;
    if (redoStack.isEmpty()) return;

    do {
      Move move = redoStack.pop();
      applyMoveWithoutClearingRedo(move);
    } while (!redoStack.isEmpty() && getCurrentPlayer().isAI());
    notifyListeners();
  }


  public boolean isGameOver() {
    return gameOver;
  }

  public boolean hasReachedGoal(Player player) {
    int id = board.id(player.getPosition().getX(), player.getPosition().getY());
    // System.out.println(getWinCondition(player).test(id));
    return getWinCondition(player).test(id);
  }

  public Predicate<Integer> getWinCondition(Player player) {
    Position start = player.getStartingPosition();
    int size = board.getSize();

    if (start.getX() == 0) return id -> (id / size) == size - 1; // last row
    if (start.getX() == size - 1) return id -> (id / size) == 0; // first row
    if (start.getY() == 0) return id -> (id % size) == size - 1; // last col
    return id -> (id % size) == 0; // first col
  }

  public void getNextPlayer() {
    currentIndexPlayer = (currentIndexPlayer + 1) % players.size();
  }

  private void previousPlayer() {
    currentIndexPlayer = (currentIndexPlayer - 1 + players.size()) % players.size();
  }

  public Player getCurrentPlayer() {
    return players.get(currentIndexPlayer);
  }

  public Board getBoard() {
    return board;
  }

  public List<Player> getPlayers() {
    return players;
  }

  public void setPaused(boolean paused) {
    this.paused = paused;
  }

  public boolean isPaused() {
    return paused;
  }

  public void saveGame(String filename) {
      System.out.println("save a implementer");
  }

  /*
  public GameState loadGame(String filename) {
      return ;
  }

   */

  public GameState reset() {
    int size = 9;
    Board newBoard = new Board(size);
    List<Player> newPlayers = new ArrayList<>();
    newPlayers.add(new HumanPlayer("Human", new Position(0, size / 2)));
    newPlayers.add(new HumanPlayer("Bob", new Position(size - 1, size / 2)));
    return new GameState(newBoard, newPlayers);
  }

  public GameState copy() {
    Board boardCopy = this.board.copy();

    List<Player> playersCopy = new ArrayList<>();
    for (Player p : this.players) {
      playersCopy.add(p.copy());
    }

    GameState copy = new GameState(boardCopy, playersCopy, this.currentIndexPlayer);

    copy.gameOver = this.gameOver;
    copy.paused = this.paused;
    return copy;
  }
}
