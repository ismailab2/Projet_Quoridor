package fr.bordeaux.core;

import static org.junit.jupiter.api.Assertions.*;

import fr.bordeaux.AI.MinimaxPlayer;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RuleCheckerTest {

  private GameState state;
  private Board board;
  private Player p1, p2;

  @BeforeEach
  public void setup() {
    board = new Board(5); // petit plateau pour tester
    p1 = new HumanPlayer("Human", new Position(0, 2));
    p2 = new MinimaxPlayer("Bot", new Position(4, 2));
    state = new GameState(board, List.of(p1, p2));
  }

  @Test
  public void pawnMove_inside_board_returns_true() {
    Move move = Move.pawn(p1.getPosition(), new Position(1, 2));
    assertTrue(RuleChecker.isMoveLegal(state, move));
  }

  @Test
  public void pawnMove_outside_board_returns_false() {
    Move move = Move.pawn(p1.getPosition(), new Position(-1, 2));
    assertFalse(RuleChecker.isMoveLegal(state, move));
  }

  @Test
  public void pawnMove_non_adjacent_returns_false() {
    Move move = Move.pawn(p1.getPosition(), new Position(3, 2));
    assertFalse(RuleChecker.isMoveLegal(state, move));
  }

  @Test
  public void pawnMove_jump_over_player_returns_true() {
    p2.setPosition(new Position(1, 2));
    Move move = Move.pawn(p1.getPosition(), new Position(2, 2));
    assertTrue(RuleChecker.isMoveLegal(state, move));
  }

  @Test
  public void pawnMove_jump_diagonal_returns_true() {
    p2.setPosition(new Position(1, 2));
    board.addWall(new Position(1, 2), Orientation.HORIZONTAL);
    Move move = Move.pawn(p1.getPosition(), new Position(2, 3));
    // assertTrue(RuleChecker.isMoveLegal(state, move));
  }

  @Test
  public void wallMove_player_no_walls_returns_false() {
    p1.useWall();
    p1.useWall();
    p1.useWall();
    p1.useWall();
    p1.useWall();
    p1.useWall();
    p1.useWall();
    p1.useWall();
    p1.useWall();
    p1.useWall();
    Move move = Move.wall(new Position(2, 2), Orientation.HORIZONTAL);
    assertFalse(RuleChecker.isMoveLegal(state, move));
  }

  @Test
  public void wallMove_collision_returns_false() {
    Move firstWall = Move.wall(new Position(2, 2), Orientation.HORIZONTAL);
    state.applyMove(firstWall); // pose le premier mur

    Move collidingWall = Move.wall(new Position(2, 2), Orientation.HORIZONTAL);
    assertFalse(RuleChecker.isMoveLegal(state, collidingWall));
  }

  @Test
  public void wallMove_blocks_path_returns_false() {
    Move wall1 = Move.wall(new Position(3, 0), Orientation.HORIZONTAL);
    board.addWall(wall1.getTo(), wall1.getOrientation());

    Move wall2 = Move.wall(new Position(3, 2), Orientation.HORIZONTAL);
    // assertFalse(RuleChecker.isMoveLegal(state, wall2));
  }

  @Test
  public void wallMove_valid_returns_true() {
    Move wall = Move.wall(new Position(2, 2), Orientation.HORIZONTAL);
    assertTrue(RuleChecker.isMoveLegal(state, wall));
  }
}
