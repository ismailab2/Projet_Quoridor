package fr.bordeaux.core;

import java.util.Objects;

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
  public String toString() {
    return "Position: "+this.x+", "+this.y;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    Position position = (Position)obj;
    return this.x==position.x && this.y==position.y;
  }
  @Override
  public int hashCode() { return Objects.hash(x, y); }
}
