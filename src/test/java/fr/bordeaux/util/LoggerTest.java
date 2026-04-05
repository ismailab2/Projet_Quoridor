package fr.bordeaux.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Field;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for Logger class. */
public class LoggerTest {

  private final PrintStream originalOut = System.out;
  private final ByteArrayOutputStream capturedOut = new ByteArrayOutputStream();
  private static final String LOG_FILE_NAME = "test_quoridor_unit.log";

  @BeforeEach
  void setUp() throws Exception {
    resetSingleton();
  }

  @AfterEach
  void tearDown() {
    System.setOut(originalOut);
    // Cleanup
    File logFile = new File(System.getProperty("user.home"), LOG_FILE_NAME);
    if (logFile.exists()) {
      logFile.delete();
    }
  }

  /**
   * Resets the Singleton instance using reflection to allow testing different constructor paths.
   */
  private void resetSingleton() throws Exception {
    Field instanceField = Logger.class.getDeclaredField("instance");
    instanceField.setAccessible(true);
    instanceField.set(null, null);
  }

  @Test
  void testGetInstance_NewInstanceCreation() {
    // Case: instance == null (first call)
    Logger logger = Logger.getInstance(LOG_FILE_NAME);
    assertNotNull(logger, "Logger should be successfully created");

    File logFile = new File(System.getProperty("user.home"), LOG_FILE_NAME);
    assertTrue(logFile.exists(), "Log file should be physically created on disk");
  }

  @Test
  void testGetInstance_DoubleCheckedLocking_AlreadyExists() {
    // First call initializes the singleton
    Logger first = Logger.getInstance(LOG_FILE_NAME);

    // Second call should enter the first if(instance == null), find it's NOT null, and return
    // immediately
    Logger second = Logger.getInstance("should_not_be_created.log");

    assertSame(first, second, "Should return the existing singleton instance");

    File ignoredFile = new File(System.getProperty("user.home"), "should_not_be_created.log");
    assertFalse(ignoredFile.exists(), "Should not have created a second log file");
  }

  @Test
  void testPrivateConstructor_IOExceptionFallback() {
    // Redirect System.out to capture the fallback message
    System.setOut(new PrintStream(capturedOut));

    String invalidFileName = ".";

    Logger logger = Logger.getInstance(invalidFileName);

    assertNotNull(logger, "Logger should fall back to a valid instance even on IO failure");
    String output = capturedOut.toString();
    assertTrue(
        output.contains("Erreur création fichier log, fallback console"),
        "Fallback message should be printed to console");

    // Verify it still works at INFO level (fallback console logic)
    logger.info("Test message after fallback");
    assertTrue(capturedOut.toString().contains("Test message after fallback"));
  }
}
