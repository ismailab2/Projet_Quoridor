package fr.bordeaux.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;
import org.junit.jupiter.api.Test;

public class I18nTest {

  @Test
  public void testGetEnglish() {
    I18n.setLocale(Locale.ENGLISH);
    assertEquals("Quoridor version", I18n.get("quoridorVersion"));
  }

  @Test
  public void testGetFrench() {
    I18n.setLocale(Locale.FRENCH);
    assertEquals("Version de Quoridor", I18n.get("quoridorVersion"));
  }

  @Test
  public void testDynamicMessage() {
    I18n.setLocale(Locale.ENGLISH);
    String msg = I18n.get("loading", "game.txt");
    assertEquals("Loading game state from file: game.txt", msg);
  }
}
