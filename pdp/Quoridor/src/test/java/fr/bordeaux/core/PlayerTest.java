package fr.bordeaux.core;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class PlayerTest {
  Player player;
  Position position;

  @Before
  public void setUp() {
    position = new Position(1, 2);
    player = new Player("ali", position);
  }

  @Test
  public void constructor_shouldSetPlayerPosition() {

    assertEquals(position, player.getPosition());
  }

  @Test
  public void getPlayerPosition_shouldReturnPosition() {
    Position position = new Position(3, 4);
    Player player = new Player("aba", position);

    Position result = player.getPosition();
    String name = player.getName();
    assertNotEquals(result, null);
    assertEquals(position, result);
  }

  @Test
  public void setPlayerPosition_shouldUpdatePosition() {
    Position initialPosition = new Position(0, 0);
    Position newPosition = new Position(5, 6);

    Player player = new Player("aabb", initialPosition);
    player.setPosition(newPosition);

    assertEquals(newPosition, player.getPosition());
  }

  @Test
  public void getRemainingWalls_shouldReturnRemainingWalls() {
    Position initialPosition = new Position(1, 0);
    Player player = new Player("aabb", initialPosition);

    assertEquals(10, player.getRemainingWalls());
  }

  @Test
  public void testFullToStringFormat() {
    String expected =
        String.format(
            "Player{name='%s', pos=%s, walls=%d}",
            player.getName(), player.getPosition(), player.getRemainingWalls());
    assertEquals("toString should match the exact format", expected, player.toString());
  }
}
