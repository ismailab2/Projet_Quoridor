package fr.bordeaux.core;

import static org.junit.Assert.*;

import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class GameEngineTest {

  private Player p1;
  private Player p2;
  private Board board;
  private GameState state;
  private GameEngine engine;

  @Before
  public void setUp() {
    p1 = new Player("Alice", new Position(4, 0));
    p2 = new Player("Bob", new Position(4, 8));
    board = new Board();
    state = new GameState(board, List.of(p1, p2));
    engine = new GameEngine(state);
  }

  @Test
  public void testLegalPawnMoveUpdatesBoard() {
    Move move = Move.pawn(new Position(4, 1));
    engine.play(move);
    assertEquals(p1, board.getCell(new Position(4, 1)).getPlayer());
    assertNull(board.getCell(new Position(4, 0)).getPlayer());
    assertEquals(p2, state.currentPlayer());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIllegalPawnMoveThrowsException() {
    Move move = Move.pawn(new Position(4, 3)); // trop loin
    engine.play(move);
  }

  @Test
  public void testLegalWallMoveUpdatesBoard() {
    Wall wall = new Wall(new Position(3, 3), Wall.Orientation.HORIZONTAL);
    Move move = Move.wall(wall);
    int remainingBefore = p1.getRemainingWalls();
    engine.play(move);
    assertTrue(board.getWalls().contains(wall));
    assertEquals(remainingBefore - 1, p1.getRemainingWalls());
    assertEquals(p2, state.currentPlayer());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIllegalWallMoveThrowsException() {
    for (int i = 0; i < 10; i++) p1.useWall();
    Wall wall = new Wall(new Position(3, 3), Wall.Orientation.HORIZONTAL);
    Move move = Move.wall(wall);
    engine.play(move);
  }
}
