package fr.bordeaux.core;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestWall {
  @Test
  public void testWall() {
    Position position = new Position(1, 1);
    Wall wall = new Wall(position, OrientationWall.HORIZONTAL);
    assertEquals(wall.isHorizontal(), true);
    assertEquals(wall.getPositionWall(), position);
  }
}
