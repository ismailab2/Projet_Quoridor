package fr.bordeaux.ai;

import static org.junit.jupiter.api.Assertions.*;

import fr.bordeaux.core.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MCTSPlayerTest {

  private Board board;
  private List<Player> players;
  private MctsPlayer ai;

  @BeforeEach
  public void setUp() {
    int size = 9;
    board = new Board(size);
    players = new ArrayList<>();

    // AI starts at the top (row 0, middle)
    ai = new MctsPlayer("MCTS_AI", new Position(0, 4), 2000);
    // Opponent starts at the bottom (row 8, middle)
    HumanPlayer opponent = new HumanPlayer("Opponent", new Position(8, 4));

    players.add(ai);
    players.add(opponent);
  }

  @Test
  public void testMCTSInitialization() {
    assertEquals("MCTS_AI", ai.getName());
    assertEquals(new Position(0, 4), ai.getPosition());
    assertEquals(10, ai.getRemainingWalls(), "AI should start with 10 walls");
  }

  @Test
  public void testMCTSCopy() {
    Player copy = ai.copy();

    assertNotSame(ai, copy, "Copy should be a different object instance");
    assertEquals(ai.getName(), copy.getName());
    assertEquals(ai.getPosition(), copy.getPosition());
    assertEquals(ai.getRemainingWalls(), copy.getRemainingWalls());
    assertInstanceOf(MctsPlayer.class, copy, "Copy should also be an instance of MCTSPlayer");
  }

  @Test
  public void testGetNextMoveReturnsLegalMove() {
    GameState state = new GameState(board, players, 0); // AI's turn

    Move move = ai.getNextMove(state);

    assertNotNull(move, "MCTS should return a move");

    // Verify the move is among legal moves
    List<Move> legalMoves = state.generateLegalMoves();
    assertTrue(
        legalMoves.contains(move), "The chosen move must be legal according to the GameState");
  }

  @Test
  public void testMCTSFindsWinningMove() {
    // Create a scenario where the AI is one step away from winning
    // Goal for Player starting at X=0 is X=8

    // We remove walls to speed up the tree expansion drastically avoiding random noise
    ai.setRemainingWalls(0);
    players.get(1).setRemainingWalls(0);

    ai.setPosition(new Position(7, 3));

    GameState state = new GameState(board, players, 0); // AI's turn

    // The winning move is to go to row 8
    Move winningMove = Move.pawn(new Position(7, 3), new Position(8, 3));
    Move chosenMove = MctsPlayer.hint(ai, state, 1000, false);

    assertEquals(
        winningMove.getTo(),
        chosenMove.getTo(),
        "MCTS should prioritize the move that wins the game");
  }

  @Test
  public void testMCTSPlayerSettings() {
    var settings = ai.getSettings();
    assertEquals("mcts", settings.get("mode"), "Settings should correctly identify MCTS mode");
  }
}
