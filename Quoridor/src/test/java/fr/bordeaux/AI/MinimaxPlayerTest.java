package fr.bordeaux.AI;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.bordeaux.AI.MinimaxPlayer;
import fr.bordeaux.AI.RandomPlayer;
import fr.bordeaux.core.Board;
import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Move;
import fr.bordeaux.core.Position;
import fr.bordeaux.core.Move.Type;
import fr.bordeaux.core.Player;


public class MinimaxPlayerTest {

  private GameState game;
  private Board board;
  private Player ia1;
  private Player ia2;


  @Test
  public void checkMiniMax(){
    board = new Board(3);
    ia1 = new MinimaxPlayer("IA1", new Position(2, 1), 2);
    ia2 = new RandomPlayer("IA2", new Position(0, 1));
    ia1.setPosition(new Position(1, 1));
    ia2.setPosition(new Position(0,2));
    
    List<Player> players = new ArrayList<>();
    players.add(ia1);
    players.add(ia2);
    game = new GameState(board, players);

    Move res = ia1.getNextMove(game);
    assertTrue(res.getTo().equals(new Position(0, 1)) && res.getType() == Type.PAWN && res.getOrientation() == null && res.getFrom().equals(new Position(1, 1)));
  }

/*   @Test
  // Check whether the MiniMax algorithm is better than a random algorithm. With a depth of 3, the
  // MiniMax algorithm should never lose
  public void checkMiniMaxBetterThanRandom() {
    board = new Board(9);
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
  } */
}
