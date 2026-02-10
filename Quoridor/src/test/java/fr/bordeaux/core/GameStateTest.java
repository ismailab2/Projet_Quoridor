package fr.bordeaux.core;

import static org.junit.Assert.*;

import java.util.List;
import java.util.function.Predicate;
import org.junit.Before;
import org.junit.Test;

public class GameStateTest {

  private Board board;
  private Player p1, p2, p3, p4;
  private GameState gameState, gameState4;
  private final int SIZE = 9;

  @Before
  public void setUp() {
    board = new Board(SIZE);
    p1 = new Player("Alice", new Position(0, 4));
    p2 = new Player("Bob", new Position(8, 4));
    p3 = new Player("Charlie", new Position(4, 0));
    p4 = new Player("David", new Position(4, 8));
    gameState = new GameState(board, List.of(p1, p2));
    gameState4 = new GameState(board, List.of(p1, p2, p3, p4));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPlayersNumberInvalidThree() {
    Board board = new Board(9);
    new GameState(board, List.of(p1, p2, p3));
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
    gameState.nextPlayer();
    gameState.nextPlayer();
    assertEquals(p1, gameState.currentPlayer());
  }

  @Test
  public void testApplyPawnMoveUpdatesPlayerPosition() {
    Position target = new Position(1, 4);
    Move move = Move.pawn(target);
    gameState.applyMove(move);
    assertEquals(target, p1.getPosition());
  }

  @Test
  public void testApplyWallMoveUpdatesBoardAndPlayer() {
    int initialWalls = p1.getRemainingWalls();
    Position wallPos = new Position(2, 2);
    Move move = Move.wall(wallPos, Orientation.HORIZONTAL);
    gameState.applyMove(move);
    assertEquals(initialWalls - 1, p1.getRemainingWalls());
    assertTrue(board.isWallCollision(wallPos, Orientation.HORIZONTAL));
  }

  @Test
  public void testIsGameOverWhenPlayerReachesGoal() {
    assertFalse(gameState.isGameOver());
    p1.setPosition(new Position(SIZE - 1, 4));
    assertTrue(gameState.isGameOver());
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

  @Test
  public void testHasReachedGoalPlayer1() {
    p1.setPosition(new Position(SIZE - 1, 4));
    assertTrue(gameState4.hasReachedGoal(p1));
    p1.setPosition(new Position(SIZE - 2, 4));
    assertFalse(gameState4.hasReachedGoal(p1));
  }

  @Test
  public void testHasReachedGoalPlayer2() {
    p2.setPosition(new Position(0, 4));
    assertTrue(gameState4.hasReachedGoal(p2));
    p2.setPosition(new Position(1, 4));
    assertFalse(gameState4.hasReachedGoal(p2));
  }

  @Test
  public void testHasReachedGoalPlayer3() {
    p3.setPosition(new Position(4, SIZE - 1));
    assertTrue(gameState4.hasReachedGoal(p3));
    p3.setPosition(new Position(4, SIZE - 2));
    assertFalse(gameState4.hasReachedGoal(p3));
  }

  @Test
  public void testHasReachedGoalPlayer4() {
    p4.setPosition(new Position(4, 0));
    assertTrue(gameState4.hasReachedGoal(p4));
    p4.setPosition(new Position(4, 1));
    assertFalse(gameState4.hasReachedGoal(p4));
  }

  @Test
  public void testIsGameOver4Players() {
    assertFalse(gameState4.isGameOver());
    p3.setPosition(new Position(4, SIZE - 1));
    assertTrue(gameState4.isGameOver());
  }

  @Test
  public void testGetWinConditionTopPlayer() {
    // Starting first row (0), win last row (8)
    Player alice = new Player("Alice", new Position(0, 4));
    Predicate<Integer> win = gameState.getWinCondition(alice);

    assertTrue(win.test(board.id(8, 0)));
    assertTrue(win.test(board.id(8, 4)));
    assertTrue(win.test(board.id(8, 8)));

    assertFalse(win.test(board.id(0, 4)));
    assertFalse(win.test(board.id(7, 4)));
  }

  @Test
  public void testGetWinConditionBottomPlayer() {
    // Starting last row (8), win first row (0)
    Player bob = new Player("Bob", new Position(8, 4));
    Predicate<Integer> win = gameState.getWinCondition(bob);

    assertTrue(win.test(board.id(0, 0)));
    assertTrue(win.test(board.id(0, 8)));

    assertFalse(win.test(board.id(8, 4)));
    assertFalse(win.test(board.id(1, 4)));
  }

  @Test
  public void testGetWinConditionLeftPlayer() {
    // Starting first column (0), win right column (8)
    Player charlie = new Player("Charlie", new Position(4, 0));
    Predicate<Integer> win = gameState.getWinCondition(charlie);

    assertTrue(win.test(board.id(0, 8)));
    assertTrue(win.test(board.id(8, 8)));

    assertFalse(win.test(board.id(4, 0)));
    assertFalse(win.test(board.id(4, 7)));
  }

  @Test
  public void testGetWinConditionRightPlayer() {
    // Starting last column (8), win first column (0)
    Player david = new Player("David", new Position(4, 8));
    Predicate<Integer> win = gameState.getWinCondition(david);

    assertTrue(win.test(board.id(0, 0)));
    assertTrue(win.test(board.id(8, 0)));

    assertFalse(win.test(board.id(4, 8)));
    assertFalse(win.test(board.id(4, 1)));
  }
}
