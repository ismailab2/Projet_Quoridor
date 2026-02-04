package fr.bordeaux.core;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class TestPlayer {

  @Test
  public void constructor_shouldSetPlayerPosition() {
    Position position = new Position(1, 2);
    Player player = new Player("ali", position);

    assertEquals(position, player.getPlayerPosition());
  }

  @Test
  public void getPlayerPosition_shouldReturnPosition() {
    Position position = new Position(3, 4);
    Player player = new Player("aba", position);

    Position result = player.getPlayerPosition();

    assertEquals(position, result);
  }

  @Test
  public void setPlayerPosition_shouldUpdatePosition() {
    Position initialPosition = new Position(0, 0);
    Position newPosition = new Position(5, 6);

    Player player = new Player("aabb", initialPosition);
    player.setPlayerPosition(newPosition);

    assertEquals(newPosition, player.getPlayerPosition());
  }

  @Test
  public void hasWon_shouldReturnTrue() {
    Position position = new Position(0, 4);
    Position position2 = new Position(8, 4);
    Player player = new Player("nn", position);
    Player player1 = new Player("nn1", position2);
    List<Player> players = new ArrayList<Player>();
    players.add(player);
    players.add(player1);
    Board board = new Board(players);

    assertFalse(player.hasWon(board));
  }
}
