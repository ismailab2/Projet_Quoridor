package fr.bordeaux.core;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import fr.bordeaux.graph.Graph;

public class BoardTest {

  private Board board;
  private final int SIZE = 9;

  @Before
  public void setUp() {
    board = new Board(SIZE);
  }

  @Test
  public void testBoardInitialization() {
    assertEquals(SIZE, board.getSize());
    assertNotNull(board.getGraph());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidBoardSizeEven() {
    new Board(8);
  }

  @Test
  public void testIsInside() {
    assertTrue(board.isInside(new Position(0, 0)));
    assertTrue(board.isInside(new Position(8, 8)));
    assertFalse(board.isInside(new Position(-1, 4)));
    assertFalse(board.isInside(new Position(9, 4)));
  }

  @Test
  public void testIdConversion() {
    assertEquals(0, board.id(0, 0));
    assertEquals(SIZE + 1, board.id(1, 1));
    assertEquals((SIZE * SIZE) - 1, board.id(8, 8)); //63
  }

  @Test
  public void testAddHorizontalWallRemovesEdges() {
    int row = 2;
    int col = 2;
    board.addWall(row, col, Orientation.HORIZONTAL);

    Graph g = board.getGraph();

    assertFalse(g.getNeighbors(board.id(2, 2)).contains(board.id(3, 2)));
    assertFalse(g.getNeighbors(board.id(2, 3)).contains(board.id(3, 3)));
    assertTrue(g.getNeighbors(board.id(2, 2)).contains(board.id(2, 3)));
  }

  @Test
  public void testAddVerticalWallRemovesEdges() {
    int row = 4;
    int col = 1;
    board.addWall(row, col, Orientation.VERTICAL);

    Graph g = board.getGraph();

    assertFalse(g.getNeighbors(board.id(4, 1)).contains(board.id(4, 2)));
    assertFalse(g.getNeighbors(board.id(5, 1)).contains(board.id(5, 2)));
    assertTrue(g.getNeighbors(board.id(4, 1)).contains(board.id(5, 1)));
  }

  @Test
  public void testHorizontalWallCollision() {
    board.addWall(2, 2, Orientation.HORIZONTAL);

    // Overlap
    assertTrue(board.isWallCollision(new Position(2, 2), Orientation.HORIZONTAL));

    // Partial overlap
    assertTrue(board.isWallCollision(new Position(2, 1), Orientation.HORIZONTAL));
    assertTrue(board.isWallCollision(new Position(2, 3), Orientation.HORIZONTAL));

    // Cross
    assertTrue(board.isWallCollision(new Position(2, 2), Orientation.VERTICAL));

    // Valid cases
    assertFalse(board.isWallCollision(new Position(2, 4), Orientation.HORIZONTAL));
    assertFalse(board.isWallCollision(new Position(0, 2), Orientation.HORIZONTAL));
  }

  @Test
  public void testVerticalWallCollision() {
    board.addWall(4, 4, Orientation.VERTICAL);

    // Overlap
    assertTrue(board.isWallCollision(new Position(4, 4), Orientation.VERTICAL));

    // Partial overlap
    assertTrue(board.isWallCollision(new Position(3, 4), Orientation.VERTICAL));
    assertTrue(board.isWallCollision(new Position(5, 4), Orientation.VERTICAL));

    // Cross
    assertTrue(board.isWallCollision(new Position(4, 4), Orientation.HORIZONTAL));

    // Valid cases
    assertFalse(board.isWallCollision(new Position(3, 3), Orientation.HORIZONTAL));
    assertFalse(board.isWallCollision(new Position(5, 4), Orientation.HORIZONTAL));
  }

  @Test
  public void testNoCollisionWhenWallsAreParallelAdjacent() {
    board.addWall(1, 1, Orientation.HORIZONTAL);

    //Valid cases
    assertFalse(board.isWallCollision(new Position(0, 1), Orientation.HORIZONTAL));
    assertFalse(board.isWallCollision(new Position(2, 1), Orientation.HORIZONTAL));
  }
}
