import static org.junit.Assert.*;

import java.util.*;
import org.junit.*;

import fr.bordeaux.AI.MinimaxPlayer;
import fr.bordeaux.core.Board;
import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;

public class MinimaxPlayerTest {

  private GameState game;
  private Board board;
  private Player ia1;
  private Player ia2;

  @Before
  public void setup() {
    board = new Board(9);
  }

  
  @Test
  // Check if the number of players is equal to 2. We cannot use AI with more than 2 players.
  public void check_nb_player(){
    ia1 = new MinimaxPlayer("IA1", new Position(0, 4), 3, 5, false);
    ia2 = new MinimaxPlayer("IA2", new Position(8, 4), 2, 5, false);
    List<Player> players = new ArrayList<>();
    players.add(ia1);
    players.add(ia2);
    game = new GameState(board, players);
    assertTrue(game.getPlayers().size() == 2);
  }

  @Test
  // Check whether the MiniMax algorithm is better than a random algorithm. With a depth of 3, the MiniMax algorithm should never lose
  public void check_MiniMax_better_than_random(){
    ia1 = new MinimaxPlayer("IA1", new Position(0, 4), 3, 5, false);
    ia2 = new MinimaxPlayer("IA2", new Position(8, 4), 30, 5, false); // I put a very high depth to test the implementation of the timer. In deed if the algorithm don't have the time to go at the depth 30 it will just return a random move.
    List<Player> players = new ArrayList<>();
    players.add(ia1);
    players.add(ia2);
    game = new GameState(board, players);

    while(!game.isGameOver()){
      if(game.getCurrentPlayer().equals(ia1)){
        game.applyMove(ia1.getNextMove(game));
      }
      else if (game.getCurrentPlayer().equals(ia2)){
        game.applyMove(ia2.getNextMove(game));
      }
    }

    assertTrue(game.hasReachedGoal(ia1));
  }

  @Test
  //Check whether the MiniMax algorithm with a depth of 3 is better than a MiniMax algorithm with a depth of 2.
  public void check_MiniMax_vs_MiniMax(){
    ia1 = new MinimaxPlayer("IA1", new Position(0, 4), 3, 5, false);
    ia2 = new MinimaxPlayer("IA2", new Position(8, 4), 2, 5, false);
    List<Player> players = new ArrayList<>();
    players.add(ia1);
    players.add(ia2);
    game = new GameState(board, players);

    while(!game.isGameOver()){
      if(game.getCurrentPlayer().equals(ia1)){
        game.applyMove(ia1.getNextMove(game));
      }
      else if (game.getCurrentPlayer().equals(ia2)){
        game.applyMove(ia2.getNextMove(game));
      }
    }

    assertTrue(game.hasReachedGoal(ia1));

  }
}