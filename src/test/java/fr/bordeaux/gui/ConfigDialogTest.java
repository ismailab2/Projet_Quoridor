package fr.bordeaux.gui;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class ConfigDialogTest {

  @BeforeAll
  static void initJavaFx() {
    try {
      Platform.startup(() -> {});
    } catch (IllegalStateException ignored) {
    }
  }

  @Test
  void testHandleKeyPress_modifierKey_returnsEarly() throws Exception {
    KeyEvent event = mock(KeyEvent.class);

    when(event.getCode()).thenReturn(KeyCode.CONTROL);

    Button button = new Button();

    var method =
        ConfigDialog.class.getDeclaredMethod(
            "handleKeyPress", KeyEvent.class, Action.class, Button.class);
    method.setAccessible(true);

    try (MockedStatic<ShortcutManager> mock = mockStatic(ShortcutManager.class)) {

      ShortcutManager manager = mock(ShortcutManager.class);
      when(ShortcutManager.getInstance()).thenReturn(manager);

      method.invoke(null, event, Action.values()[0], button);

      verify(manager, never()).setShortcut(any(), any());
    }
  }

  @Test
  void testHandleKeyPress_validKey_updatesShortcut() throws Exception {
    KeyEvent event = mock(KeyEvent.class);

    when(event.getCode()).thenReturn(KeyCode.A);
    when(event.isControlDown()).thenReturn(true);
    when(event.isShiftDown()).thenReturn(false);
    when(event.isAltDown()).thenReturn(false);

    Button button = new Button();

    var method =
        ConfigDialog.class.getDeclaredMethod(
            "handleKeyPress", KeyEvent.class, Action.class, Button.class);
    method.setAccessible(true);

    try (MockedStatic<ShortcutManager> mock = mockStatic(ShortcutManager.class)) {

      ShortcutManager manager = mock(ShortcutManager.class);
      when(ShortcutManager.getInstance()).thenReturn(manager);

      method.invoke(null, event, Action.values()[0], button);

      verify(manager, times(1)).setShortcut(eq(Action.values()[0]), any());
      assertNotNull(button.getText());
    }
  }

  @Test
  void testIsModifierKey() throws Exception {
    var method = ConfigDialog.class.getDeclaredMethod("isModifierKey", KeyCode.class);
    method.setAccessible(true);

    assertTrue((boolean) method.invoke(null, KeyCode.CONTROL));
    assertTrue((boolean) method.invoke(null, KeyCode.SHIFT));
    assertTrue((boolean) method.invoke(null, KeyCode.ALT));

    assertFalse((boolean) method.invoke(null, KeyCode.A));
  }

  @Test
  void testExtractModifiers() throws Exception {
    KeyEvent event = mock(KeyEvent.class);

    when(event.isControlDown()).thenReturn(true);
    when(event.isShiftDown()).thenReturn(true);
    when(event.isAltDown()).thenReturn(false);

    var method = ConfigDialog.class.getDeclaredMethod("extractModifiers", KeyEvent.class);
    method.setAccessible(true);

    KeyCodeCombination.Modifier[] mods = (KeyCodeCombination.Modifier[]) method.invoke(null, event);

    assertEquals(2, mods.length);
  }
}
