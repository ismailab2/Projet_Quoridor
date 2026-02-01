package fr.bordeaux.core;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestGameEngine {

  @Test
  public void testBonjour() {
    GameEngine gameEngine = new GameEngine();

    String result = gameEngine.bonjour();

    assertEquals("bonjour", result);
  }
}
