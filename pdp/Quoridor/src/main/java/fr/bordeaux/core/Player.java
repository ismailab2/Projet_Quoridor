package fr.bordeaux.core;

import java.util.Objects;

/**
 * Represents a player in Quoridor. A player has a name, a pawn position, and a number of remaining
 * walls.
 */
public class Player {

  private final String name;
  private Position position;
  private int remainingWalls;

  private static final int MAX_WALLS = 10; // standard Quoridor

  public Player(String name, Position startPosition) {
    this.name = Objects.requireNonNull(name, "Player name cannot be null");
    this.position = Objects.requireNonNull(startPosition, "Start position cannot be null");
    this.remainingWalls = MAX_WALLS;
  }

  public String getName() {
    return name;
  }

  public Position getPlayerPosition() {
    return position;
  }

  public void setPlayerPosition(Position position) {
    this.position = Objects.requireNonNull(position, "Position cannot be null");
  }

  public int getRemainingWalls() {
    return remainingWalls;
  }

  /**
   * Place a wall and decrease the count.
   *
   * @return true if the wall was placed successfully
   */
  public boolean useWall() {
    if (remainingWalls > 0) {
      remainingWalls--;
      return true;
    }
    return false;
  }

  /** Restore a wall */
  public void restoreWall() {
    if (remainingWalls < MAX_WALLS) {
      remainingWalls++;
    }
  }

  /**
   * Check if the player has won the game. In Quoridor, a player wins when their pawn reaches the
   * opposite side of the board.
   *
   * @param board the current board
   * @return true if the player has reached the winning line
   */
  public boolean hasWon(Board board) {
    int winningRow = board.getWinningRowForPlayer(this);
    return position.getX() == winningRow;
  }

  @Override
  public String toString() {
    return String.format("Player{name='%s', pos=%s, walls=%d}", name, position, remainingWalls);
  }
}
