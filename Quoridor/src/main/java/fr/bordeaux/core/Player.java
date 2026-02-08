package fr.bordeaux.core;

/**
 * Represents a player in Quoridor. A player has a name, a pawn position, and a number of remaining
 * walls.
 */
public class Player {
  private final String name;
  private Position position;
  private int remainingWalls = 10;
  private final Position startingPosition;

  public Player(String name, Position positionStart) {
    this.name = name;
    this.position = positionStart;
    this.startingPosition = positionStart;
  }

  public String getName() {
    return name;
  }

  public Position getStartingPosition() {
    return startingPosition;
  }

  public Position getPosition() {
    return position;
  }

  public void setPosition(Position position) {
    this.position = position;
  }

  public int getRemainingWalls() {
    return remainingWalls;
  }

  public void useWall() {
    remainingWalls--;
  }

  @Override
  public String toString() {
    return String.format("Player{name='%s', pos=%s, walls=%d}", name, position, remainingWalls);
  }
}
