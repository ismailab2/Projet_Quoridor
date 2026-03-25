package fr.bordeaux.core;

import fr.bordeaux.persistence.GameLoadException;
import fr.bordeaux.persistence.GameLoader;
import fr.bordeaux.persistence.GameSaver;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

/**
 * Manages the current state of a Quoridor game. It tracks the board, players, current turn, and
 * maintains the history for undo/redo functionality.
 */
public class GameState {

  private final Board board;
  private final List<Player> players;
  private int currentIndexPlayer;
  private boolean paused;
  private boolean gameOver;
  private final Deque<Move> undoStack;
  private final Deque<Move> redoStack;
  private boolean debug = false;
  private boolean verbose = false;
  private boolean blitz = false;
  private boolean contest = false;
  private boolean ai = false;
  private int turnLimit = 100;

  private final transient List<Runnable> listeners = new ArrayList<>();

  /**
   * Constructs a GameState with a board, a list of players, and an initial player index.
   *
   * @param board The board used for the game.
   * @param players The list of participants (must be 2 or 4).
   * @param currentIndexPlayer The index of the player who starts.
   * @throws IllegalArgumentException If the number of players is not 2 or 4.
   */
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

  /**
   * Constructs a GameState with a board and a list of players, starting with the first player.
   *
   * @param board The board used for the game.
   * @param players The list of participants.
   */
  public GameState(Board board, List<Player> players) {
    this(board, players, 0);
  }

  /**
   * Adds a listener to be notified of state changes.
   *
   * @param listener The {@link Runnable} to execute on change.
   */
  public void addListener(Runnable listener) {
    listeners.add(listener);
  }

  /** Notifies all registered listeners of a state change. */
  private void notifyListeners() {
    for (Runnable l : listeners) l.run();
  }

  /**
   * Applies a move to the current state. Updates player position or board walls, manages history,
   * and moves to the next turn.
   *
   * @param move The {@link Move} to apply.
   */
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

