package fr.bordeaux.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.logging.Level;
import org.junit.jupiter.api.*;

public class LogTest {

  @BeforeEach
  void reset() {
    Log.logger.setLevel(Level.INFO);
  }

  @Test
  void testConstructor() {
    Log log = new Log();
    assertNotNull(log);
  }

  @Test
  void testSetVerboseTrue() {
    Log.setVerbose(true);
    assertEquals(Level.INFO, Log.logger.getLevel());
  }

  @Test
  void testSetVerboseFalse() {
    Log.setVerbose(false);
    assertEquals(Level.FINE, Log.logger.getLevel());
  }

  @Test
  void testSetDebugTrue() {
    Log.setDebug(true);
    assertEquals(Level.FINEST, Log.logger.getLevel());
  }

  @Test
  void testSetDebugFalseDoesNothing() {
    Log.setVerbose(true);
    Log.setDebug(false);
    assertEquals(Level.INFO, Log.logger.getLevel());
  }

  @Test
  void testInfo() {
    assertDoesNotThrow(() -> Log.info("hello"));
  }

  @Test
  void testWarning() {
    assertDoesNotThrow(() -> Log.warning("warn"));
  }

  @Test
  void testDebug() {
    assertDoesNotThrow(() -> Log.debug("debug"));
  }

  @Test
  void testTrace() {
    assertDoesNotThrow(() -> Log.trace("trace"));
  }

  @Test
  void testError() {
    Exception e = new RuntimeException("error");
    assertDoesNotThrow(() -> Log.error("msg", e));
  }

  @Test
  void testStaticInit() {
    assertNotNull(Log.logger);
    assertDoesNotThrow(() -> Log.info("init test"));
  }
}
