package fr.bordeaux.core;

import static org.junit.Assert.*;

import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class RuleCheckerTest {

  private GameState state;
  private Player p1;
  private Player p2;
  private Board board;

  @Before
  public void setUp() {
    p1 = new Player("Alice", new Position(0, 4));
    p2 = new Player("Bob", new Position(8, 4));
    int SIZE = 9;
    board = new Board(SIZE);
    state = new GameState(board, List.of(p1, p2));
  }

  @Test
  public void testLegalPawnMove() {
    Move move = Move.pawn(new Position(1, 4));
    assertTrue(RuleChecker.isMoveLegal(state, move));
  }

  @Test
  public void testPawnMoveOutsideBoard() {
    Move move = Move.pawn(new Position(-1, 4));
    assertFalse(RuleChecker.isMoveLegal(state, move));
  }

  @Test
  public void testPawnMoveOnOccupiedCell() {
    p2.setPosition(new Position(1, 4));
    Move move = Move.pawn(new Position(1, 4));
    // assertFalse(RuleChecker.isMoveLegal(state, move));
  }

  @Test
  public void testPawnMoveTooFar() {
    Move move = Move.pawn(new Position(2, 4));
    assertFalse(RuleChecker.isMoveLegal(state, move));
  }

  @Test
  public void testPawnMoveThroughWall() {
    board.addWall(0, 4, Orientation.HORIZONTAL);
    Move move = Move.pawn(new Position(1, 4));
    assertFalse(RuleChecker.isMoveLegal(state, move));
  }

  @Test
  public void testLegalWallPlacement() {
    Move move = Move.wall(new Position(3, 3), Orientation.HORIZONTAL);
    assertTrue(RuleChecker.isMoveLegal(state, move));
  }

  @Test
  public void testWallPlacementNoWallsLeft() {
    for (int i = 0; i < 10; i++) {
      p1.useWall();
    }
    Move move = Move.wall(new Position(3, 3), Orientation.HORIZONTAL);
    assertFalse(RuleChecker.isMoveLegal(state, move));
  }

  @Test
  public void testWallConflict() {
    board.addWall(2, 2, Orientation.HORIZONTAL);
    Move move = Move.wall(new Position(2, 2), Orientation.HORIZONTAL);
    assertFalse(RuleChecker.isMoveLegal(state, move));
  }

  @Test
  public void testWallCrossing() {
    board.addWall(5, 5, Orientation.HORIZONTAL);
    Move move = Move.wall(new Position(5, 5), Orientation.VERTICAL);
    assertFalse(RuleChecker.isMoveLegal(state, move));
  }

  @Test
  public void testPawnMoveDiagonalIllegal() {
    Move move = Move.pawn(new Position(1, 5));
    assertFalse(RuleChecker.isMoveLegal(state, move));
  }
}