  /**
   * Applies a move without clearing the redo stack. Useful for internal state manipulations or
   * redoing moves.
   *
   * @param move The {@link Move} to apply.
   */
  public void applyMoveWithoutClearingRedo(Move move) {
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

  /**
   * Retrieves all moves played so far.
   *
   * @return An array of {@link Move} objects representing the history.
   */
  public Move[] getMovesPlayed() {
    Object[] obj = undoStack.toArray();
    Move[] moves = new Move[obj.length];
    for (int i = 0; i < obj.length; i++) {
      moves[i] = (Move) obj[i];
    }
    return moves;
  }

  /**
   * Reverts the last move. If the previous player was an AI, it continues reverting until a human
   * player is reached.
   */
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

  /** Re-applies the last undone move. If the next player is an AI, it continues redoing. */
  public void redo() {
    if (paused || gameOver) return;
    if (redoStack.isEmpty()) return;

    do {
      Move move = redoStack.pop();
      applyMoveWithoutClearingRedo(move);
    } while (!redoStack.isEmpty() && getCurrentPlayer().isAI());
    notifyListeners();
  }

  /**
   * Checks if the game is over.
   *
   * @return {@code true} if a player has won or game is terminated.
   */
  public boolean isGameOver() {
    return gameOver;
  }

  /**
   * Checks if the specified player has reached their goal.
   *
   * @param player The player to check.
   * @return {@code true} if the player is on a winning cell.
   */
  public boolean hasReachedGoal(Player player) {
    int id = board.id(player.getPosition().getX(), player.getPosition().getY());
    return getWinCondition(player).test(id);
  }

  /**
   * Returns a predicate representing the win condition for a specific player.
   *
   * @param player The player whose win condition is needed.
   * @return A {@link Predicate} that returns true if a cell ID is a winning cell.
   */
  public Predicate<Integer> getWinCondition(Player player) {
    Position start = player.getStartingPosition();
    int size = board.getSize();

    if (start.getX() == 0) return id -> (id / size) == size - 1; // last row
    if (start.getX() == size - 1) return id -> (id / size) == 0; // first row
    if (start.getY() == 0) return id -> (id % size) == size - 1; // last col
    return id -> (id % size) == 0; // first col
  }

  /** Moves the turn to the next player. */
  public void getNextPlayer() {
    currentIndexPlayer = (currentIndexPlayer + 1) % players.size();
  }

  /** Moves the turn to the previous player. */
  public void previousPlayer() {
    currentIndexPlayer = (currentIndexPlayer - 1 + players.size()) % players.size();
  }

  /**
   * Gets the index of the player who played the last move.
   *
   * @return The previous player's index.
   */
  public int getPreviousIndexPlayer() {
    return (currentIndexPlayer - 1 + players.size()) % players.size();
  }

  /**
   * Gets the player whose turn it is.
   *
   * @return The current {@link Player}.
   */
  public Player getCurrentPlayer() {
    return players.get(currentIndexPlayer);
  }

  /**
   * Gets the index of the current player.
   *
   * @return The current index.
   */
  public int getCurrentIndexPlayer() {
    return currentIndexPlayer;
  }

  /**
   * Gets the game board.
   *
   * @return The {@link Board} instance.
   */
  public Board getBoard() {
    return board;
  }

  /**
   * Gets the list of players.
   *
   * @return The list of {@link Player} instances.
   */
  public List<Player> getPlayers() {
    return players;
  }

  /**
   * Sets the pause state of the game.
   *
   * @param paused {@code true} to pause, {@code false} to resume.
   */
  public void setPaused(boolean paused) {
    this.paused = paused;
  }

  /**
   * Checks if the game is currently paused.
   *
   * @return {@code true} if paused.
   */
  public boolean isPaused() {
    return paused;
  }

  /**
   * Saves the state to a file.
   *
   * @param filename Path to the file.
   * @throws IOException If saving fails.
   */
  public void saveGame(String filename) throws IOException {
    GameSaver.saveGame(this, filename);
  }

  /**
   * Loads a game state from a file.
   *
   * @param filename Path to the file.
   * @return The loaded {@link GameState}.
   * @throws IOException If reading fails.
   * @throws GameLoadException If the file content is invalid.
   */
  public GameState loadGame(String filename) throws IOException, GameLoadException {
    return GameLoader.loadGame(filename);
  }

  /**
   * Resets the game to a default state (9x9 board with two human players).
   *
   * @return A new {@link GameState} instance.
   */
  public GameState reset() {
    int size = 9;
    Board newBoard = new Board(size);
    List<Player> newPlayers = new ArrayList<>();
    newPlayers.add(new HumanPlayer("Human", new Position(0, size / 2)));
    newPlayers.add(new HumanPlayer("Bob", new Position(size - 1, size / 2)));
    notifyListeners();
    return new GameState(newBoard, newPlayers);
  }

  /**
   * Creates a deep copy of the current state (Board and Players).
   *
   * @return A cloned {@link GameState}.
   */
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

  /**
   * Generates all possible legal moves (pawn movements and wall placements) for the current player.
   *
   * @return A list of legal {@link Move} objects.
   */
  public List<Move> generateLegalMoves() {
    List<Move> res = new ArrayList<>();
    for (Move m : generatePseudoLegalMoves()) {
      if (RuleChecker.isMoveLegal(this, m)) {
        res.add(m);
      }
    }
    return res;
  }

  /**
   * Generates all potential move candidates without rule validation.
   *
   * @return A list of pseudo-legal {@link Move} objects.
   */
  public List<Move> generatePseudoLegalMoves() {
    List<Move> all_possible_moves = new ArrayList<>();
    Position player_pos = getCurrentPlayer().getPosition();
    int x = player_pos.getX();
    int y = player_pos.getY();
    for (int i = -1; i < 2; i++) {
      for (int j = -1; j < 2; j++) {
        if (i != 0 || j != 0) {
          all_possible_moves.add(Move.pawn(new Position(x, y), new Position(x + i, y + j)));
        }
      }
    }

    all_possible_moves.add(Move.pawn(player_pos, new Position(x + 2, y)));
    all_possible_moves.add(Move.pawn(player_pos, new Position(x - 2, y)));
    all_possible_moves.add(Move.pawn(player_pos, new Position(x, y + 2)));
    all_possible_moves.add(Move.pawn(player_pos, new Position(x, y - 2)));

    if (getCurrentPlayer().getRemainingWalls() > 0) {
      for (x = 0; x < board.getSize() - 1; x++) {
        for (y = 0; y < board.getSize() - 1; y++) {
          Position pos_wall = new Position(x, y);

          all_possible_moves.add(Move.wall(pos_wall, Orientation.HORIZONTAL));

          all_possible_moves.add(Move.wall(pos_wall, Orientation.VERTICAL));
        }
      }
    }

    return all_possible_moves;
  }

  /**
   * Gets only legal pawn movements for a specific player.
   *
   * @param player The player to check.
   * @return A list of legal target {@link Position}s.
   */
  public List<Position> getLegalMoves(Player player) {

    List<Position> res = new ArrayList<>();

    Position pos = player.getPosition();
    int x = pos.getX();
    int y = pos.getY();

    Position[] candidates = {
      new Position(x + 1, y), new Position(x - 1, y), new Position(x, y + 1), new Position(x, y - 1)
    };

    for (Position p : candidates) {

      Move m = Move.pawn(new Position(x, y), p);

      if (RuleChecker.isMoveLegal(this, m)) {
        res.add(p);
      }
    }

    return res;
  }

  /**
   * Checks whether the debug mode is currently active.
   *
   * @return True if debug mode is enabled.
   */
  public boolean isDebug() {
    return debug;
  }

  /**
   * Sets the status of the debug mode.
   *
   * @param debug Set debug mode.
   */
  public void setDebug(boolean debug) {
    this.debug = debug;
  }

  /**
   * Checks whether the verbose mode is currently active for detailed logging.
   *
   * @return True if verbose mode is enabled.
   */
  public boolean isVerbose() {
    return verbose;
  }

  /**
   * Sets the status of the verbose mode.
   *
   * @param verbose Set verbose mode.
   */
  public void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }

