package fr.bordeaux.core;

import static org.junit.Assert.*;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;
import org.junit.*;

public class GameStateTest {

  private GameState game;
  private Board board;
  private Player human;
  private Player ia;

  @Before
  public void setup() {
    board = new Board(9);
    human = new Player("Human", new Position(0, 4), TypePlayer.HUMAN);
    ia = new Player("IA", new Position(8, 4), TypePlayer.IA);

    List<Player> players = new ArrayList<>();
    players.add(human);
    players.add(ia);

    game = new GameState(board, players);
  }

  @Test
  public void constructor_ok_2_players() {
    assertNotNull(game);
    assertEquals(human, game.getCurrentPlayer());
  }

  @Test(expected = IllegalArgumentException.class)
  public void constructor_throws_if_bad_player_count() {
    List<Player> players = List.of(human);
    new GameState(board, players);
  }

  @Test
  public void listener_is_called_on_applyMove() {
    final boolean[] called = {false};

    game.addListener(() -> called[0] = true);

    Position pos = game.getCurrentPlayer().getPosition();
    Move move = Move.pawn(pos, new Position(1, 4));
    game.applyMove(move);

    assertTrue(called[0]);
  }

  @Test
  public void applyMove_pawn_changes_position_and_switch_player() {
    Position newPos = new Position(1, 4);
    Position pos = game.getCurrentPlayer().getPosition();

    Move move = Move.pawn(pos, newPos);
    game.applyMove(move);

    assertEquals(newPos, human.getPosition());
    assertEquals(ia, game.getCurrentPlayer());
  }

  @Test
  public void applyMove_wall_places_wall_and_switch_player() {
    Move move = Move.wall(new Position(4, 4), Orientation.HORIZONTAL);

    game.applyMove(move);

    assertEquals(ia, game.getCurrentPlayer());
  }

  @Test
  public void applyMove_does_nothing_if_paused() {
    game.setPaused(true);

    Position old = human.getPosition();
    Position pos = game.getCurrentPlayer().getPosition();

    game.applyMove(Move.pawn(pos, new Position(1, 4)));

    assertEquals(old, human.getPosition());
  }

  @Test
  public void undo_reverts_move() {
    Position p1 = new Position(1, 4);
    Player player = game.getCurrentPlayer();
    game.applyMove(Move.pawn(player.getPosition(), p1));

    game.undo();

    assertEquals(new Position(0, 4), human.getPosition());
    assertEquals(human, game.getCurrentPlayer());
  }

  @Test
  public void redo_reapplies_move() {
    Position p1 = new Position(1, 4);
    Position pos = game.getCurrentPlayer().getPosition();

    Move move = Move.pawn(pos, p1);

    // System.out.println("1 " + human.getPosition());
    game.applyMove(move);
    // System.out.println("2 " + human.getPosition());
    game.undo();
    // System.out.println("3 " + human.getPosition());
    game.redo();
    // System.out.println("4 " + human.getPosition());

    assertEquals(p1, human.getPosition());
    assertEquals(ia, game.getCurrentPlayer());
  }

  @Test
  public void undo_does_nothing_if_stack_empty() {
    game.undo();
    assertEquals(human, game.getCurrentPlayer());
  }

  @Test
  public void redo_does_nothing_if_stack_empty() {
    game.redo();
    assertEquals(human, game.getCurrentPlayer());
  }

  @Test
  public void victory_is_detected() {
    human.setPosition(new Position(8, 4));
    Position pos = game.getCurrentPlayer().getPosition();
    // System.out.println("1 " + human.getPosition());
    game.applyMove(Move.pawn(pos, new Position(8, 4)));
    // System.out.println("2 " + human.getPosition());

    assertFalse(game.isGameOver());
  }

  @Test
  public void win_condition_from_top_row() {
    Predicate<Integer> predicate = game.getWinCondition(human);
    int id = board.id(8, 4);
    assertTrue(predicate.test(id));
  }

  @Test
  public void win_condition_from_bottom_row() {
    Player p = new Player("P", new Position(8, 4), TypePlayer.HUMAN);
    GameState g = new GameState(board, List.of(p, ia));
    Predicate<Integer> predicate = g.getWinCondition(p);
    int id = board.id(0, 4);
    assertTrue(predicate.test(id));
  }

  @Test
  public void win_condition_from_left_col() {
    Player p = new Player("P", new Position(4, 0), TypePlayer.HUMAN);
    GameState g = new GameState(board, List.of(p, ia));
    Predicate<Integer> predicate = g.getWinCondition(p);
    int id = board.id(4, 8);
    assertTrue(predicate.test(id));
  }

  @Test
  public void win_condition_from_right_col() {
    Player p = new Player("P", new Position(4, 8), TypePlayer.HUMAN);
    GameState g = new GameState(board, List.of(p, ia));
    Predicate<Integer> predicate = g.getWinCondition(p);
    int id = board.id(4, 0);
    assertTrue(predicate.test(id));
  }

  // =========================
  // SAVE / LOAD JSON
  // =========================

  @Test
  public void save_and_load_json_works() {
    String filename = "test_save.json";
    game.saveGameJSON(filename);

    GameState loaded = game.loadGameJSON(filename);

    assertNotNull(loaded);
    assertEquals(game.getPlayers().size(), loaded.getPlayers().size());

    new File(filename).delete();
  }

  // =========================
  // RESET
  // =========================

  @Test
  public void reset_creates_new_game() {
    GameState newGame = game.reset();

    assertNotSame(game, newGame);
    assertEquals(2, newGame.getPlayers().size());
    assertEquals(9, newGame.getBoard().getSize());
  }
}
