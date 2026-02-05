package fr.bordeaux.core;

public class Wall {
  public enum Orientation {
    HORIZONTAL,
    VERTICAL;
  }

  private Position position;
  private Orientation orientation;

  public Wall(Position position, Orientation orientation) {
    this.position = position;
    this.orientation = orientation;
  }

  public Position getPosition() {
    return position;
  }

  public Orientation getOrientation() {
    return orientation;
  }

  public boolean conflictsWith(Wall other) {
    return position.equals(other.position) && orientation == other.orientation;
  }

  public boolean crosses(Wall other) {
    return orientation != other.orientation && position.equals(other.position);
  }
}
