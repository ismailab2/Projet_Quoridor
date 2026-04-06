package fr.bordeaux.core;

import java.util.Objects;

/** Represents a position on the Quoridor board. */
public class Position {

  private final int x;
  private final int y;

  /**
   * Constructs a new Position with specified coordinates.
   *
   * @param x The x-coordinate.
   * @param y The y-coordinate.
   */
  public Position(final int x, final int y) {
    this.x = x;
    this.y = y;
  }

  /**
   * Gets the x-coordinate of this position.
   *
   * @return The x value.
   */
  public int getX() {
    return x;
  }

  /**
   * Gets the y-coordinate of this position.
   *
   * @return The y value.
   */
  public int getY() {
    return y;
  }

  /**
   * Returns a string representation of the position.
   *
   * @return A string in the format "Position: x, y".
   */
  @Override
  public String toString() {
    return "Position: " + this.x + ", " + this.y;
  }

  /**
   * Compares this position to another object for equality.
   *
   * @param obj The object to compare with.
   * @return {@code true} if the coordinates are identical, {@code false} otherwise.
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final Position position = (Position) obj;
    return this.x == position.x && this.y == position.y;
  }

  /**
   * Generates a hash code based on the x and y coordinates.
   *
   * @return The hash code value for this position.
   */
  @Override
  public int hashCode() {
    return Objects.hash(x, y);
  }
}
