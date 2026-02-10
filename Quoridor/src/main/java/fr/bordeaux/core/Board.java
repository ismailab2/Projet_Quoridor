package fr.bordeaux.core;

import fr.bordeaux.graph.*;

/** Represents the Quoridor board. The size of the board is odd and between 3x3 and 15x15. */
public class Board {

  private final int size;

  private final Graph graph;

  public Board(int size) {
    if (size < 3 || size > 15 || size % 2 == 0) {
      throw new IllegalArgumentException(
          "Size must be odd and between 3 and 15, but was : " + size);
    }
    // Create the graph representing the board
    graph = GraphBuilder.buildEmptyGrid(size);
    this.size = size;
  }

  public int id(int row, int col) {
    return row * size + col;
  }

  // Method to add a wall by removing corresponding edges.
  public void addWall(int row, int col, Orientation orientation) {
    int u1 = id(row, col);
    int u2 = id(row, col + 1);
    int v1 = id(row + 1, col);
    int v2 = id(row + 1, col + 1);
    switch (orientation) {
      case HORIZONTAL -> {
        graph.removeEdge(u1, v1);
        graph.removeEdge(u2, v2);
      }
      case VERTICAL -> {
        graph.removeEdge(u1, u2);
        graph.removeEdge(v1, v2);
      }
    }
  }

  // Check if position is inside the board.
  public boolean isInside(Position position) {
    return position.getX() >= 0
        && position.getX() < size
        && position.getY() >= 0
        && position.getY() < size;
  }

  public Graph getGraph() {
    return graph;
  }

  public int getSize() {
    return size;
  }

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
}