  /**
   * Checks whether the blitz mode is currently enabled for fast-paced gameplay.
   *
   * @return True if blitz mode is enabled.
   */
  public boolean isBlitz() {
    return blitz;
  }

  /**
   * Sets the status of the blitz mode.
   *
   * @param blitz Set blitz mode.
   */
  public void setBlitz(boolean blitz) {
    this.blitz = blitz;
  }

  /**
   * Checks whether the contest mode is currently enabled for competitive matches.
   *
   * @return True if contest mode is enabled.
   */
  public boolean isContest() {
    return contest;
  }

  /**
   * Sets the status of the contest mode.
   *
   * @param contest Set contest mode.
   */
  public void setContest(boolean contest) {
    this.contest = contest;
  }

  /**
   * Checks whether the global AI management system is active.
   *
   * @return True if AI management is enabled.
   */
  public boolean isAi() {
    return ai;
  }

  /**
   * Sets the status of the AI management system.
   *
   * @param ai Set AI management.
   */
  public void setAi(boolean ai) {
    this.ai = ai;
  }

  /**
   * Retrieves the maximum number of turns allowed in the game.
   *
   * @return The current turn limit.
   */
  public int getTurnLimit() {
    return turnLimit;
  }

  /**
   * Sets the maximum number of turns allowed before the game ends.
   *
   * @param turnLimit Set the turn limit.
   */
  public void setTurnLimit(int turnLimit) {
    this.turnLimit = turnLimit;
  }

  /**
   * Initializes the reflection time for all players in the game. This should be called at the start
   * of a Blitz game to set the countdown timers to the specified limit.
   *
   * @param timeLimit The initial time allowed for each player in milliseconds.
   */
  public void setLimitTimePlayer(long timeLimit) {
    for (Player player : getPlayers()) {
      player.setRemainingTime(timeLimit);
    }
  }

  /**
   * Populates the history without applying the moves to the board. Useful for loading a saved game
   * history.
   *
   * @param history The list of {@link Move} objects to load into the history stack.
   */
  public void loadHistoryWithoutApplying(List<Move> history) {
    this.undoStack.clear();
    for (Move m : history) {
      this.undoStack.push(m);
    }
  }

  /**
   * Sets the game over status.
   *
   * @param b {@code true} to end the game.
   */
  public void setGameOver(boolean b) {
    this.gameOver = b;
  }
}
