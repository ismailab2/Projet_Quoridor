package fr.bordeaux.core;

import fr.bordeaux.graph.*;
import java.util.*;

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

  private Board(int size, Graph graph) {
    this.size = size;
    this.graph = graph;
  }

  public int id(int row, int col) {
    return row * size + col;
  }

  // Method to add a wall by removing corresponding edges.
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

  // Method to remove a wall by adding back the corresponding edges
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

  public Board copy() {
    return new Board(size, this.graph.copy());
  }
}
