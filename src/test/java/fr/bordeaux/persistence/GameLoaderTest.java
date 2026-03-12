package fr.bordeaux.persistence;

import static org.junit.jupiter.api.Assertions.*;

import fr.bordeaux.core.*;
import java.io.*;
import java.util.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class GameLoaderTest {

  private static final String TEST_FILE = "load_test.txt";

  @BeforeAll
  public static void setup() throws IOException {
    // Initialize board and players
    Board board = new Board(9);
    List<Player> players = new ArrayList<>();
    players.add(new HumanPlayer("Player1", new Position(0, 4)));
    players.add(new HumanPlayer("Player2", new Position(8, 4)));

    GameState state = new GameState(board, players, 0);

    // Apply moves to generate history (undoStack)
    // Player 1 moves pawn from e1 (0,4) to e2 (1,4)
    state.applyMove(Move.pawn(new Position(0, 4), new Position(1, 4)));

    // Player 2 places a horizontal wall at c3 (2,2)
    state.applyMove(Move.wall(new Position(2, 2), Orientation.HORIZONTAL));

    // Save the state to a file
    GameSaver.saveGame(state, TEST_FILE);
  }

  @Test
  public void testLoadGame() throws IOException, GameLoadException {
    GameState loaded = GameLoader.loadGame(TEST_FILE);

    System.out.println("Joueur courant : " + (loaded.getCurrentIndexPlayer() + 1));
    System.out.println("Joueur 1 : " + loaded.getPlayers().get(0).getPosition());
    System.out.println("Joueur 2 : " + loaded.getPlayers().get(1).getPosition());
    System.out.println("Murs J1 : " + loaded.getPlayers().get(0).getRemainingWalls());
    System.out.println("Murs J2 : " + loaded.getPlayers().get(1).getRemainingWalls());
  }

  @Test
  public void testLoadGameErreur() throws IOException {
    PrintWriter writer = new PrintWriter(new FileWriter("erreur.txt"));
    writer.println("mauvais_entete");
    writer.println("2  # Couleur du joueur courant");
    writer.close();

    try {
      GameLoader.loadGame("erreur.txt");
    } catch (GameLoadException e) {
      System.out.println("Type erreur : " + e.getErrorType());
      System.out.println("Ligne : " + e.getLineNumber());
      System.out.println("Message : " + e.getMessage());
    }
  }

  @Test
  public void testLoadGameHistory() throws IOException, GameLoadException {
    GameState loaded = GameLoader.loadGame(TEST_FILE);
    Move[] history = loaded.getMovesPlayed(); // Reverse order, last move is index 0

    // Verify history stack size
    assertEquals(2, history.length, "History should contain 2 moves");

    // Last move was the wall at c3 (2,2)
    Move lastMove = history[0];
    assertTrue(lastMove.isWall());
    assertEquals(new Position(2, 2), lastMove.getTo());
    assertEquals(Orientation.HORIZONTAL, lastMove.getOrientation());
  }
}
