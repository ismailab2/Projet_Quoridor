package fr.bordeaux.core;

import fr.bordeaux.persistence.GameSerializer;
import fr.bordeaux.persistence.GameLoadException;
import java.io.IOException;
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

  public GameState(Board board, List<Player> players, int currentIndexPlayer) {
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

  // Apply a move, needs to be a valid move because it won't be checked in this function.
  // Check victory is not done here as well
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

  public Move[] getMovesPlayed() {
    Object[] obj = undoStack.toArray();
    Move[] moves = new Move[obj.length];
    for (int i = 0; i < obj.length; i++) {
      moves[i] = (Move)obj[i];
    }
    return moves;
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

  public void previousPlayer() {
    currentIndexPlayer = (currentIndexPlayer - 1 + players.size()) % players.size();
  }

  public Player getCurrentPlayer() {
    return players.get(currentIndexPlayer);
  }

  public int getCurrentIndexPlayer() {
    return currentIndexPlayer;
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

  public void saveGame(String filename) throws IOException {
    GameSerializer.saveGame(this, filename);
  }

  public static GameState loadGame(String filename) throws IOException, GameLoadException {
    return GameSerializer.loadGame(filename);
  }

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

  public List<Move> generateLegalMoves() {
    List<Move> res = new ArrayList<>();
    Position player_pos = getCurrentPlayer().getPosition();
    int x = player_pos.getX();
    int y = player_pos.getY();
    List<Move> all_possible_moves = new ArrayList<>();
    for (int i = -1; i < 2; i++) {
      for (int j = -1; j < 2; j++) {
        if (i != 0 || j != 0) {
          all_possible_moves.add(Move.pawn(player_pos, new Position(x + i, y + j)));
        }
      }
    }

    all_possible_moves.add(Move.pawn(player_pos, new Position(x + 2, y)));
    all_possible_moves.add(Move.pawn(player_pos, new Position(x - 2, y)));
    all_possible_moves.add(Move.pawn(player_pos, new Position(x, y + 2)));
    all_possible_moves.add(Move.pawn(player_pos, new Position(x, y - 2)));

    if (getCurrentPlayer().getRemainingWalls() > 0) {
      for (x = 0; x < board.getSize() - 1; x++) {
        for (y = 1; y < board.getSize(); y++) {
          Position pos_wall = new Position(x, y);

          all_possible_moves.add(Move.wall(pos_wall, Orientation.HORIZONTAL));

          all_possible_moves.add(Move.wall(pos_wall, Orientation.VERTICAL));
        }
      }
    }

    for (Move m : all_possible_moves) {
      if (RuleChecker.isMoveLegal(this, m)) {
        res.add(m);
      }
    }

    return res;
  }
}
