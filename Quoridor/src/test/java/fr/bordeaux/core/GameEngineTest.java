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
  private final int SIZE = 9;

    @Before
  public void setUp() {
    p1 = new Player("Alice", new Position(0, 4));
    p2 = new Player("Bob", new Position(8, 4));
    board = new Board(SIZE);
    state = new GameState(board, List.of(p1, p2));
    engine = new GameEngine(state);
  }

  @Test
  public void testLegalPawnMoveUpdatesPosition() {
    Position target = new Position(1, 4);
    Move move = Move.pawn(target);
    engine.play(move);

    assertEquals(target, p1.getPosition());
    assertEquals(p2, state.currentPlayer());
  }

  @Test
  public void testIllegalPawnMoveReturnsFalse() {
    Move move = Move.pawn(new Position(3, 4));
    assertFalse(engine.play(move));
  }

  @Test
  public void testLegalWallMoveUpdatesState() {
    Position wallPos = new Position(2, 2);
    Move move = Move.wall(wallPos, Orientation.HORIZONTAL);
    int remainingBefore = p1.getRemainingWalls();

    engine.play(move);

    assertEquals(remainingBefore - 1, p1.getRemainingWalls());
    assertEquals(p2, state.currentPlayer());
    assertTrue(board.isWallCollision(wallPos, Orientation.HORIZONTAL));
  }

  @Test
  public void testNoMoreWallsReturnsFalse() {
    for (int i = 0; i < 10; i++) p1.useWall();
    Move move = Move.wall(new Position(3, 3), Orientation.HORIZONTAL);
    assertFalse(engine.play(move));
  }

  @Test
  public void testWallOnWallReturnsFalse() {
    Position pos = new Position(4, 4);
    engine.play(Move.wall(pos, Orientation.HORIZONTAL));
    state.nextPlayer();
    assertFalse(engine.play(Move.wall(pos, Orientation.HORIZONTAL)));
  }

  @Test
  public void testPlayReturnsFalseWhenGameOver() {
    p1.setPosition(new Position(SIZE - 1, 4));
    assertTrue(state.isGameOver());

    Move move = Move.pawn(new Position(SIZE - 2, 4));
    assertFalse(engine.play(move));
  }

  @Test
  public void testWinningMove() {
    p1.setPosition(new Position(SIZE - 2, 5));
    Move winningMove = Move.pawn(new Position(SIZE - 1, 5));

    assertTrue(engine.play(winningMove));
    assertTrue(state.hasReachedGoal(p1));
  }

  @Test
  public void testGetterState() {
      assertEquals(state, engine.getState());
  }
}
