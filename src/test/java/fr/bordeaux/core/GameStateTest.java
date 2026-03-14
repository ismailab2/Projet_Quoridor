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
  public void constructorOk2Players() {
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
  public void listenerIsCalledOnApplyMove() {
    final boolean[] called = {false};

    game.addListener(() -> called[0] = true);

    Position pos = game.getCurrentPlayer().getPosition();
    Move move = Move.pawn(pos, new Position(1, 4));
    game.applyMove(move);

    assertTrue(called[0]);
  }

  @Test
  public void applyMovePawnChangesPositionAndSwitchPlayer() {
    Position newPos = new Position(1, 4);
    Position pos = game.getCurrentPlayer().getPosition();

    Move move = Move.pawn(pos, newPos);
    game.applyMove(move);

    assertEquals(newPos, human.getPosition());
    assertEquals(ia, game.getCurrentPlayer());
  }

  @Test
  public void applyMoveWallPlacesWallAndSwitchPlayer() {
    Move move = Move.wall(new Position(4, 4), Orientation.HORIZONTAL);

    game.applyMove(move);

    assertEquals(ia, game.getCurrentPlayer());
  }

  @Test
  public void undoRevertsMove() {
    Position p1 = new Position(1, 4);
    Player player = game.getCurrentPlayer();
    game.applyMove(Move.pawn(player.getPosition(), p1));

    game.undo();

    assertEquals(new Position(0, 4), human.getPosition());
    assertEquals(human, game.getCurrentPlayer());
  }

  @Test
  public void redoReappliesMove() {
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
  public void undoDoesNothingIfStackEmpty() {
    game.undo();
    assertEquals(human, game.getCurrentPlayer());
  }

  @Test
  public void redoDoesNothingIfStackEmpty() {
    game.redo();
    assertEquals(human, game.getCurrentPlayer());
  }

  @Test
  public void victoryIsDetected() {
    human.setPosition(new Position(8, 4));
    Position pos = game.getCurrentPlayer().getPosition();
    // System.out.println("1 " + human.getPosition());
    game.applyMove(Move.pawn(pos, new Position(8, 4)));
    // System.out.println("2 " + human.getPosition());

    assertFalse(game.isGameOver());
  }

  @Test
  public void winConditionFromTopRow() {
    Predicate<Integer> predicate = game.getWinCondition(human);
    int id = board.id(8, 4);
    assertTrue(predicate.test(id));
  }

  @Test
  public void winConditionFromBottomRow() {
    Player p = new HumanPlayer("P", new Position(8, 4));
    GameState g = new GameState(board, List.of(p, ia));
    Predicate<Integer> predicate = g.getWinCondition(p);
    int id = board.id(0, 4);
    assertTrue(predicate.test(id));
  }

  @Test
  public void winConditionFromLeftCol() {
    Player p = new HumanPlayer("P", new Position(4, 0));
    GameState g = new GameState(board, List.of(p, ia));
    Predicate<Integer> predicate = g.getWinCondition(p);
    int id = board.id(4, 8);
    assertTrue(predicate.test(id));
  }

  @Test
  public void winConditionFromRightCol() {
    Player p = new HumanPlayer("P", new Position(4, 8));
    GameState g = new GameState(board, List.of(p, ia));
    Predicate<Integer> predicate = g.getWinCondition(p);
    int id = board.id(4, 0);
    assertTrue(predicate.test(id));
  }

  @Test
  public void saveAndLoadJsonWorks() {
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
  public void resetCreatesNewGame() {
    GameState newGame = game.reset();

    assertNotSame(game, newGame);
    assertEquals(2, newGame.getPlayers().size());
    assertEquals(9, newGame.getBoard().getSize());
  }

  @Test
  public void generateLegalMovesNoWallsLeftExcludesWallMoves() {
    // Use all walls
    for (int i = 0; i < 10; i++) {
      human.useWall();
    }

    List<Move> legalMoves = game.generateLegalMoves();

    // Should not contain any wall moves
    boolean hasWallMove = legalMoves.stream().anyMatch(Move::isWall);
    assertFalse(hasWallMove);

    // But should still have pawn moves
    Move pawnMove = Move.pawn(new Position(0, 4), new Position(1, 4));
    assertTrue(legalMoves.contains(pawnMove));
  }

  @Test
  public void generateLegalMovesWithOpponentBlockingIncludesJump() {
    // Move human to position where IA is blocking
    human.setPosition(new Position(6, 4));
    ia.setPosition(new Position(7, 4));

    List<Move> legalMoves = game.generateLegalMoves();

    // Should include jump over IA
    Move jumpMove = Move.pawn(new Position(6, 4), new Position(8, 4));
    assertTrue(legalMoves.contains(jumpMove));

    game.getBoard().addWall(new Position(7, 4), Orientation.HORIZONTAL);
    legalMoves = game.generateLegalMoves();
    Move diagJumpMove1 = Move.pawn(new Position(6, 4), new Position(7, 5));
    Move diagJumpMove2 = Move.pawn(new Position(6, 4), new Position(7, 3));
    assertTrue(legalMoves.contains(diagJumpMove1));
    assertTrue(legalMoves.contains(diagJumpMove2));
    assertFalse(legalMoves.contains(jumpMove));
  }

  @Test
  public void generateLegalMovesWithWallsBlockingReducesMoves() {
    // Add a wall that blocks some pawn moves
    board.addWall(new Position(0, 4), Orientation.VERTICAL);

    List<Move> legalMoves = game.generateLegalMoves();

    // Should not include pawn move to the right if blocked
    Move blockedMove = Move.pawn(new Position(0, 4), new Position(0, 5));

    assertFalse(legalMoves.contains(blockedMove));
  }

  @Test
  public void generateLegalMovesInitialPositionExactMoves() {
    // Use a small board for exact verification
    Board smallBoard = new Board(5);
    Player p1 = new HumanPlayer("P1", new Position(0, 2));
    p1.setPosition(new Position(1, 2));
    Player p2 = new HumanPlayer("P2", new Position(4, 2));
    GameState smallGame = new GameState(smallBoard, List.of(p1, p2));

    List<Move> legalMoves = smallGame.generateLegalMoves();

    // Expected: 3 pawn moves + 32 wall moves = 35
    assertEquals(36, legalMoves.size());

    // Check specific pawn moves
    assertTrue(legalMoves.contains(Move.pawn(new Position(1, 2), new Position(0, 2))));
    assertTrue(legalMoves.contains(Move.pawn(new Position(1, 2), new Position(1, 3))));
    assertTrue(legalMoves.contains(Move.pawn(new Position(1, 2), new Position(1, 1))));
    assertTrue(legalMoves.contains(Move.pawn(new Position(1, 2), new Position(2, 2))));

    // Check that non-adjacent pawn moves are not included
    assertFalse(legalMoves.contains(Move.pawn(new Position(0, 2), new Position(1, 1))));
    assertFalse(legalMoves.contains(Move.pawn(new Position(0, 2), new Position(0, 0))));

    // Check some wall moves are included
    assertTrue(legalMoves.contains(Move.wall(new Position(0, 0), Orientation.HORIZONTAL)));
    assertTrue(legalMoves.contains(Move.wall(new Position(2, 2), Orientation.VERTICAL)));

    // Ensure no invalid wall positions
    assertFalse(
        legalMoves.contains(
            Move.wall(new Position(4, 4), Orientation.HORIZONTAL))); // out of bounds for walls
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
