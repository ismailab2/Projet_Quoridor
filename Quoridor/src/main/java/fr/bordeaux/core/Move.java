package fr.bordeaux.core;

/**
 * Represents a move in Quoridor: either a pawn movement or a wall placement. Immutable and safe for
 */
public class Move {

  public enum Type {
    PAWN,
    WALL
  }

  private final Type type;
  private final Position to;
  private final Wall wall;

  Move(Type type, Position to, Wall wall) {
    this.type = type;
    this.to = to;
    this.wall = wall;
  }

  public static Move pawn(Position to) {
    return new Move(Type.PAWN, to, null);
  }

  public static Move wall(Wall wall) {
    return new Move(Type.WALL, null, wall);
  }

  public Type getType() {
    return type;
  }

  public Position getTo() {
    return to;
  }

  public Wall getWall() {
    return wall;
  }
}
