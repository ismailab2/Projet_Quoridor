package fr.bordeaux.core;

import fr.bordeaux.graph.*;
import java.util.*;

/** Represents the Quoridor board. The size of the board is odd and between 3x3 and 15x15. */
public class Board {

  /** The number of cells in one dimension of the board. */
  private final int size;

  /** The graph structure representing the board connectivity. */
  private final Graph graph;

  /**
   * Constructs a new empty board of the given size.
   *
   * @param size The width and height of the board. Must be odd and between 3 and 15.
   * @throws IllegalArgumentException If the size is even or out of the [3, 15] range.
   */
  public Board(final int size) {
    if (size < 3 || size > 15 || size % 2 == 0) {
      throw new IllegalArgumentException(
          "Size must be odd and between 3 and 15, but was : " + size);
    }
    // Create the graph representing the board
    this.graph = GraphBuilder.buildEmptyGrid(size);
    this.size = size;
  }

  /**
   * Private constructor used for creating a deep copy of the board.
   *
   * @param size The size of the board.
   * @param graph The graph structure to associate with this board.
   */
  private Board(final int size, final Graph graph) {
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
  public int getCellId(final int row, final int col) {
    return row * size + col;
  }

  /**
   * Adds a wall to the board by removing the corresponding edges in the graph.
   *
   * @param pos The top-left position of the wall.
   * @param orientation The orientation of the wall (HORIZONTAL or VERTICAL).
   */
  public void addWall(final Position pos, final Orientation orientation) {
    final int posX = pos.getX();
    final int posY = pos.getY();
    final int cellU1 = getCellId(posX, posY);
    final int cellU2 = getCellId(posX, posY + 1);
    final int cellV1 = getCellId(posX + 1, posY);
    final int cellV2 = getCellId(posX + 1, posY + 1);

    if (orientation == Orientation.HORIZONTAL) {
      graph.removeEdge(cellU1, cellV1);
      graph.removeEdge(cellU2, cellV2);
    } else {
      graph.removeEdge(cellU1, cellU2);
      graph.removeEdge(cellV1, cellV2);
    }
  }

  /**
   * Removes a wall from the board by restoring the corresponding edges in the graph.
   *
   * @param pos The top-left position of the wall.
   * @param orientation The orientation of the wall.
   */
  public void removeWall(final Position pos, final Orientation orientation) {
    final int posX = pos.getX();
    final int posY = pos.getY();
    final int cellU1 = getCellId(posX, posY);
    final int cellU2 = getCellId(posX, posY + 1);
    final int cellV1 = getCellId(posX + 1, posY);
    final int cellV2 = getCellId(posX + 1, posY + 1);

    if (orientation == Orientation.HORIZONTAL) {
      graph.addEdge(cellU1, cellV1);
      graph.addEdge(cellU2, cellV2);
    } else {
      graph.addEdge(cellU1, cellU2);
      graph.addEdge(cellV1, cellV2);
    }
  }

  /**
   * Checks if a given position is within the board's boundaries.
   *
   * @param position The position to check.
   * @return {@code true} if the position is inside the board, {@code false} otherwise.
   */
  public boolean isInside(final Position position) {
    final int posX = position.getX();
    final int posY = position.getY();
    return posX >= 0 && posX < size && posY >= 0 && posY < size;
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
   * @param targetPos The top-left position for the new wall.
   * @param orientation The orientation of the new wall.
   * @return {@code true} if there is a collision, {@code false} if the placement is clear.
   */
  public boolean isWallCollision(final Position targetPos, final Orientation orientation) {
    final int posX = targetPos.getX();
    final int posY = targetPos.getY();
    final int cellU1 = getCellId(posX, posY);
    final int cellU2 = getCellId(posX, posY + 1);
    final int cellV1 = getCellId(posX + 1, posY);
    final int cellV2 = getCellId(posX + 1, posY + 1);

    final boolean hOverlap = !graph.hasEdge(cellU1, cellV1) || !graph.hasEdge(cellU2, cellV2);
    final boolean hCross = !graph.hasEdge(cellU1, cellU2) && !graph.hasEdge(cellV1, cellV2);

    final boolean vOverlap = !graph.hasEdge(cellU1, cellU2) || !graph.hasEdge(cellV1, cellV2);
    final boolean vCross = !graph.hasEdge(cellU1, cellV1) && !graph.hasEdge(cellU2, cellV2);

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
