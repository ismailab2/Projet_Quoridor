import static org.junit.Assert.*;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;
import org.junit.*;

import fr.bordeaux.AI.AiPlayer;
import fr.bordeaux.core.Board;
import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;
import fr.bordeaux.core.TypePlayer;

public class AiPlayerTest {

  private GameState game;
  private Board board;
  private Player ia1;
  private Player ia2;

  @Before
  public void setup() {
    board = new Board(9);
    ia1 = new Player("IA1", new Position(0, 4), TypePlayer.IA);
    ia2 = new Player("IA2", new Position(8, 4), TypePlayer.IA);

    List<Player> players = new ArrayList<>();
    players.add(ia1);
    players.add(ia2);

    game = new GameState(board, players);

  }

  
  @Test
  // Check if the number of players is equal to 2. We cannot use AI with more than 2 players.
  public void check_nb_player(){
    assertTrue(game.getPlayers().size() == 2);
  }

  @Test
  // Check whether the MiniMax algorithm is better than a random algorithm. With a depth of 3, the MiniMax algorithm should never lose
  public void check_MiniMax_better_than_random(){
    AiPlayer ai1 = new AiPlayer(game, 3);
    AiPlayer ai2 = new AiPlayer(game, "Random");
    while(!game.isGameOver()){
      if(game.getCurrentPlayer().equals(ia1)){
        ai1.play();
      }
      else if (game.getCurrentPlayer().equals(ia2)){
        ai2.play();
      }
    }

    assertTrue(game.hasReachedGoal(ia1));
  }

  @Test
  //Check whether the MiniMax algorithm with a depth of 3 is better than a MiniMax algorithm with a depth of 2.
  public void check_MiniMax_vs_MiniMax(){
    AiPlayer ai1 = new AiPlayer(game, 3);
    AiPlayer ai2 = new AiPlayer(game, 2);
    while(!game.isGameOver()){
      if(game.getCurrentPlayer().equals(ia1)){
        ai1.play();
      }
      else if (game.getCurrentPlayer().equals(ia2)){
        ai2.play();
      }
    }

    assertTrue(game.hasReachedGoal(ia1));

  }
}