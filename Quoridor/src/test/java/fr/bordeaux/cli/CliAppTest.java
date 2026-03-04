package fr.bordeaux.cli;

import static org.junit.jupiter.api.Assertions.*;

<<<<<<< HEAD
=======
import fr.bordeaux.cli.command.MoveCommand;
import fr.bordeaux.core.*;
import fr.bordeaux.core.Board;
import fr.bordeaux.core.GameEngine;
import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;
import fr.bordeaux.core.TypePlayer;
>>>>>>> 07cd6540fd954d3317d9ee6a126bb9367e846292
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

<<<<<<< HEAD
import static org.junit.jupiter.api.Assertions.*;

import fr.bordeaux.AI.MinimaxPlayer;
import fr.bordeaux.cli.CliApp;
import fr.bordeaux.core.*;
import fr.bordeaux.graph.Graph;
import fr.bordeaux.cli.command.Command;
import fr.bordeaux.cli.command.MoveCommand;
import fr.bordeaux.cli.command.QuitCommand;

=======
>>>>>>> 07cd6540fd954d3317d9ee6a126bb9367e846292
public class CliAppTest {
  private GameEngine engine;

<<<<<<< HEAD
    @BeforeEach
    public void setUp() {
        engine = new GameEngine(new GameState(new Board(9), List.of(new HumanPlayer("Human", new Position(0, 4)), new MinimaxPlayer("Bob", new Position(8, 4)))));;
    }
=======
  @BeforeAll
  public static void setUp() {
    engine =
        new GameEngine(
            new GameState(
                new Board(9),
                List.of(
                    new Player("Human", new Position(0, 4), TypePlayer.HUMAN),
                    new Player("Bob", new Position(8, 4), TypePlayer.HUMAN))));
    ;
  }

  @AfterAll
  public static void tearDown() {
    engine = null;
  }

  @AfterAll
  public static void tearDown() {
    engine = null;
  }
>>>>>>> 07cd6540fd954d3317d9ee6a126bb9367e846292

  // Quit command test
  @Test
  public void testFetchQuitNoSaving() throws Exception {
    String input = "n\n";

    InputStream in = new ByteArrayInputStream(input.getBytes());
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    Terminal terminal = TerminalBuilder.builder().system(false).streams(in, out).build();
    LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();

    CliApp cli = new CliApp(engine, terminal, reader);
    try {
      cli.fetch("quit");
    } catch (EndOfFileException e) {
      e.printStackTrace();
    }

    assertFalse(cli.isRunning());
  }

  @ParameterizedTest
  @ValueSource(strings = {"help", "quit", "save", "foo"})
  public void testCommandDoesntChangeEngine(String str) throws Exception {
    GameEngine testEngine = engine;

    InputStream in = new ByteArrayInputStream("n\n".getBytes());
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    Terminal terminal = TerminalBuilder.builder().system(false).streams(in, out).build();
    LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();

    CliApp cli = new CliApp(engine, terminal, reader);
    try {
      cli.fetch(str);
    } catch (EndOfFileException e) {
      e.printStackTrace();
    }
  }

<<<<<<< HEAD
    /* @Test
    public void testMoveCommandPawn() throws IOException {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originOut = System.out;
        System.setOut(new PrintStream(outContent));
=======
  @Test
  public void testMoveCommandPawn() throws IOException {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originOut = System.out;
    System.setOut(new PrintStream(outContent));
>>>>>>> 07cd6540fd954d3317d9ee6a126bb9367e846292

    ByteArrayInputStream in = new ByteArrayInputStream("".getBytes());
    Terminal terminal = TerminalBuilder.builder().system(false).streams(in, outContent).build();
    LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();

    CliApp cli = new CliApp(engine, terminal, reader);
    cli.fetch("e1-e2");
    Position firstStart = engine.getState().getPlayers().getFirst().getStartingPosition();
    Position firstMoved = engine.getState().getPlayers().getFirst().getPosition();
    assertEquals(firstStart, MoveCommand.parsePosition("e1"));
    assertEquals(firstMoved, MoveCommand.parsePosition("e2"));

<<<<<<< HEAD
        Position secondStart = engine.getState().getPlayers().get(1).getStartingPosition();
        Position secondMoved = engine.getState().getPlayers().get(1).getPosition();
        assertEquals(secondStart, MoveCommand.parsePosition("e9"));
        assertEquals(secondMoved, MoveCommand.parsePosition("e9"));
    } */
}
=======
    Position secondStart = engine.getState().getPlayers().get(1).getStartingPosition();
    Position secondMoved = engine.getState().getPlayers().get(1).getPosition();
    assertEquals(secondStart, MoveCommand.parsePosition("e9"));
    assertEquals(secondMoved, MoveCommand.parsePosition("e9"));
  }
}
>>>>>>> 07cd6540fd954d3317d9ee6a126bb9367e846292
