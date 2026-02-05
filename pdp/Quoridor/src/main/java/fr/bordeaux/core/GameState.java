package fr.bordeaux.core;

import java.util.List;

/**
 * Represents the state of a Quoridor game. Manages the board, players, current player, and move
 * history. Undo/Redo functionality is implemented using stacks.
 */
public class GameState {

  private final Board board;
  private final List<Player> players;
  private int currentPlayer = 0;

  public GameState(Board board, List<Player> players) {
    this.board = board;
    this.players = players;
    for (Player p : players) board.getCell(p.getPosition()).setPlayer(p);
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
