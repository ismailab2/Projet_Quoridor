package fr.bordeaux.core;

import java.util.Objects;

/** Represents a move in Quoridor: either a pawn movement or a wall placement. Immutable and safe */
public final class Move {

  /** Defines the type of move. */
  public enum Type {
    /** A move representing a pawn jumping or stepping to a new position. */
    PAWN,
    /** A move representing the placement of a wall on the board. */
    WALL
  }

  private final Type type;
  private final Position from;
  private final Position to;
  private final Orientation orientation;

  /**
   * Private constructor to enforce the use of static factory methods.
   *
   * @param type The type of the move (PAWN or WALL).
   * @param from The starting position (only for PAWN moves).
   * @param to The target position or wall coordinate.
   * @param orientation The orientation of the wall (only for WALL moves).
   */
  private Move(Type type, Position from, Position to, Orientation orientation) {
    this.type = type;
    this.from = from;
    this.to = to;
    this.orientation = orientation;
  }

  /**
   * Creates a pawn movement.
   *
   * @param from The current position of the pawn.
   * @param to The destination position.
   * @return A new {@link Move} instance of type PAWN.
   */
  public static Move pawn(Position from, Position to) {
    return new Move(Type.PAWN, from, to, null);
  }

  /**
   * Creates a wall placement.
   *
   * @param to The top-left coordinate where the wall is placed.
   * @param orientation The orientation of the wall (HORIZONTAL or VERTICAL).
   * @return A new {@link Move} instance of type WALL.
   */
  public static Move wall(Position to, Orientation orientation) {
    return new Move(Type.WALL, null, to, orientation);
  }

  /**
   * Gets the starting position of the move.
   *
   * @return The starting {@link Position}, or {@code null} if it's a wall placement.
   */
  public Position getFrom() {
    return from;
  }

  /**
   * Gets the destination or target position of the move.
   *
   * @return The target {@link Position}.
   */
  public Position getTo() {
    return to;
  }

  /**
   * Gets the orientation of the wall.
   *
   * @return The {@link Orientation}, or {@code null} if it's a pawn move.
   */
  public Orientation getOrientation() {
    return orientation;
  }

  /**
   * Gets the type of the move.
   *
   * @return The {@link Type} of move.
   */
  public Type getType() {
    return type;
  }

  /**
   * Checks if the move is a wall placement.
   *
   * @return {@code true} if it's a wall, {@code false} otherwise.
   */
  public boolean isWall() {
    return type == Type.WALL;
  }

  /**
   * Checks if the move is a pawn movement.
   *
   * @return {@code true} if it's a pawn move, {@code false} otherwise.
   */
  public boolean isPawn() {
    return type == Type.PAWN;
  }

  /**
   * Returns a string representation of the move.
   *
   * @return A string containing move details.
   */
  @Override
  public String toString() {
    return "Move{"
        + "type="
        + type
        + ", from="
        + from
        + ", to="
        + to
        + ", orientation="
        + orientation
        + '}';
  }

  /**
   * Compares this move with another object for equality.
   *
   * @param o The object to compare with.
   * @return {@code true} if the moves are identical in type, positions, and orientation.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Move)) return false;
    Move move = (Move) o;
    return type == move.type
        && ((from == null && move.from == null) || from.equals(move.from))
        && to.equals(move.to)
        && orientation == move.orientation;
  }

  /**
   * Generates a hash code for this move.
   *
   * @return The hash code value.
   */
  @Override
  public int hashCode() {
    return Objects.hash(type, from, to, orientation);
  }
}
