package fr.bordeaux.cli;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import fr.bordeaux.core.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CliAppTest {

  private CliApp app;
  private GameEngine engine;
  private LineReader reader;
  private Terminal terminal;
  private Player p1;

  @BeforeEach
  void setUp() {
    engine = createEngine();
    p1 = engine.getState().getPlayers().getFirst();
    reader = mock(LineReader.class);
    terminal = mock(Terminal.class);
    app = new CliApp(engine, terminal, reader);
  }

  @Test
  void defaultConstructor() throws Exception {
    CliApp defaultApp = new CliApp();

    assertNotNull(defaultApp);
    assertNotNull(defaultApp.getEngine());
    assertTrue(defaultApp.isRunning());
  }

  @Test
  void constructorWithEngine() throws Exception {
    CliApp appWithEngine = new CliApp(engine);

    assertNotNull(appWithEngine);
    assertSame(engine, appWithEngine.getEngine());
    assertTrue(appWithEngine.isRunning());
  }

  @Test
  void beep() throws Exception {
    PrintWriter writer = mock(PrintWriter.class);
    Terminal term = mock(Terminal.class);
    when(term.writer()).thenReturn(writer);

    CliApp appWithTerminal = new CliApp(engine, term, reader);

    var method = CliApp.class.getDeclaredMethod("beep");
    method.setAccessible(true);
    method.invoke(appWithTerminal);

    verify(writer).print("\007");
    verify(term).flush();
  }

  @Test
  void runQuit() throws Exception {
    when(reader.readLine(">> ")).thenReturn("quit");
    when(reader.readLine("Save before quitting? [y/N] >> ")).thenReturn("n");

    assertDoesNotThrow(() -> app.run());

    assertFalse(app.isRunning());
    verify(reader).readLine(">> ");
    verify(reader).readLine("Save before quitting? [y/N] >> ");
    verify(terminal).close();
  }

  @Test
  void fetchEmpty() throws IOException {
    GameEngine before = app.getEngine();

    app.fetch("   ");

    assertSame(before, app.getEngine());
    assertTrue(app.isRunning());
    assertEquals(new Position(0, 4), p1.getPosition());
  }

  @Test
  void fetchQuit() throws IOException {
    when(reader.readLine(anyString())).thenReturn("n");

    app.fetch("quit");

    assertFalse(app.isRunning());
    verify(reader).readLine("Save before quitting? [y/N] >> ");
  }

  @Test
  void fetchHelp() throws IOException {
    GameEngine before = app.getEngine();

    app.fetch("help");

    assertSame(before, app.getEngine());
    assertTrue(app.isRunning());
  }

  @Test
  void fetchMove() throws IOException {
    app.fetch("e1-e2");

    assertEquals(new Position(1, 4), p1.getPosition());
  }

  private GameEngine createEngine() {
    Board board = new Board(9);
    Player player1 = new HumanPlayer("Human", new Position(0, 4), Color.WHITE);
    Player player2 = new HumanPlayer("Bob", new Position(8, 4), Color.BLACK);
    GameState state = new GameState(board, List.of(player1, player2));
    return new GameEngine(state, false, false, false, false, 10);
  }
}
