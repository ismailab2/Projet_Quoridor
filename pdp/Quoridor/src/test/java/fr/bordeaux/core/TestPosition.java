package fr.bordeaux.core;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestPosition {
  @Test
  public void constructor_shouldSetXAndY() {
    Position position = new Position(0, 0);
    assertEquals(position.getX(), 0);
    assertEquals(position.getY(), 0);
  }
}
