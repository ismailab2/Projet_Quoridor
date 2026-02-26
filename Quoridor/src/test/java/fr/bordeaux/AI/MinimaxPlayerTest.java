package fr.bordeaux.AI;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.bordeaux.core.Board;
import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Position;
import fr.bordeaux.core.Player;


public class MinimaxPlayerTest {

  private GameState game;
  private Board board;
  private Player ia1;
  private Player ia2;

  @BeforeEach
  public void setup() {
    board = new Board(9);
  }

  @Test
  // Check if the number of players is equal to 2. We cannot use AI with more than 2 players.
  public void check_nb_player() {
    ia1 = new MinimaxPlayer("IA1", new Position(0, 4), 3, 5, false);
    ia2 = new MinimaxPlayer("IA2", new Position(8, 4), 2, 5, false);
    Player ia3 = new MinimaxPlayer("IA3", new Position(0, 4), 3, 5, false);
    Player ia4 = new MinimaxPlayer("IA4", new Position(8, 4), 2, 5, false);
    List<Player> players = new ArrayList<>();
    players.add(ia1);
    players.add(ia2);
    players.add(ia3);
    players.add(ia4);
    game = new GameState(board, players);
    assertThrows(IllegalArgumentException.class, () -> {
        new GameState(board, players);
    });
  }

  @Test
  // Check whether the MiniMax algorithm is better than a random algorithm. With a depth of 3, the
  // MiniMax algorithm should never lose
  public void check_MiniMax_better_than_random() {
    ia1 = new MinimaxPlayer("IA1", new Position(0, 4), 3, 5, false);
    ia2 = new RandomPlayer("IA2", new Position(8, 4));
    List<Player> players = new ArrayList<>();
    players.add(ia1);
    players.add(ia2);
    game = new GameState(board, players);

    while (!game.isGameOver()) {
      if (game.getCurrentPlayer().equals(ia1)) {
        game.applyMove(ia1.getNextMove(game));
      } else if (game.getCurrentPlayer().equals(ia2)) {
        game.applyMove(ia2.getNextMove(game));
      }
    }

    assertTrue(game.hasReachedGoal(ia1));
  }

  @Test
  // Check whether the MiniMax algorithm with a depth of 3 is better than a MiniMax algorithm with a
  // depth of 2.
  public void check_MiniMax_vs_MiniMax() {
    ia1 = new MinimaxPlayer("IA1", new Position(0, 4), 3, 5, false);
    ia2 = new MinimaxPlayer("IA2", new Position(8, 4), 2, 5, false);
    List<Player> players = new ArrayList<>();
    players.add(ia1);
    players.add(ia2);
    game = new GameState(board, players);

    while (!game.isGameOver()) {
      if (game.getCurrentPlayer().equals(ia1)) {
        game.applyMove(ia1.getNextMove(game));
      } else if (game.getCurrentPlayer().equals(ia2)) {
        game.applyMove(ia2.getNextMove(game));
      }
    }

    assertTrue(game.hasReachedGoal(ia1));
  }
}
