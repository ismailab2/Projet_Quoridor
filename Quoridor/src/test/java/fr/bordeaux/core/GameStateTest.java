package fr.bordeaux.core;

import static org.junit.Assert.*;

import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class GameStateTest {

  private Board board;
  private Player p1;
  private Player p2;
  private GameState gameState;

  @Before
  public void setUp() {
    board = new Board();

    p1 = new Player("Alice", new Position(4, 0));
    p2 = new Player("Bob", new Position(4, 8));

    gameState = new GameState(board, List.of(p1, p2));
  }

  @Test
  public void testPlayersPlacedOnBoard() {
    assertEquals(p1, board.getCell(new Position(4, 0)).getPlayer());
    assertEquals(p2, board.getCell(new Position(4, 8)).getPlayer());
  }

  @Test
  public void testInitialCurrentPlayer() {
    assertEquals(p1, gameState.currentPlayer());
  }

  @Test
  public void testNextPlayerSwitchesTurn() {
    gameState.nextPlayer();
    assertEquals(p2, gameState.currentPlayer());
  }

  @Test
  public void testNextPlayerLoopsBack() {
    gameState.nextPlayer(); // p2
    gameState.nextPlayer(); // back to p1
    assertEquals(p1, gameState.currentPlayer());
  }

  @Test
  public void testGetBoard() {
    assertSame(board, gameState.getBoard());
  }

  @Test
  public void testGetPlayers() {
    List<Player> players = gameState.getPlayers();
    assertEquals(2, players.size());
    assertTrue(players.contains(p1));
    assertTrue(players.contains(p2));
  }
}
