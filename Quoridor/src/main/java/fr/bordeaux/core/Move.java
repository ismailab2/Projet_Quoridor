package fr.bordeaux.core;

/** Represents a move in Quoridor: either a pawn movement or a wall placement. Immutable and safe */
public class Move {

  public enum Type {
    PAWN,
    WALL
  }

  private final Type type;
  private final Position to;
  private final Orientation orientation;

  private Move(Type type, Position to, Orientation orientation) {
    this.type = type;
    this.to = to;
    this.orientation = orientation;
  }

  public static Move pawn(Position to) {
    return new Move(Type.PAWN, to, null);
  }

  public static Move wall(Position to, Orientation orientation) {
    return new Move(Type.WALL, to, orientation);
  }

  public Type getType() {
    return type;
  }

  public Position getTo() {
    return to;
  }

  public Orientation getOrientation() {
    return orientation;
  }

  public boolean isWall() {
    return type == Type.WALL;
  }

  public boolean isPawn() {
    return type == Type.PAWN;
  }
}
