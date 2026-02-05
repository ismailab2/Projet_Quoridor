package fr.bordeaux.core;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class PositionTest {

  private Position pos;

  @Before
  public void setUp() {
    pos = new Position(3, 5);
  }

  @Test
  public void testToStringReturnsCorrectFormat() {
    String str = pos.toString();
    assertNotNull(str);
    assertEquals("(3,5)", str);
  }

  @Test
  public void testToStringDifferentValues() {
    Position pos2 = new Position(0, 0);
    assertEquals("(0,0)", pos2.toString());

    Position pos3 = new Position(8, 8);
    assertEquals("(8,8)", pos3.toString());
  }
}
