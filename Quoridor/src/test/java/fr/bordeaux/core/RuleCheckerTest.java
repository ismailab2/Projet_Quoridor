package fr.bordeaux.core;

import static org.junit.Assert.*;

import java.util.ArrayList;
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
    assertFalse(RuleChecker.isMoveLegal(state, move));
  }

  @Test
  public void testPawnMoveTooFar() {
    Move move = Move.pawn(new Position(2, 4));
    assertFalse(RuleChecker.isMoveLegal(state, move));
  }

  @Test
  public void testPawnMoveThroughWall() {
    Position position = new Position(0,4);
    board.addWall(position, Orientation.HORIZONTAL);
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
    Position position = new Position(2,2);
    board.addWall(position, Orientation.HORIZONTAL);
    Move move = Move.wall(new Position(2, 2), Orientation.HORIZONTAL);
    assertFalse(RuleChecker.isMoveLegal(state, move));
  }

  @Test
  public void testWallCrossing() {
    Position position = new Position(5,5);
    board.addWall(position, Orientation.HORIZONTAL);
    Move move = Move.wall(new Position(5, 5), Orientation.VERTICAL);
    assertFalse(RuleChecker.isMoveLegal(state, move));
  }

  @Test
  public void testPawnMoveDiagonalIllegal() {
    Move move = Move.pawn(new Position(1, 5));
    assertFalse(RuleChecker.isMoveLegal(state, move));
  }

  @Test
  public void testWallBlocksAllPathsReturnsFalse() {
    //Alice can only go through (0,8)
    board.addWall(new Position(0, 0), Orientation.HORIZONTAL);
    board.addWall(new Position(0, 2), Orientation.HORIZONTAL);
    board.addWall(new Position(0, 4), Orientation.HORIZONTAL);
    board.addWall(new Position(0, 6), Orientation.HORIZONTAL);

    //If we add this wall, Alice is blocked
    Move blockingMove = Move.wall(new Position(0, 7), Orientation.VERTICAL);

    assertFalse(RuleChecker.isMoveLegal(state, blockingMove));
  }
}
