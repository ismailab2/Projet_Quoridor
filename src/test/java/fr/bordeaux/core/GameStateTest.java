package fr.bordeaux.core;

import static org.junit.jupiter.api.Assertions.*;

import fr.bordeaux.ai.MinimaxPlayer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GameStateTest {

  private GameState game;
  private Board board;
  private Player human;
  private Player ia;

  @BeforeEach
  public void setup() {
    board = new Board(9);
    human = new HumanPlayer("Human", new Position(0, 4));
    ia = MinimaxPlayer.builder("IA", new Position(8, 4)).build();

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

  @Test
  public void testPlayersNumberInvalidThree() {
    Board board = new Board(9);
    Player human2 = new HumanPlayer("Human2", new Position(4, 4));
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          new GameState(board, List.of(human, human2, ia));
        });
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
    Player p = new HumanPlayer("P", new Position(8, 4));
    GameState g = new GameState(board, List.of(p, ia));
    Predicate<Integer> predicate = g.getWinCondition(p);
    int id = board.id(0, 4);
    assertTrue(predicate.test(id));
  }

  @Test
  public void win_condition_from_left_col() {
    Player p = new HumanPlayer("P", new Position(4, 0));
    GameState g = new GameState(board, List.of(p, ia));
    Predicate<Integer> predicate = g.getWinCondition(p);
    int id = board.id(4, 8);
    assertTrue(predicate.test(id));
  }

  @Test
  public void win_condition_from_right_col() {
    Player p = new HumanPlayer("P", new Position(4, 8));
    GameState g = new GameState(board, List.of(p, ia));
    Predicate<Integer> predicate = g.getWinCondition(p);
    int id = board.id(4, 0);
    assertTrue(predicate.test(id));
  }

  @Test
  public void save_and_load_json_works() {
    /*
    String filename = "test_save.json";
    game.saveGameJSON(filename);

    GameState loaded = game.loadGameJSON(filename);

    assertNotNull(loaded);
    assertEquals(game.getPlayers().size(), loaded.getPlayers().size());

    new File(filename).delete();

       */
  }

  @Test
  public void reset_creates_new_game() {
    GameState newGame = game.reset();

    assertNotSame(game, newGame);
    assertEquals(2, newGame.getPlayers().size());
    assertEquals(9, newGame.getBoard().getSize());
  }

  @Test
  public void checkGenerateLegalMoves() {
    board = new Board(3);
    human = new HumanPlayer("Human", new Position(0, 1));
    ia = MinimaxPlayer.builder("IA", new Position(2, 1)).build();
    ia.setPosition(new Position(1, 1));
    List<Player> players = new ArrayList<>();
    players.add(human);
    players.add(ia);

    game = new GameState(board, players);
    Position p = human.getPosition();
    List<Move> moves =
        new ArrayList<>(
            List.of(
                Move.pawn(p, new Position(0, 0)),
                Move.pawn(p, new Position(0, 2)),
                Move.pawn(p, new Position(2, 1))));
    for (int x = 0; x < board.getSize() - 1; x++) {
      for (int y = 0; y < board.getSize() - 1; y++) {
        Position pos_wall = new Position(x, y);

        moves.add(Move.wall(pos_wall, Orientation.HORIZONTAL));

        moves.add(Move.wall(pos_wall, Orientation.VERTICAL));
      }
    }
    assertTrue(moves.containsAll(game.generateLegalMoves()));

    game.applyMove(Move.wall(new Position(0, 0), Orientation.HORIZONTAL));
    p = game.getCurrentPlayer().getPosition();
    List<Move> moves2 =
        new ArrayList<>(
            List.of(
                Move.pawn(p, new Position(1, 0)),
                Move.pawn(p, new Position(1, 2)),
                Move.pawn(p, new Position(2, 1))));

    moves2.add(Move.wall(new Position(1, 0), Orientation.HORIZONTAL));
    moves2.add(Move.wall(new Position(1, 0), Orientation.VERTICAL));
    moves2.add(Move.wall(new Position(1, 1), Orientation.HORIZONTAL));

    assertTrue(moves2.containsAll(game.generateLegalMoves()));
  }
}
