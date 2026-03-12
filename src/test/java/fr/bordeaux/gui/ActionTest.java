package fr.bordeaux.gui;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class ActionTest {

  @Test
  void testValues() {
    Action[] actions = Action.values();

    assertEquals(10, actions.length);
    assertArrayEquals(
        new Action[] {
          Action.NEW_GAME,
          Action.LOAD_GAME,
          Action.SAVE_GAME,
          Action.CONFIGURATION,
          Action.INFO,
          Action.QUIT,
          Action.UNDO,
          Action.REDO,
          Action.PAUSE,
          Action.HINT
        },
        actions);
  }

  @Test
  void testValueOf() {
    assertEquals(Action.NEW_GAME, Action.valueOf("NEW_GAME"));
    assertEquals(Action.LOAD_GAME, Action.valueOf("LOAD_GAME"));
    assertEquals(Action.SAVE_GAME, Action.valueOf("SAVE_GAME"));
    assertEquals(Action.CONFIGURATION, Action.valueOf("CONFIGURATION"));
    assertEquals(Action.INFO, Action.valueOf("INFO"));
    assertEquals(Action.QUIT, Action.valueOf("QUIT"));
    assertEquals(Action.UNDO, Action.valueOf("UNDO"));
    assertEquals(Action.REDO, Action.valueOf("REDO"));
    assertEquals(Action.PAUSE, Action.valueOf("PAUSE"));
    assertEquals(Action.HINT, Action.valueOf("HINT"));
  }

  @Test
  void testInvalidValueOf() {
    Executable executable = () -> Action.valueOf("INVALID");
    assertThrows(IllegalArgumentException.class, executable);
  }
}
