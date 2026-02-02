package fr.bordeaux.core;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestPlayer {

  @Test
  public void constructor_shouldSetPosition() {
    Position position = new Position(1, 2);
    Player player = new Player(position);

    assertEquals(position, player.getPlayerPosition());
  }

  @Test
  public void getPlayerPosition_shouldReturnPosition() {
    Position position = new Position(3, 4);
    Player player = new Player(position);

    Position result = player.getPlayerPosition();

    assertEquals(position, result);
  }

  @Test
  public void setPlayerPosition_shouldUpdatePosition() {
    Position initialPosition = new Position(0, 0);
    Position newPosition = new Position(5, 6);

    Player player = new Player(initialPosition);
    player.setPlayerPosition(newPosition);

    assertEquals(newPosition, player.getPlayerPosition());
  }

  @Test
  public void hasWon_shouldReturnTrue() {
    Position position = new Position(1, 1);
    Player player = new Player(position);

    assertTrue(player.hasWon());
  }

  @Test
  public void toString_shouldContainPosition() {
    Position position = new Position(2, 3);
    Player player = new Player(position);

    String result = player.toString();

    assertTrue(result.contains("position"));
    assertTrue(result.contains(position.toString()));
  }
}
