package fr.bordeaux.core;

/** Represents a position on the Quoridor board. */
public class Position {
  private int x;
  private int y;

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

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof Position)) return false;
    Position other = (Position) obj;
    return this.x == other.x && this.y == other.y;
  }

  @Override
  public int hashCode() {
    return 31 * x + y;
  }

  @Override
  public String toString() {
    return "(" + x + "," + y + ")";
  }
}
