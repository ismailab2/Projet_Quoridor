package fr.bordeaux.core;

/** Represents a position on the Quoridor board. */
public class Position {
  int x;
  int y;

  public Position(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }
}
