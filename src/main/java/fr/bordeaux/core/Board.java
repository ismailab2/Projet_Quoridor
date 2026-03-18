package fr.bordeaux.core;

import fr.bordeaux.graph.*;
import java.util.*;

/** Represents the Quoridor board. The size of the board is odd and between 3x3 and 15x15. */
public class Board {

  private final int size;
  private final Graph graph;

  /**
   * Constructs a new empty board of the given size.
   *
   * @param size The width and height of the board. Must be odd and between 3 and 15.
   * @throws IllegalArgumentException If the size is even or out of the [3, 15] range.
   */
  public Board(int size) {
    if (size < 3 || size > 15 || size % 2 == 0) {
      throw new IllegalArgumentException(
          "Size must be odd and between 3 and 15, but was : " + size);
    }
    // Create the graph representing the board
    graph = GraphBuilder.buildEmptyGrid(size);
    this.size = size;
  }

  /**
   * Private constructor used for creating a deep copy of the board.
   *
   * @param size The size of the board.
   * @param graph The graph structure to associate with this board.
   */
  private Board(int size, Graph graph) {
    this.size = size;
    this.graph = graph;
  }

  /**
   * Computes a unique integer identifier for a cell based on its coordinates.
   *
   * @param row The row index (X coordinate).
   * @param col The column index (Y coordinate).
   * @return A unique integer ID corresponding to the cell.
   */
  public int id(int row, int col) {
    return row * size + col;
  }

  /**
   * Adds a wall to the board by removing the corresponding edges in the graph.
   *
   * @param pos The top-left position of the wall.
   * @param orientation The orientation of the wall (HORIZONTAL or VERTICAL).
   */
  public void addWall(Position pos, Orientation orientation) {
    int u1 = id(pos.getX(), pos.getY());
    int u2 = id(pos.getX(), pos.getY() + 1);
    int v1 = id(pos.getX() + 1, pos.getY());
    int v2 = id(pos.getX() + 1, pos.getY() + 1);

    if (orientation == Orientation.HORIZONTAL) {
      graph.removeEdge(u1, v1);
      graph.removeEdge(u2, v2);
    } else {
      graph.removeEdge(u1, u2);
      graph.removeEdge(v1, v2);
    }
  }

  /**
   * Removes a wall from the board by restoring the corresponding edges in the graph.
   *
   * @param pos The top-left position of the wall.
   * @param orientation The orientation of the wall.
   */
  public void removeWall(Position pos, Orientation orientation) {
    int u1 = id(pos.getX(), pos.getY());
    int u2 = id(pos.getX(), pos.getY() + 1);
    int v1 = id(pos.getX() + 1, pos.getY());
    int v2 = id(pos.getX() + 1, pos.getY() + 1);

    if (orientation == Orientation.HORIZONTAL) {
      graph.addEdge(u1, v1);
      graph.addEdge(u2, v2);
    } else {
      graph.addEdge(u1, u2);
      graph.addEdge(v1, v2);
    }
  }

  /**
   * Checks if a given position is within the board's boundaries.
   *
   * @param position The position to check.
   * @return {@code true} if the position is inside the board, {@code false} otherwise.
   */
  public boolean isInside(Position position) {
    return position.getX() >= 0
        && position.getX() < size
        && position.getY() >= 0
        && position.getY() < size;
  }

  /**
   * Gets the underlying graph representation of the board.
   *
   * @return The {@link Graph} instance used by the board.
   */
  public Graph getGraph() {
    return graph;
  }

  /**
   * Gets the size of the board.
   *
   * @return The number of cells in one dimension.
   */
  public int getSize() {
    return size;
  }

  /**
   * Checks if placing a wall at the specified position would cause a collision. This includes
   * overlapping with existing walls or crossing through another wall.
   *
   * @param to The top-left position for the new wall.
   * @param orientation The orientation of the new wall.
   * @return {@code true} if there is a collision, {@code false} if the placement is clear.
   */
  public boolean isWallCollision(Position to, Orientation orientation) {
    int u1 = id(to.getX(), to.getY());
    int u2 = id(to.getX(), to.getY() + 1);
    int v1 = id(to.getX() + 1, to.getY());
    int v2 = id(to.getX() + 1, to.getY() + 1);

    // Overlap : At least a part of the wall is colliding with another wall
    // Cross : There is already a wall at the same place but different orientation, can't form a
    // cross
    boolean hOverlap = !graph.hasEdge(u1, v1) || !graph.hasEdge(u2, v2);
    boolean hCross = !graph.hasEdge(u1, u2) && !graph.hasEdge(v1, v2);

    boolean vOverlap = !graph.hasEdge(u1, u2) || !graph.hasEdge(v1, v2);
    boolean vCross = !graph.hasEdge(u1, v1) && !graph.hasEdge(u2, v2);

    return (orientation == Orientation.HORIZONTAL) ? hOverlap || hCross : vOverlap || vCross;
  }

  /**
   * Creates a deep copy of the board, including its graph state.
   *
   * @return A new {@link Board} instance identical to this one.
   */
  public Board copy() {
    return new Board(size, this.graph.copy());
  }
}
