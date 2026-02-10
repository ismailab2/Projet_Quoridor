package fr.bordeaux.core;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class MoveTest {

  private Move pawnMove;
  private Move hWallMove;
  private Move vWallMove;

  @Before
  public void setUp() {
    pawnMove = Move.pawn(new Position(1, 1));
    hWallMove = Move.wall(new Position(2, 2), Orientation.HORIZONTAL);
    vWallMove = Move.wall(new Position(3, 3), Orientation.VERTICAL);
  }

  @Test
  public void testMoveTypes() {
    assertEquals(Move.Type.PAWN, pawnMove.getType());
    assertEquals(Move.Type.WALL, hWallMove.getType());
    assertEquals(Move.Type.WALL, vWallMove.getType());
    assertTrue(pawnMove.isPawn());
    assertTrue(hWallMove.isWall());
    assertTrue(vWallMove.isWall());
  }

  @Test
  public void testPawnMoveData() {
    // assertEquals(new Position(1, 1), pawnMove.getTo());
    assertNull(pawnMove.getOrientation());
  }

  @Test
  public void testHorizontalWallMoveData() {
    // assertEquals(new Position(2, 2), hWallMove.getTo());
    assertEquals(Orientation.HORIZONTAL, hWallMove.getOrientation());
  }

  @Test
  public void testVerticalWallMoveData() {
    // assertEquals((new Position(3, 3)), vWallMove.getTo());
    assertEquals(Orientation.VERTICAL, vWallMove.getOrientation());
  }
}
