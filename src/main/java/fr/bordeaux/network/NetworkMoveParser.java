package fr.bordeaux.network;

import fr.bordeaux.core.Move;
import fr.bordeaux.core.Orientation;
import fr.bordeaux.core.Position;

/** Utility class used to convert textual network moves into Move objects. */
public final class NetworkMoveParser {

  private NetworkMoveParser() {}

  /**
   * Parses a board coordinate such as e2 into a Position.
   *
   * @param str textual board coordinate
   * @return parsed position
   */
  public static Position parsePosition(String str) {
    if (str == null || str.length() < 2) {
      throw new IllegalArgumentException("Invalid position: " + str);
    }

    int row = Integer.parseInt(str.substring(1)) - 1;
    int col = str.charAt(0) - 'a';

    return new Position(row, col);
  }

  /**
   * Parses a textual move received from the network.
   *
   * @param move move in textual format
   * @return corresponding Move object
   */
  public static Move parseMove(String move) {
    if (move == null || move.isBlank()) {
      throw new IllegalArgumentException("Empty move");
    }

    move = move.trim();

    if (move.contains("-")) {
      String[] parts = move.split("-");
      if (parts.length != 2) {
        throw new IllegalArgumentException("Invalid pawn move: " + move);
      }

      Position from = parsePosition(parts[0]);
      Position to = parsePosition(parts[1]);
      return Move.pawn(from, to);
    }

    if (move.endsWith("h") || move.endsWith("v")) {
      Position pos = parsePosition(move.substring(0, move.length() - 1));
      Orientation orientation =
          move.charAt(move.length() - 1) == 'v' ? Orientation.VERTICAL : Orientation.HORIZONTAL;
      return Move.wall(pos, orientation);
    }

    throw new IllegalArgumentException("Unknown move format: " + move);
  }
}
