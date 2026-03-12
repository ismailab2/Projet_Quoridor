package fr.bordeaux.AI;

import static org.junit.jupiter.api.Assertions.*;

import fr.bordeaux.core.Board;
import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Move;
import fr.bordeaux.core.Move.Type;
import fr.bordeaux.core.Orientation;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class MinimaxPlayerTest {

  private GameState game;
  private Board board;
  private Player ia1;
  private Player ia2;

  @Test
  public void checkParameters() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          ia1 = MinimaxPlayer.builder("IA1", new Position(8, 4)).withDepth(-12).build();
        });
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          ia1 = MinimaxPlayer.builder("IA1", new Position(8, 4)).withReflexion(0).build();
        });
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          ia1 = MinimaxPlayer.builder("IA1", new Position(8, 4)).withHeuristic("bonjour").build();
        });
  }

  @Test
  public void checkMiniMaxMoveWall() {
    board = new Board(9);
    ia1 =
        MinimaxPlayer.builder("IA1", new Position(8, 4))
            .withDepth(2)
            .withReflexion(5)
            .withHeuristic("h3")
            .build();
    ia2 = MinimaxPlayer.builder("IA2", new Position(0, 4)).build();
    ia1.setPosition(new Position(2, 4));
    ia2.setPosition(new Position(7, 2));

    List<Player> players = new ArrayList<>();
    players.add(ia1);
    players.add(ia2);
    game = new GameState(board, players);

    Move res = ia1.getNextMove(game);

    assertTrue(
        res.getTo().equals(new Position(7, 1))
            && res.getType() == Type.WALL
            && res.getOrientation() == Orientation.HORIZONTAL);
  }

  @Test
  public void checkMiniMaxMovePawn() {
    board = new Board(3);
    ia1 = MinimaxPlayer.builder("IA1", new Position(2, 1)).withDepth(2).build();
    ia2 = new RandomPlayer("IA2", new Position(0, 1));
    ia1.setPosition(new Position(1, 1));
    ia2.setPosition(new Position(0, 2));

    List<Player> players = new ArrayList<>();
    players.add(ia1);
    players.add(ia2);
    game = new GameState(board, players);

    Move res = ia1.getNextMove(game);
    assertTrue(
        res.getTo().equals(new Position(0, 1))
            && res.getType() == Type.PAWN
            && res.getOrientation() == null
            && res.getFrom().equals(new Position(1, 1)));
  }

  @Test
  public void checkTimer() {
    board = new Board(9);
    ia1 = MinimaxPlayer.builder("IA1", new Position(8, 4)).withDepth(10).build();
    ia2 = new RandomPlayer("IA2", new Position(0, 4));
    List<Player> players = new ArrayList<>();
    players.add(ia1);
    players.add(ia2);
    game = new GameState(board, players);

    long startTime = System.currentTimeMillis();
    Move move = ia1.getNextMove(game);
    long duration = System.currentTimeMillis() - startTime;

    assert (duration >= 5000);
    assertNotNull(move);
  }

  @Test
  public void checkIterativeDeepening() {
    board = new Board(9);
    ia1 = MinimaxPlayer.builder("IA1", new Position(8, 4)).withDepth(2).withHeuristic("h2").build();
    ia2 = new RandomPlayer("IA2", new Position(0, 4));
    List<Player> players = new ArrayList<>();
    players.add(ia1);
    players.add(ia2);
    game = new GameState(board, players);

    Move move1 = ia1.getNextMove(game);

    ia1 =
        MinimaxPlayer.builder("IA1", new Position(8, 4))
            .isIterativeDeepening(true)
            .withHeuristic("h2")
            .build();
    List<Player> new_players = new ArrayList<>();
    new_players.add(ia1);
    new_players.add(ia2);
    game = new GameState(board, new_players);

    Move move2 = ia1.getNextMove(game);

    assertTrue(move1.equals(move2));
  }

  @Test
  public void checkToString() {
    ia1 = MinimaxPlayer.builder("IA1", new Position(8, 4)).build();
    assertEquals(
        ia1.toString(),
        "MiniMax AI "
            + String.format(
                "Player{name='%s', pos=%s, walls=%d, score=%d}",
                ia1.getName(), ia1.getPosition(), ia1.getRemainingWalls(), ia1.getScore()));
  }
}
