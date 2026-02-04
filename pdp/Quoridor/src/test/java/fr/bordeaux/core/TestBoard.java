package fr.bordeaux.core;

import static org.junit.Assert.*;

import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;

public class TestBoard {

  private Player alice;
  private Player bob;
  private Board board;

  @Before
  public void setUp() {
    alice = new Player("Alice", new Position(0, 4));
    bob = new Player("Bob", new Position(8, 4));
    board = new Board(Arrays.asList(alice, bob));
  }

  @Test
  public void testInitialPlacement() {
    assertEquals(alice, board.getPlayerAt(alice.getPlayerPosition()));
    assertEquals(bob, board.getPlayerAt(bob.getPlayerPosition()));
  }

  @Test
  public void testValidMove() {
    Position newPos = new Position(1, 4);
    assertTrue(board.isMoveAllowed(alice.getPlayerPosition(), newPos));
    board.movePlayer(alice, newPos);
    assertEquals(newPos, alice.getPlayerPosition());
    assertEquals(alice, board.getPlayerAt(newPos));
    assertNull(board.getPlayerAt(new Position(0, 4)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidMoveOccupied() {
    // Bob est déjà à (8,4), Alice essaye d'y aller
    board.movePlayer(alice, bob.getPlayerPosition());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidMoveOutOfBounds() {
    Position invalid = new Position(-1, 0);
    board.movePlayer(alice, invalid);
  }

  @Test
  public void testPlaceWall() {
    Wall wall = new Wall(new Position(4, 4), Wall.OrientationWall.HORIZONTAL);
    board.placeWall(wall);
    assertEquals(1, board.getWalls().size());
    assertTrue(board.getWalls().contains(wall));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPlaceConflictingWall() {
    Wall wall1 = new Wall(new Position(4, 4), Wall.OrientationWall.HORIZONTAL);
    Wall wall2 = new Wall(new Position(4, 4), Wall.OrientationWall.HORIZONTAL); // même position
    board.placeWall(wall1);
    board.placeWall(wall2); // doit lancer exception
  }

  /*
  @Test
  public void testHasPlayerWon() {
    // Déplacer Alice jusqu'à la ligne de victoire
    Position winningRow = new Position(0, Board.SIZE - 1);
    board.movePlayer(alice, winningRow);
    assertTrue(board.hasPlayerWon(alice));
    assertFalse(board.hasPlayerWon(bob));
  }

   */

  @Test
  public void testToStringContainsPlayers() {
    String boardStr = board.toString();
    assertTrue(boardStr.contains("A")); // Alice
    assertTrue(boardStr.contains("B")); // Bob
  }
}
