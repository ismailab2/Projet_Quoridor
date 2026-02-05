package fr.bordeaux.core;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class BoardTest {

  private Board board;
  private Player p1;
  private Player p2;

  @Before
  public void setUp() {
    board = new Board();
    p1 = new Player("Alice", new Position(0, 0));
    p2 = new Player("Bob", new Position(8, 8));
    board.getCell(new Position(0, 0)).setPlayer(p1);
    board.getCell(new Position(8, 8)).setPlayer(p2);

    board.addWall(new Wall(new Position(1, 1), Wall.Orientation.HORIZONTAL));
    board.addWall(new Wall(new Position(2, 2), Wall.Orientation.VERTICAL));
  }

  @Test
  public void testBoardInitialization() {
    for (int x = 0; x < Board.SIZE; x++) {
      for (int y = 0; y < Board.SIZE; y++) {
        Cell cell = board.getCell(new Position(x, y));
        assertNotNull(cell);
      }
    }
  }

  @Test
  public void testIsInsideTrue() {
    assertTrue(board.isInside(new Position(0, 0)));
    assertTrue(board.isInside(new Position(8, 8)));
    assertTrue(board.isInside(new Position(4, 5)));
  }

  @Test
  public void testIsInsideFalse() {
    assertFalse(board.isInside(new Position(-1, 0)));
    assertFalse(board.isInside(new Position(0, -1)));
    assertFalse(board.isInside(new Position(9, 0)));
    assertFalse(board.isInside(new Position(0, 9)));
  }

  @Test
  public void testGetCellReturnsCorrectCell() {
    Position p = new Position(3, 4);
    Cell cell = board.getCell(p);

    assertEquals(3, cell.getPosition().getX());
    assertEquals(4, cell.getPosition().getY());
  }

  @Test
  public void testAddHorizontalWall() {
    Wall wall = new Wall(new Position(2, 2), Wall.Orientation.HORIZONTAL);
    board.addWall(wall);

    assertEquals(3, board.getWalls().size());

    assertTrue(board.getCell(new Position(2, 2)).hasBottomWall());
    assertTrue(board.getCell(new Position(3, 2)).hasBottomWall());

    assertTrue(board.getCell(new Position(2, 3)).hasTopWall());
    assertTrue(board.getCell(new Position(3, 3)).hasTopWall());
  }

  @Test
  public void testAddVerticalWall() {
    Wall wall = new Wall(new Position(4, 1), Wall.Orientation.VERTICAL);
    board.addWall(wall);

    assertEquals(3, board.getWalls().size());

    assertTrue(board.getCell(new Position(4, 1)).hasRightWall());
    assertTrue(board.getCell(new Position(4, 2)).hasRightWall());

    assertTrue(board.getCell(new Position(5, 1)).hasLeftWall());
    assertTrue(board.getCell(new Position(5, 2)).hasLeftWall());
  }

  @Test
  public void testGetWallsReturnsCopy() {
    board.addWall(new Wall(new Position(1, 1), Wall.Orientation.HORIZONTAL));

    assertEquals(3, board.getWalls().size());

    board.getWalls().clear();

    assertEquals(3, board.getWalls().size());
  }

  @Test
  public void testToStringContainsBoard() {
    String str = board.toString();
    assertNotNull(str);
    assertTrue(str.contains("Board:"));
  }

  @Test
  public void testToStringShowsWallsCount() {
    String str = board.toString();
    assertTrue(str.contains("Walls posés: 2"));
  }

  @Test
  public void testToStringShowsPlayers() {
    String str = board.toString();
    assertTrue(str.contains(" A "));
    assertTrue(str.contains(" B "));
  }
}
