package fr.bordeaux.core;

import static org.junit.Assert.*;

import org.junit.Test;

public class WallTest {

  @Test
  public void testGetters() {
    Position pos = new Position(2, 3);
    Wall wall = new Wall(pos, Wall.Orientation.HORIZONTAL);

    assertEquals(pos, wall.getPosition());
    assertEquals(Wall.Orientation.HORIZONTAL, wall.getOrientation());
  }

  @Test
  public void testConflictsWith() {
    Wall wall1 = new Wall(new Position(1, 1), Wall.Orientation.HORIZONTAL);
    Wall wall2 = new Wall(new Position(1, 1), Wall.Orientation.HORIZONTAL);
    Wall wall3 = new Wall(new Position(2, 2), Wall.Orientation.HORIZONTAL);

    assertTrue(wall1.conflictsWith(wall2));
    assertFalse(wall1.conflictsWith(wall3));
  }

  @Test
  public void testCrosses() {
    Wall h1 = new Wall(new Position(1, 1), Wall.Orientation.HORIZONTAL);
    Wall v1 = new Wall(new Position(1, 1), Wall.Orientation.VERTICAL);
    Wall v2 = new Wall(new Position(2, 1), Wall.Orientation.VERTICAL);
    Wall h2 = new Wall(new Position(1, 1), Wall.Orientation.HORIZONTAL);

    assertTrue(h1.crosses(v1));
    assertFalse(h1.crosses(v2));
    assertFalse(h1.crosses(h2));
    assertFalse(h1.crosses(new Wall(new Position(2, 2), Wall.Orientation.HORIZONTAL)));
  }
}
