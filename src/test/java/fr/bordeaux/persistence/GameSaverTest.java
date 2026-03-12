package fr.bordeaux.persistence;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.bordeaux.core.*;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import org.junit.jupiter.api.Test;

public class GameSaverTest {

  @Test
  public void testSaveGame() throws IOException {
    Board board = new Board(9);
    board.addWall(new Position(2, 3), Orientation.HORIZONTAL);
    board.addWall(new Position(4, 5), Orientation.VERTICAL);

    List<Player> players = new ArrayList<>();
    players.add(new HumanPlayer("Joueur1", new Position(0, 4)));
    players.add(new HumanPlayer("Joueur2", new Position(8, 4)));

    GameState state = new GameState(board, players, 1);
    GameSaver.saveGame(state, "test.txt");
    System.out.println("Fichier généré !");
  }

  @Test
  public void testSaveGameWithHistory() throws IOException {
    // Setup board and players
    Board board = new Board(9);
    List<Player> players = new ArrayList<>();
    players.add(new HumanPlayer("Player1", new Position(0, 4))); // e1
    players.add(new HumanPlayer("Player2", new Position(8, 4))); // e9
    GameState state = new GameState(board, players, 0);

    // Simulate moves
    // Player 1 moves from e1 (0,4) to e2 (1,4)
    state.applyMove(Move.pawn(new Position(0, 4), new Position(1, 4)));
    // Player 2 places a horizontal wall at c3 (2,2)
    state.applyMove(Move.wall(new Position(2, 2), Orientation.HORIZONTAL));
    // Player 1 moves from e2 (1,4) to f2 (1,5)
    state.applyMove(Move.pawn(new Position(1, 4), new Position(1, 5)));

    // Save the game to a file
    String filename = "test_history.txt";
    GameSaver.saveGame(state, filename);

    // Verify file existence and content
    File file = new File(filename);
    assertTrue(file.exists(), "The save file should be created");

    // Read file and verify the [History] section
    List<String> lines = Files.readAllLines(file.toPath());
    boolean historyFound = false;
    List<String> historyLines = new ArrayList<>();

    for (String line : lines) {
      if (line.trim().equals("[History]")) {
        historyFound = true;
        continue;
      }
      if (historyFound) {
        if (line.startsWith("[") || line.trim().isEmpty()) break;
        historyLines.add(line.trim());
      }
    }

    assertTrue(historyFound, "The [History] section header is missing");
    assertFalse(historyLines.isEmpty(), "The [History] section should not be empty");

    // Assert specific move notation (Chronological order)
    // Format expected: "1 e1-e2; 2 c3h;" (Turn 1) and "1 e2-f2;" (Turn 2)
    String fullHistory = String.join(" ", historyLines);

    assertTrue(
        fullHistory.contains("1 e1-e2;"), "First move (Player 1 pawn) is missing or incorrect");
    assertTrue(
        fullHistory.contains("2 c3h;"), "Second move (Player 2 wall) is missing or incorrect");
    assertTrue(
        fullHistory.contains("1 e2-f2;"), "Third move (Player 1 pawn) is missing or incorrect");
  }
}
