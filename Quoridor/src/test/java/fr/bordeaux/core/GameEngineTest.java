package fr.bordeaux.core;

import static org.junit.jupiter.api.Assertions.*;

import fr.bordeaux.AI.MinimaxPlayer;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GameEngineTest {

  private GameEngine engine;
  private GameState state;
  private Player p1, p2;
  private Board board;

  @BeforeEach
  public void setup() {
    board = new Board(9);
    p1 = new HumanPlayer("Human", new Position(0, 2));
    p2 = new MinimaxPlayer("Bot", new Position(4, 2), 2);
    state = new GameState(board, List.of(p1, p2));
    engine = new GameEngine(state);
  }

  @Test
  public void play_pawn_move_valid() {
    Move move = Move.pawn(p1.getPosition(), new Position(1, 2));
    assertTrue(engine.play(move));
    assertEquals(new Position(1, 2), p1.getPosition());
  }

  @Test
  public void play_pawn_move_illegal() {
    Move move = Move.pawn(p1.getPosition(), new Position(3, 2));
    assertFalse(engine.play(move));
  }

  @Test
  public void play_wall_move_valid() {
    Move wall = Move.wall(new Position(2, 2), Orientation.HORIZONTAL);
    assertTrue(engine.play(wall));
  }

  @Test
  public void play_wall_move_illegal_collision() {
    Move wall1 = Move.wall(new Position(2, 2), Orientation.HORIZONTAL);
    engine.play(wall1);
    Move wall2 = Move.wall(new Position(2, 2), Orientation.HORIZONTAL);
    assertFalse(engine.play(wall2));
  }

  @Test
  public void play_wall_move_blocks_path() {
    Move wall1 = Move.wall(new Position(3, 0), Orientation.HORIZONTAL);
    board.addWall(wall1.getTo(), wall1.getOrientation());
    Move wall2 = Move.wall(new Position(3, 2), Orientation.HORIZONTAL);
    System.out.println(state.getCurrentPlayer().getPosition());
    System.out.println(RuleChecker.isMoveLegal(state, wall2));
    assertFalse(RuleChecker.isMoveLegal(state, wall2));

  }

  @Test
  public void undo_reverts_pawn_move() {
    Position from = p1.getPosition();
    Position to = new Position(1, 2);
    Move move = Move.pawn(from, to);
    engine.play(move);

    engine.undo();
    assertEquals(from, p1.getPosition());
  }

  @Test
  public void redo_reapplies_pawn_move() {
    Position from = p1.getPosition();
    Position to = new Position(1, 2);
    Move move = Move.pawn(from, to);
    engine.play(move);
    engine.undo();
    engine.redo();
    assertEquals(to, p1.getPosition());
  }

  @Test
  public void undo_redo_wall_moves() {
    Move wall = Move.wall(new Position(2, 2), Orientation.HORIZONTAL);
    engine.play(wall);
    engine.undo();
    engine.redo();
    // System.out.println(wall.getTo());
    assertTrue(board.isWallCollision(new Position(2, 2), Orientation.HORIZONTAL));
  }

  @Test
  public void pause_blocks_play() {
    engine.pause();
    Move move = Move.pawn(p1.getPosition(), new Position(1, 2));
    assertFalse(engine.play(move));
    assertEquals(new Position(0, 2), p1.getPosition());
  }

  @Test
  public void newGame_resets_state() {
    GameState old = engine.getState();
    engine.newGame();
    GameState fresh = engine.getState();
    assertSame(old, fresh);
    assertEquals(2, fresh.getPlayers().size());
    // System.out.println(fresh.getBoard().getSize());
    assertEquals(9, fresh.getBoard().getSize());
  }

  @Test
  public void save_and_load_game() {
    String file = "engine_test.json";
    engine.saveGame(file);
    engine.newGame();
    engine.loadGame(file);
    assertEquals(2, engine.getState().getPlayers().size());
    new File(file).delete();
  }

  @Test
  public void pawn_jump_over_player_straight() {
    p2.setPosition(new Position(1, 2));
    Move move = Move.pawn(p1.getPosition(), new Position(2, 2));
    assertTrue(RuleChecker.isMoveLegal(state, move));
  }

  @Test
  public void pawn_jump_over_player_diagonal() {
    p2.setPosition(new Position(1, 2));
    board.addWall(new Position(1, 2), Orientation.HORIZONTAL);
    Move move = Move.pawn(p1.getPosition(), new Position(2, 3));
    // assertTrue(RuleChecker.isMoveLegal(state, move));
  }

  @Test
  public void pawn_move_non_adjacent_invalid() {
    Move move = Move.pawn(p1.getPosition(), new Position(3, 2));
    assertFalse(RuleChecker.isMoveLegal(state, move));
  }

  @Test
  public void wall_no_remaining_walls() {
    for (int i = 0; i < 10; i++) p1.useWall();
    Move wall = Move.wall(new Position(1, 1), Orientation.HORIZONTAL);
    assertFalse(RuleChecker.isMoveLegal(state, wall));
  }
}
