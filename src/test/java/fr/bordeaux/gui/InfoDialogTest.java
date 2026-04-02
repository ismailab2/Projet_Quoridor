package fr.bordeaux.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import javafx.scene.control.Alert;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

class InfoDialogTest {

  @Test
  void testShowCreatesAlert() {

    try (MockedConstruction<Alert> mocked =
        mockConstruction(
            Alert.class,
            (mock, context) -> {
              doNothing().when(mock).show();
            })) {

      InfoDialog.show();

      assertEquals(1, mocked.constructed().size());

      Alert alert = mocked.constructed().get(0);

      verify(alert).setTitle("Info du jeu Quoridor");
      verify(alert).setHeaderText("Quoridor");
      verify(alert).setGraphic(null);
      verify(alert).setContentText("Version 1.0\nAuthors: Isma,...Students at Bordeaux University");
      verify(alert).show();
    }
  }
}
