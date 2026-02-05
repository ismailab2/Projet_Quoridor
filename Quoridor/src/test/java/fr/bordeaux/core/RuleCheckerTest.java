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
    p1 = new Player("Alice", new Position(4, 0));
    p2 = new Player("Bob", new Position(4, 8));

    board = new Board();
    state = new GameState(board, List.of(p1, p2));
  }

  @Test
  public void testLegalPawnMove() {
    Move move = Move.pawn(new Position(4, 1));
    boolean legal = RuleChecker.isMoveLegal(state, move);
    assertTrue(legal);
  }

  @Test
  public void testPawnMoveOutsideBoard() {
    Move move = Move.pawn(new Position(4, -1));
    assertFalse(RuleChecker.isMoveLegal(state, move));
  }

  @Test
  public void testPawnMoveOnOccupiedCell() {
    Move move = Move.pawn(new Position(4, 8));
    assertFalse(RuleChecker.isMoveLegal(state, move));
  }

  @Test
  public void testPawnMoveTooFar() {
    Move move = Move.pawn(new Position(4, 3));
    assertFalse(RuleChecker.isMoveLegal(state, move));
  }

  @Test
  public void testLegalWallPlacement() {
    Wall wall = new Wall(new Position(3, 3), Wall.Orientation.HORIZONTAL);
    Move move = Move.wall(wall);

    assertTrue(RuleChecker.isMoveLegal(state, move));
  }

  @Test
  public void testWallPlacementNoWallsLeft() {
    for (int i = 0; i < 10; i++) {
      p1.useWall();
    }
    Wall wall = new Wall(new Position(3, 3), Wall.Orientation.HORIZONTAL);
    Move move = Move.wall(wall);
    assertFalse(RuleChecker.isMoveLegal(state, move));
  }

  @Test
  public void testWallConflict() {
    Wall existing = new Wall(new Position(2, 2), Wall.Orientation.HORIZONTAL);
    board.addWall(existing);
    Wall same = new Wall(new Position(2, 2), Wall.Orientation.HORIZONTAL);
    Move move = Move.wall(same);
    assertFalse(RuleChecker.isMoveLegal(state, move));
  }

  @Test
  public void testWallCrossing() {
    Wall horizontal = new Wall(new Position(5, 5), Wall.Orientation.HORIZONTAL);
    board.addWall(horizontal);
    Wall vertical = new Wall(new Position(5, 5), Wall.Orientation.VERTICAL);
    Move move = Move.wall(vertical);
    assertFalse(RuleChecker.isMoveLegal(state, move));
  }

  @Test
  public void testPawnMoveLegalBranches() {
    board.getCell(p1.getPosition()).setPlayer(p1);
    board.getCell(p2.getPosition()).setPlayer(p2);
    GameState state = new GameState(board, List.of(p1, p2));

    assertFalse(RuleChecker.isMoveLegal(state, Move.pawn(new Position(4, -1))));
    assertFalse(RuleChecker.isMoveLegal(state, Move.pawn(new Position(4, 8))));
    assertTrue(RuleChecker.isMoveLegal(state, Move.pawn(new Position(4, 1))));
  }

  @Test
  public void testWallMoveLegalBranches() {

    GameState state = new GameState(board, List.of(p1, p2));
    Wall existing = new Wall(new Position(2, 2), Wall.Orientation.HORIZONTAL);
    board.addWall(existing);
    for (int i = 0; i < 10; i++) p1.useWall();
    assertFalse(
        RuleChecker.isMoveLegal(
            state, Move.wall(new Wall(new Position(3, 3), Wall.Orientation.HORIZONTAL))));
    p1 = new Player("Alice", new Position(4, 0));
    state = new GameState(board, List.of(p1, p2));
    Wall conflict = new Wall(new Position(2, 2), Wall.Orientation.HORIZONTAL);
    assertFalse(RuleChecker.isMoveLegal(state, Move.wall(conflict)));
    Wall cross = new Wall(new Position(2, 2), Wall.Orientation.VERTICAL);
    assertFalse(RuleChecker.isMoveLegal(state, Move.wall(cross)));
    Wall legal = new Wall(new Position(3, 3), Wall.Orientation.HORIZONTAL);
    assertTrue(RuleChecker.isMoveLegal(state, Move.wall(legal)));
  }
}
