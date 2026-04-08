package fr.bordeaux.ai.nn;

import static fr.bordeaux.ai.nn.NeuralNetworkEvaluator.NUM_FEATURES;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class NeuralNetworkTrainerTest {

  @Test
  public void testConstructor() {
    NeuralNetworkTrainer trainer = new NeuralNetworkTrainer();
    assertNotNull(trainer, "Trainer should be instantiable within the same package");
  }

  @Test
  public void testLoadModelWithValidStream() throws IOException {
    try (NeuralNetworkEvaluator evaluator = new NeuralNetworkEvaluator(0, 0)) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      try (DataOutputStream dos = new DataOutputStream(baos)) {
        // Write some dummy weights
        for (int i = 0; i < NUM_FEATURES; i++) {
          dos.writeFloat(0.5f);
        }
        // Write dummy bias
        dos.writeFloat(0.1f);
      }

      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      NeuralNetworkTrainer.loadModel(evaluator, bais);

      assertNotNull(evaluator);
    }
  }

  @Test
  public void testTrainModelBasic() throws IOException {
    try (NeuralNetworkEvaluator evaluator = new NeuralNetworkEvaluator(0, 0)) {
      NeuralNetworkTrainer trainer = new NeuralNetworkTrainer();

      // Test training with 1 game and 1 epoch to ensure no crash
      assertDoesNotThrow(
          () -> trainer.trainModel(evaluator, 1, 1),
          "Training with 1 game should not throw exception");
    }
  }

  @Test
  public void testLoadModelThrowsOnShortStream() throws IOException {
    try (NeuralNetworkEvaluator evaluator = new NeuralNetworkEvaluator(0, 0)) {
      byte[] shortData = new byte[4]; // Only 1 float worth of data
      ByteArrayInputStream bais = new ByteArrayInputStream(shortData);

      assertThrows(
          IOException.class,
          () -> NeuralNetworkTrainer.loadModel(evaluator, bais),
          "Loading from a short stream should throw IOException");
    }
  }
}
