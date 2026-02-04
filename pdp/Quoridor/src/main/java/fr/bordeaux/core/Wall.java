package fr.bordeaux.core;

public class Wall {

  public enum OrientationWall {
    HORIZONTAL,
    VERTICAL;
  }

  Position position;
  OrientationWall orientation;

  public Wall(Position position, OrientationWall orientation) {
    this.position = position;
    this.orientation = orientation;
  }

  public boolean conflictsWith(Wall wall) {
    return position.equals(wall.position) && orientation.equals(wall.orientation);
  }

  public boolean blocksMovement(Position from, Position to) {
    return position.equals(from) && orientation.equals(to);
  }

  public Position getPositionWall() {
    return position;
  }

  public OrientationWall getOrientation() {
    return orientation;
  }

  public boolean isHorizontal() {
    return orientation == OrientationWall.HORIZONTAL;
  }
}
