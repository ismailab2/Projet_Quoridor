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
  public void checkMiniMaxMoveWall() {
    board = new Board(9);
    ia1 = new MinimaxPlayer("IA1", new Position(8, 4), 2, 5, false, "h3");
    ia2 = new RandomPlayer("IA2", new Position(0, 4));
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
    ia1 = new MinimaxPlayer("IA1", new Position(2, 1), 2);
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
}
