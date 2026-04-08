package fr.bordeaux.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BlitzManagerTest {

  private GameEngine engine;
  private GameState state;
  private BlitzManager blitz;

  @BeforeEach
  public void setUp() {
    Board board = new Board(9);
    List<Player> players = new ArrayList<>();
    players.add(new HumanPlayer("Human 1", new Position(8, 4), Color.WHITE));
    players.add(new HumanPlayer("Human 2", new Position(0, 4), Color.BLACK));

    state = new GameState(board, players);
    engine = new GameEngine(state, true, false, false, false, 10);
    blitz = engine.getBlitzManager();
  }

  @AfterEach
  public void tearDown() {
    if (blitz != null) {
      blitz.pause();
    }
  }

  @Test
  public void testDecreaseTime() throws InterruptedException {
    state.setLimitTimePlayer(65000); // 65 seconds
    Player player = state.getCurrentPlayer();
    long initialTime = player.getRemainingTime();

    blitz.start();
    assertTrue(blitz.isRunning(), "Le BlitzManager devrait être actif");

    Thread.sleep(1500);

    long afterSleep = player.getRemainingTime();

    assertTrue(afterSleep < initialTime, "Le temps devrait avoir diminué");
    assertEquals(64000, afterSleep, "Le temps restant devrait être exactement de 64000 ms");
  }

  @Test
  public void testLooseByTime() throws InterruptedException {
    state.setLimitTimePlayer(1000); // 1 second
    Player player = state.getCurrentPlayer();

    blitz.start();
    blitz.start();
    assertTrue(blitz.isRunning());

    Thread.sleep(2500);

    assertEquals(0, player.getRemainingTime(), "Le temps restant devrait être de 0");
    assertTrue(state.isGameOver(), "La partie devrait être terminée (GameOver)");
    assertFalse(blitz.isRunning(), "Le BlitzManager devrait s'être mis en pause tout seul");
  }

  @Test
  public void testPauseTimer() throws InterruptedException {
    state.setLimitTimePlayer(5000);
    Player player = state.getCurrentPlayer();

    blitz.start();
    blitz.pause();
    assertFalse(blitz.isRunning(), "Le BlitzManager ne devrait plus être actif");

    Thread.sleep(1500);

    assertEquals(
        5000, player.getRemainingTime(), "Le temps ne devrait pas avoir diminué pendant la pause");
  }
}
