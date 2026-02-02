package fr.bordeaux.core;

public class Wall {
  Position position;
  OrientationWall orientation;

  public Wall(Position position, OrientationWall orientation) {
    this.position = position;
    this.orientation = orientation;
  }

  public Position getPositionWall() {
    return position;
  }

  public boolean isHorizontal() {
    return orientation == OrientationWall.HORIZONTAL;
  }
}
