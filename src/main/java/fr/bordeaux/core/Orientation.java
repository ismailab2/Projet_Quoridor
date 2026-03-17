package fr.bordeaux.core;

/**
 * Defines the possible orientations for wall placement on the board. A wall can either be placed
 * horizontally or vertically, spanning two cell edges.
 */
public enum Orientation {

  /** Represents a wall placed horizontally between rows. */
  HORIZONTAL,

  /** Represents a wall placed vertically between columns. */
  VERTICAL;

  /**
   * Returns a short string representation of the orientation.
   *
   * @return {@code "h"} for HORIZONTAL or {@code "v"} for VERTICAL.
   */
  @Override
  public String toString() {
    return (this == HORIZONTAL) ? "h" : "v";
  }
}
