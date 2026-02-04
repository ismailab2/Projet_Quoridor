package fr.bordeaux.core;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class TestMove {

  @Test
  public void testPawnMoveCreation() {
    Player player = new Player("Alice", new Position(0, 4));
    Position from = new Position(0, 4);
    Position to = new Position(1, 4);
    List<Player> players = new ArrayList<Player>();

    Board board = new Board(players);

    Move move = new Move(player, board, from, to);

    assertFalse("Should not be a wall placement", move.isWallPlacement());
    assertEquals(Move.MoveType.PAWN_MOVE, move.getType());
    assertEquals(player, move.getPlayer());
    assertEquals(from, move.getFrom());
    assertEquals(to, move.getTo());
    assertNull(move.getWall());
    assertTrue(move.toString().contains("Pawn Move"));
  }

  @Test
  public void testWallMoveCreation() {
    Player player = new Player("Bob", new Position(8, 4));
    Wall wall = new Wall(new Position(2, 2), Wall.OrientationWall.HORIZONTAL);
    List<Player> players = new ArrayList<Player>();
    players.add(player);
    Board board = new Board(players);

    Move move = new Move(player, board, wall);

    assertTrue("Should be a wall placement", move.isWallPlacement());
    assertEquals(Move.MoveType.WALL_PLACEMENT, move.getType());
    assertEquals(player, move.getPlayer());
    assertEquals(wall, move.getWall());
    assertNull(move.getFrom());
    assertNull(move.getTo());
    assertTrue(move.toString().contains("Wall Move"));
  }

  @Test
  public void testEqualsAndHashCode() {
    Player player1 = new Player("Alice", new Position(0, 4));
    Position from = new Position(0, 4);
    Position to = new Position(1, 4);
    List<Player> players = new ArrayList<Player>();
    players.add(player1);
    Board board = new Board(players);

    Move move1 = new Move(player1, board, from, to);
    Move move2 = new Move(player1, board, from, to);

    assertEquals("Moves should be equal", move1, move2);
    assertEquals("Hashcodes should match", move1.hashCode(), move2.hashCode());

    Move move3 = new Move(player1, board, new Position(1, 4), new Position(2, 4));
    assertNotEquals("Moves should not be equal", move1, move3);
  }

  @Test
  public void testPawnAndWallNotEqual() {
    Player player = new Player("Alice", new Position(0, 4));
    Position from = new Position(0, 4);
    Position to = new Position(1, 4);
    Wall wall = new Wall(new Position(2, 2), Wall.OrientationWall.HORIZONTAL);
    List<Player> players = new ArrayList<Player>();
    players.add(player);
    Board board = new Board(players);

    Move pawnMove = new Move(player, board, from, to);
    Move wallMove = new Move(player, board, wall);

    assertNotEquals("Pawn move and wall move should not be equal", pawnMove, wallMove);
  }

  // TESTS POUR LES DÉPLACEMENTS INTERDITS

  @Test(expected = IllegalArgumentException.class)
  public void testPawnMoveDiagonalThrows() {
    Player player = new Player("Alice", new Position(0, 0));
    Position from = new Position(0, 0);
    Position to = new Position(1, 1); // diagonale interdite
    List<Player> players = new ArrayList<Player>();
    players.add(player);
    Board board = new Board(players);

    new Move(player, board, from, to);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPawnMoveTooFarThrows() {
    Player player = new Player("Alice", new Position(0, 0));
    Position from = new Position(0, 0);
    Position to = new Position(0, 2); // plus d'une case
    List<Player> players = new ArrayList<Player>();

    players.add(player);
    Board board = new Board(players);

    new Move(player, board, from, to);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPawnMoveZeroDistanceThrows() {
    Player player = new Player("Alice", new Position(0, 0));
    Position from = new Position(0, 0);
    Position to = new Position(0, 0); // pas de mouvement

    List<Player> players = new ArrayList<Player>();

    players.add(player);
    Board board = new Board(players);
    new Move(player, board, from, to);
  }

  @Test
  public void testPawnMoveValidDirections() {
    Player player = new Player("Alice", new Position(4, 4));
    Position from = new Position(4, 4);
    List<Player> players = new ArrayList<Player>();
    players.add(player);
    Board board = new Board(players);

    // haut
    Move moveUp = new Move(player, board, from, new Position(4, 3));
    assertEquals(new Position(4, 3), moveUp.getTo());

    // bas
    Move moveDown = new Move(player, board, from, new Position(4, 5));
    assertEquals(new Position(4, 5), moveDown.getTo());

    // gauche
    Move moveLeft = new Move(player, board, from, new Position(3, 4));
    assertEquals(new Position(3, 4), moveLeft.getTo());

    // droite
    Move moveRight = new Move(player, board, from, new Position(5, 4));
    assertEquals(new Position(5, 4), moveRight.getTo());
  }
}
