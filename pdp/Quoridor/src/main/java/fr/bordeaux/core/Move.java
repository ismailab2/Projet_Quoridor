package fr.bordeaux.core;

import java.util.Objects;

/**
 * Represents a move in Quoridor: either a pawn movement or a wall placement. Immutable and safe for
 */
public class Move {
  public enum MoveType {
    PAWN_MOVE,
    WALL_PLACEMENT
  }

  private final Player player;
  private final MoveType moveType;
  private final Position from;
  private final Position to;
  private final Wall wall;
  private final Board board;

  /** Constructor for a pawn movement move. */
  public Move(Player player, Board board, Position from, Position to) {
    this.player = Objects.requireNonNull(player, "Player cannot be null");
    this.from = Objects.requireNonNull(from, "From cannot be null");
    this.to = Objects.requireNonNull(to, "To cannot be null");

    int dx = Math.abs(to.getX() - from.getX());
    int dy = Math.abs(to.getY() - from.getY());
    if (!(board.isMoveAllowed(getFrom(), getTo()))) {
      throw new IllegalArgumentException(
          "Invalid pawn move: can only move one square up, down, left, or right");
    }

    this.moveType = MoveType.PAWN_MOVE;
    this.wall = null;
    this.board = board;
  }

  /** Constructor for a wall placement move. */
  public Move(Player player, Board board, Wall wall) {
    this.player = Objects.requireNonNull(player, "Player cannot be null");
    this.from = null;
    this.to = null;
    this.moveType = MoveType.WALL_PLACEMENT;
    this.wall = Objects.requireNonNull(wall, "Wall cannot be null");
    this.board = board;
  }

  public boolean isWallPlacement() {
    return moveType == MoveType.WALL_PLACEMENT;
  }

  public MoveType getType() {
    return moveType;
  }

  public Player getPlayer() {
    return player;
  }

  public Position getFrom() {
    return from;
  }

  public Position getTo() {
    return to;
  }

  public Wall getWall() {
    return wall;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof Move)) return false;
    Move other = (Move) obj;
    return Objects.equals(player, other.player)
        && moveType == other.moveType
        && Objects.equals(from, other.from)
        && Objects.equals(to, other.to)
        && Objects.equals(wall, other.wall);
  }

  @Override
  public int hashCode() {
    return Objects.hash(player, moveType, from, to, wall);
  }

  @Override
  public String toString() {
    if (isWallPlacement()) {
      return "Wall Move: " + wall + " by " + player.getName();
    } else {
      return "Pawn Move: " + player.getName() + " from " + from + " to " + to;
    }
  }
}
