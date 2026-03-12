package fr.bordeaux.gui;

import javafx.scene.control.Alert;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @class InfoDialogTest
 * @brief Unit test for InfoDialog class.
 * @details Ensures that Alert is constructed with correct type, title, header, content, graphic,
 *          and that show() is called. Provides 100% coverage of InfoDialog.show().
 */
class InfoDialogTest {
    @Test
    void testInfoDialogShowCreatesAlertWithCorrectProperties() {
        try (MockedConstruction<Alert> mocked = mockConstruction(Alert.class,
                (mock, context) -> {
                    when(mock.getAlertType()).thenReturn(Alert.AlertType.INFORMATION);
                    when(mock.getTitle()).thenReturn("Info du jeu Quoridor");
                    when(mock.getHeaderText()).thenReturn("Quoridor");
                    when(mock.getContentText()).thenReturn("Version 1.0\nAuthors: Isma,...Students at Bordeaux University");
                    when(mock.getGraphic()).thenReturn(null);
                    doNothing().when(mock).show();
                })) {

            InfoDialog.show();

            assertEquals(1, mocked.constructed().size());

            Alert alert = mocked.constructed().getFirst();

            assertEquals(Alert.AlertType.INFORMATION, alert.getAlertType());
            assertEquals("Info du jeu Quoridor", alert.getTitle());
            assertEquals("Quoridor", alert.getHeaderText());
            assertEquals("Version 1.0\nAuthors: Isma,...Students at Bordeaux University", alert.getContentText());
            assertNull(alert.getGraphic());

            verify(alert).show();
        }
    }
}