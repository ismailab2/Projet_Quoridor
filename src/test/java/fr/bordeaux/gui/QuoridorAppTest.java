package fr.bordeaux.gui;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

class QuoridorAppTest extends ApplicationTest {

  @Override
  public void start(Stage stage) throws IOException {
    QuoridorApp app = new QuoridorApp();
    app.start(stage);
  }

  @Test
  void testApplicationStarts() {
    Stage stage =
        (Stage)
            javafx.stage.Window.getWindows().stream()
                .filter(javafx.stage.Window::isShowing)
                .findFirst()
                .orElseThrow();

    assertNotNull(stage);
    assertEquals("Quoridor", stage.getTitle());
    assertTrue(stage.isShowing());
  }

  @Test
  void testSceneIsSet() {
    Stage stage =
        (Stage)
            javafx.stage.Window.getWindows().stream()
                .filter(javafx.stage.Window::isShowing)
                .findFirst()
                .orElseThrow();

    Scene scene = stage.getScene();

    assertNotNull(scene);
    assertTrue(scene.getWidth() > 0);
    assertTrue(scene.getHeight() > 0);
  }
}
