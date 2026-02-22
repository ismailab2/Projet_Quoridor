package fr.bordeaux.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Represents a position on the Quoridor board. */
public class Position {

  int x;
  int y;

  @JsonCreator
  public Position(@JsonProperty("x") int x, @JsonProperty("y") int y) {
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
  public boolean equals(Object o) {
    if (this == o) return true; // même référence
    if (o == null || getClass() != o.getClass()) return false;
    Position position = (Position) o;
    return x == position.x && y == position.y;
  }

  @Override
  public int hashCode() {
    return 31 * x + y;
  }

  @Override
  public String toString() {
    return "Position [x=" + x + ", y=" + y + "]";
  }
}
