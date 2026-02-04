package fr.bordeaux.core;

/**
 * Represents a single cell on the Quoridor board. Each cell can have a horizontal wall, vertical
 * wall, or a player.
 */
public class Cell {

  private boolean horizontalWall;
  private boolean verticalWall;
  private boolean hasPlayer;

  /** Clears the cell: no walls, no player */
  public void clear() {
    horizontalWall = false;
    verticalWall = false;
    hasPlayer = false;
  }

  /** Returns true if there is a horizontal wall on this cell */
  public boolean hasHorizontalWall() {
    return horizontalWall;
  }

  /** Sets or removes a horizontal wall on this cell */
  public void setHorizontalWall(boolean wall) {
    horizontalWall = wall;
  }

  /** Returns true if there is a vertical wall on this cell */
  public boolean hasVerticalWall() {
    return verticalWall;
  }

  /** Sets or removes a vertical wall on this cell */
  public void setVerticalWall(boolean wall) {
    verticalWall = wall;
  }

  /** Returns true if a player is currently on this cell */
  public boolean hasPlayer() {
    return hasPlayer;
  }

  /** Sets or removes a player on this cell */
  public void setPlayer(boolean player) {
    hasPlayer = player;
  }
}
