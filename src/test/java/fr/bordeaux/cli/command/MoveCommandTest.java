package fr.bordeaux.cli.command;

import static org.junit.jupiter.api.Assertions.*;

import fr.bordeaux.core.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MoveCommandTest {

  private GameEngine engine;
  private Player p1;
  private Player p2;

  @BeforeEach
  void setUp() {
    Board board = new Board(9);
    p1 = new HumanPlayer("Human", new Position(0, 4));
    p2 = new HumanPlayer("Bob", new Position(8, 4));
    GameState state = new GameState(board, List.of(p1, p2));
    engine = new GameEngine(state);
  }

  @Test
  void parsePosition() {
    MoveCommand command = new MoveCommand();

    Position pos = command.parsePosition("e2");

    assertEquals(new Position(1, 4), pos);
  }

  @Test
  void executePawnMove() {
    MoveCommand command = new MoveCommand("e1-e2");

    command.execute(engine);

    assertEquals(new Position(1, 4), p1.getPosition());
  }

  @Test
  void executeInvalidMove() {
    MoveCommand command = new MoveCommand("zzz");
    Position before = p1.getPosition();

    command.execute(engine);

    assertEquals(before, p1.getPosition());
  }

  @Test
  void executeHorizontalWall() {
    MoveCommand command = new MoveCommand("e2h");

    assertDoesNotThrow(() -> command.execute(engine));
  }

  @Test
  void executeVerticalWall() {
    MoveCommand command = new MoveCommand("e2v");

    assertDoesNotThrow(() -> command.execute(engine));
  }

  @Test
  void handleMove() {
    MoveCommand command = new MoveCommand("e1-e2");

    command.handleMove(engine);

    assertEquals(new Position(1, 4), p1.getPosition());
  }

  @Test
  void handleWallHorizontal() {
    MoveCommand command = new MoveCommand("e2h");

    assertDoesNotThrow(() -> command.handleWall(engine));
  }

  @Test
  void handleWallVertical() {
    MoveCommand command = new MoveCommand("e2v");

    assertDoesNotThrow(() -> command.handleWall(engine));
  }

  @Test
  void helpText() {
    MoveCommand command = new MoveCommand();

    assertDoesNotThrow(command::helpText);
  }
}
