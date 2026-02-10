package fr.bordeaux.core;

import java.util.List;
import java.util.function.Predicate;

/**
 * Represents the state of a Quoridor game. Manages the board, players, current player, and move
 * history. Undo/Redo functionality is implemented using stacks.
 */
public class GameState {

  private final Board board;
  private final List<Player> players;
  private int currentPlayer = 0;

  public GameState(Board board, List<Player> players) {
    if (players.size() != 2 && players.size() != 4) {
      throw new IllegalArgumentException(
          "Number of players must be 2 or 4, but was : " + players.size());
    }
    this.board = board;
    this.players = players;
  }

  public void applyMove(Move move) {
    if (move.isPawn()) {
      currentPlayer().setPosition(move.getTo());
    } else {
      board.addWall(move.getTo(), move.getOrientation());
      currentPlayer().useWall();
    }
    nextPlayer();
  }

  public boolean isGameOver() {
    for (Player player : players) {
      if (hasReachedGoal(player)) {
        return true;
      }
    }
    return false;
  }

  // Returns if a player has won the game, uses predicate from getWinCondition
  public boolean hasReachedGoal(Player player) {
    int currentId = board.id(player.getPosition().getX(), player.getPosition().getY());
    return getWinCondition(player).test(currentId);
  }

  // Returns the predicate for a player to win, used in BFS
  public Predicate<Integer> getWinCondition(Player player) {
    Position start = player.getStartingPosition();
    int size = board.getSize();

    // Start first line, middle column
    if (start.getX() == 0) {
      return id -> (id / size) == size - 1; // Last row
    }

    // Start last line, middle column
    else if (start.getX() == size - 1) {
      return id -> (id / size) == 0; // first row
    }

    // Start first column, middle line
    else if (start.getY() == 0) {
      return id -> (id % size) == size - 1; // Last column
    }
    // Start last column, middle line
    else {
      return id -> (id % size) == 0; // First column
    }
  }

  public Player currentPlayer() {
    return players.get(currentPlayer);
  }

  public void nextPlayer() {
    currentPlayer = (currentPlayer + 1) % players.size();
  }

  public Board getBoard() {
    return board;
  }

  public List<Player> getPlayers() {
    return players;
  }
}
