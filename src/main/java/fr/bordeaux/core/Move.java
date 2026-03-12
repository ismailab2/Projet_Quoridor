package fr.bordeaux.core;

import java.util.Objects;

/** Represents a move in Quoridor: either a pawn movement or a wall placement. Immutable and safe */
public class Move {

  public enum Type {
    PAWN,
    WALL
  }

  private final Type type;
  private final Position from;
  private final Position to;
  private final Orientation orientation;

  private Move(Type type, Position from, Position to, Orientation orientation) {
    this.type = type;
    this.from = from;
    this.to = to;
    this.orientation = orientation;
  }

  public static Move pawn(Position from, Position to) {
    return new Move(Type.PAWN, from, to, null);
  }

  public static Move wall(Position to, Orientation orientation) {
    return new Move(Type.WALL, null, to, orientation);
  }

  public Position getFrom() {
    return from;
  }

  public Position getTo() {
    return to;
  }

  public Orientation getOrientation() {
    return orientation;
  }

  public Type getType() {
    return type;
  }

  public boolean isWall() {
    return type == Type.WALL;
  }

  public boolean isPawn() {
    return type == Type.PAWN;
  }

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

  @Override
  public int hashCode() {
    return Objects.hash(type, from, to, orientation);
  }
}
