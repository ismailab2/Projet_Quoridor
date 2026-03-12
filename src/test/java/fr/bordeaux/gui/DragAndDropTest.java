package fr.bordeaux.gui;

import static org.junit.jupiter.api.Assertions.*;

import fr.bordeaux.core.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DragAndDropTest {

  private GameEngine engine;
  private GameState state;

  // Create a fresh game before each test
  @BeforeEach
  void setUp() {
    Board board = new Board(9);
    state = new GameState(board, Player.createDefaultPlayers());
    engine = new GameEngine(state);
  }

  // install()
  // Engine and game state must be correctly initialized
  @Test
  void install_isReady() {
    assertNotNull(engine);
    assertNotNull(state);
  }

  // The game must have at least 2 players
  @Test
  void install_minTwoPlayers() {
    assertTrue(state.getPlayers().size() >= 2);
  }

  // installDragOnPawn()
  // Current player must be at his start position
  @Test
  void drag_currentPlayerAtStart() {
    Position currentPos = state.getCurrentPlayer().getPosition();
    assertEquals(new Position(0, 4), currentPos);
  }

  // isMoveLegal()
  // A move one step forward must be accepted
  @Test
  void move_legal_isAccepted() {
    Move move = Move.pawn(new Position(0, 4), new Position(1, 4));
    assertTrue(RuleChecker.isMoveLegal(state, move));
    assertTrue(engine.play(move));
  }

  // A move far away must be rejected
  @Test
  void move_illegal_isRejected() {
    Move move = Move.pawn(new Position(0, 4), new Position(5, 5));
    assertFalse(RuleChecker.isMoveLegal(state, move));
    assertFalse(engine.play(move));
  }

  // installDropOnCell()
  // After a move, it must be the other player's turn
  @Test
  void drop_playerChanges() {
    assertEquals("Human", state.getCurrentPlayer().getName());
    engine.play(Move.pawn(new Position(0, 4), new Position(1, 4)));
    assertEquals("Bob", state.getCurrentPlayer().getName());
  }

  // After a move, the pawn position must be updated
  @Test
  void drop_positionUpdated() {
    engine.play(Move.pawn(new Position(0, 4), new Position(1, 4)));
    engine.play(Move.pawn(new Position(8, 4), new Position(7, 4)));
    // Human position must be (1,4)
    Player human = state.getPlayers().get(0);
    assertEquals(new Position(1, 4), human.getPosition());
  }

  // No move allowed when game is paused
  @Test
  void drop_whenPaused_isRejected() {
    engine.pause();
    Move move = Move.pawn(new Position(0, 4), new Position(1, 4));
    assertFalse(engine.play(move));
  }
}
