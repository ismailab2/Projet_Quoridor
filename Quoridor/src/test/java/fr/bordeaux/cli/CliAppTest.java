package fr.bordeaux.cli;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.reader.Completer;
import org.jline.reader.EndOfFileException;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.utils.ClosedException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;


import fr.bordeaux.cli.CliApp;
import fr.bordeaux.core.Board;
import fr.bordeaux.core.GameEngine;
import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;
import fr.bordeaux.graph.Graph;
import fr.bordeaux.cli.command.Command;
import fr.bordeaux.cli.command.QuitCommand;

public class CliAppTest {
    private static GameEngine engine;

    @BeforeAll
    public static void setUp() {
        engine = new GameEngine(new GameState(new Board(9), List.of(new Player("Alice", new Position(0, 4)), new Player("Bob", new Position(8, 4)))));
    }
    @AfterClass
    public static void tearDown() {
        engine = null;
    }
    

    // Quit command test
    @Test
    public void testFetchQuitNoSaving() throws Exception {
        String input = "n\n";

        InputStream in = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Terminal terminal = TerminalBuilder.builder().system(false).streams(in, out).build();
        LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();

        CliApp cli = new CliApp(engine, terminal, reader);
        try { cli.fetch("quit"); }
        catch (EndOfFileException e) { e.printStackTrace(); }
        
        assertFalse(cli.isRunning());
    }
    @ParameterizedTest
    @ValueSource(strings = {"help", "quit", "save"})
    public void testCommandDoesntChangeEngine(String str) throws Exception {
        GameEngine testEngine = engine;

        InputStream in = new ByteArrayInputStream("n\n".getBytes());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Terminal terminal = TerminalBuilder.builder().system(false).streams(in, out).build();
        LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();

        CliApp cli = new CliApp(engine, terminal, reader);
        try { cli.fetch(str); }
        catch (EndOfFileException e) { e.printStackTrace(); }
        assertTrue(engine.equals(testEngine));
    }
}