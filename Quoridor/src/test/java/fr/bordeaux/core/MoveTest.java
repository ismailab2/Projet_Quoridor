package fr.bordeaux.core;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class MoveTest {

  Move move, move1;
  Position pos;
  Wall wall;

  @Before
  public void setUp() {
    pos = new Position(0, 0);
    wall = new Wall(pos, Wall.Orientation.HORIZONTAL);
    move = new Move(Move.Type.PAWN, pos, wall);
    move1 = new Move(Move.Type.WALL, new Position(2, 2), wall);
  }

  @Test
  public void testMove() {
    assertNotEquals(Move.Type.WALL, move.getType());
    assertEquals(Move.Type.WALL, move1.getType());
    assertEquals(pos, move.getTo());
    assertEquals(wall, move1.getWall());
  }

  @Test
  public void testPawnMove() {
    Position pos = new Position(0, 0);
    assertEquals(pos, move.pawn(pos).getTo());
    assertNotEquals(new Position(2, 3), move1.pawn(pos).getTo());
  }

  @Test
  public void testWallMove() {
    Wall wall = new Wall(pos, Wall.Orientation.HORIZONTAL);
    Wall wall1 = new Wall(new Position(2, 8), Wall.Orientation.HORIZONTAL);
    assertEquals(wall, move.wall(wall).getWall());
    assertNotEquals(wall1, move.wall(wall).getWall());
  }
}
