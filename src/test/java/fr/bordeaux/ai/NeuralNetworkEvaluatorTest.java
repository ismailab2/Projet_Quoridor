package fr.bordeaux.ai;

import static org.junit.jupiter.api.Assertions.*;

import fr.bordeaux.core.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

// Share a single trained model across all tests for speed.
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NeuralNetworkEvaluatorTest {

  // 5000 games and 750 epochs
  private static final int TEST_NUM_TRAINING_GAMES = 5000;
  private static final int TEST_EPOCHS = 750;
  private static final String MODEL_PATH = "src/main/resources/nnmodels/default_quoridor_model.bin";
  private NeuralNetworkEvaluator evaluator;
  private GameState state;

  /** Trains the model once before any test runs. */
  @BeforeAll
  public void setUp() throws IOException {
    File modelFile = new File(MODEL_PATH);

    if (modelFile.exists()) {
      System.out.println("--- Chargement du modèle pré-entraîné ---");
      evaluator = new NeuralNetworkEvaluator();

    } else {
      System.out.println("--- Aucun modèle trouvé. Entraînement en cours... ---");
      evaluator = new NeuralNetworkEvaluator(TEST_NUM_TRAINING_GAMES, TEST_EPOCHS);
      evaluator.saveModel(MODEL_PATH);
      System.out.println("--- Modèle sauvegardé pour les futurs tests ---");
    }
    Board board = new Board(9);
    Player p1 = new RandomPlayer("P1", new Position(0, 4), Color.WHITE);
    Player p2 = new RandomPlayer("P2", new Position(8, 4), Color.BLACK);
    List<Player> players = Arrays.asList(p1, p2);
    state = new GameState(board, players);
  }

  /** Releases TensorFlow resources after all tests have run. */
  @AfterAll
  public void tearDown() {
    evaluator.close();
  }

  /**
   * The evaluator must initialize without throwing any exception. This implicitly tests that
   * training completes successfully.
   */
  @Test
  public void testInitializationSucceeds() {
    assertNotNull(evaluator, "Evaluator should be initialized correctly");
  }

  /**
   * The score returned by evaluate() must always lie within [-1000, 1000], since it is derived from
   * a sigmoid output (probability in [0, 1]).
   */
  @Test
  public void testEvaluateReturnsScoreInValidRange() {
    int score = evaluator.evaluate(state);
    assertTrue(score >= -1000 && score <= 1000, "Score should be in [-1000, 1000], got: " + score);
  }

  /**
   * The evaluator must return the same score for the same game state (the model is deterministic
   * after training).
   */
  @Test
  public void testEvaluateIsConsistent() {
    int score1 = evaluator.evaluate(state);
    int score2 = evaluator.evaluate(state);
    assertEquals(score1, score2, "Evaluate should return the same score for the same state");
  }

  /**
   * A player positioned one row away from their goal should be evaluated more favorably than when
   * they are far from it. This is a sanity check that the model captures proximity-to-win.
   */
  @Test
  public void testEvaluateFavorsWinningPosition() {
    // nearState: p1 is moved close to its goal (row 8), p2 stays at its start
    Board boardNear = new Board(9);
    Player p1Near = new RandomPlayer("P1Near", new Position(0, 4), Color.WHITE);
    Player p2Near = new RandomPlayer("P2Near", new Position(8, 4), Color.BLACK);
    p1Near.setRemainingWalls(0);
    p2Near.setRemainingWalls(0);
    p1Near.setPosition(new Position(7, 3)); // 1 step from goal row 8
    GameState nearState = new GameState(boardNear, Arrays.asList(p1Near, p2Near));

    // farState: both players stay at their canonical starting positions
    Board boardFar = new Board(9);
    Player p1Far = new RandomPlayer("P1Far", new Position(0, 4), Color.WHITE);
    Player p2Far = new RandomPlayer("P2Far", new Position(8, 4), Color.BLACK);
    p1Far.setRemainingWalls(0);
    p2Far.setRemainingWalls(0);
    GameState farState = new GameState(boardFar, Arrays.asList(p1Far, p2Far));
    int scoreNear = evaluator.evaluate(nearState);
    int scoreFar = evaluator.evaluate(farState);
    int gap = scoreNear - scoreFar;
    assertTrue(
        gap >= 50,
        "A player closer to the goal should score clearly higher (gap >= 50). Near: "
            + scoreNear
            + ", Far: "
            + scoreFar
            + ", Gap: "
            + gap);
  }
}
