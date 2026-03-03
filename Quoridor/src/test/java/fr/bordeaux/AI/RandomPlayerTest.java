package fr.bordeaux.AI;

import static org.junit.jupiter.api.Assertions.*;

import fr.bordeaux.core.Board;
import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Move;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class RandomPlayerTest {

  private GameState game;
  private Board board;
  private Player ia1;
  private Player ia2;

  @Test
  public void checkRandom() {
    board = new Board(9);
    ia1 = new RandomPlayer("IA1", new Position(8, 4));
    ia2 = new RandomPlayer("IA2", new Position(0, 4));

    List<Player> players = new ArrayList<>();
    players.add(ia1);
    players.add(ia2);
    game = new GameState(board, players);

    List<Move> saveMove = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      saveMove.add(ia1.getNextMove(game));
    }

    Move firstMove = saveMove.get(0);
    boolean check = false;
    for (Move m : saveMove) {
      if (!m.equals(firstMove)) {
        check = true;
      }
    }

    assertTrue(check);
  }
}
