package fr.bordeaux.util;

import fr.bordeaux.config.ConfigManager;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Logger simple pour l'application Quoridor.
 *
 * <p>Supporte une sortie console ou fichier avec différents niveaux : INFO, DEBUG, VERBOSE et
 * ERROR.
 *
 * <p>Implémente le pattern Singleton thread-safe.
 */
public final class Logger {
  /** ANSI escape code to reset text formatting to default. */
  private static final String RESET = "\u001B[0m";

  /** Coulor for INFO message */
  private static final String CYAN = "\u001B[36m";

  /** Coulor for DEBUG message */
  private static final String YELLOW = "\u001B[33m";

  /** Coulor for VERBOSE message */
  private static final String GREEN = "\u001B[32m";

  /** Coulor for ERROR message */
  private static final String RED = "\u001B[31m";

  /** Singleton Logger */
  private static volatile Logger instance;

  /** Output stream */
  private final PrintStream writer;

  /** Boolean in case debug is enabled */
  private boolean debugEnabled;

  /** Boolean in case verbose is enabled */
  private boolean verboseEnabled;

  /**
   * Private constructor for console-based logging. Initializes settings from {@link ConfigManager}
   * and sets output to System.out.
   */
  private Logger() {
    this.writer = System.out;
    refreshConfig();
  }

  /**
   * Private constructor (with file).
   *
   * @param logFile file to put log messages
   */
  private Logger(final String logFile) {
    PrintStream out;

    try {
      final File file = new File(System.getProperty("user.home"), logFile);
      out = new PrintStream(file);
      out.close();
    } catch (IOException e) {
      System.out.println("Erreur création fichier log, fallback console");
      out = System.out;
    }

    this.writer = out;
    refreshConfig();
  }

  /**
   * Return the Logger Singleton instance.
   *
   * @return logger's unique instance
   */
  public static Logger getInstance() {
    if (instance == null) {
      synchronized (Logger.class) {
        if (instance == null) {
          instance = new Logger();
        }
      }
    }
    return instance;
  }

  /**
   * Return the Logger Singleton instance with the output file.
   *
   * @param logFile file where to print the logs
   * @return logger's unique instance
   */
  public static Logger getInstance(final String logFile) {
    if (instance == null) {
      synchronized (Logger.class) {
        if (instance == null) {
          instance = new Logger(logFile);
        }
      }
    }
    return instance;
  }

  // =========================================================
  // PUBLIC API
  // =========================================================

  /**
   * INFO level message to display.
   *
   * @param message message to display
   */
  public void info(final String message) {
    if (isInfoEnabled()) {
      log("[INFO]", message, CYAN);
    }
  }

  /**
   * DEBUG level message to display.
   *
   * @param message message to display
   */
  public void debug(final String message) {
    if (isDebugEnabled()) {
      log("[DEBUG]", message, YELLOW);
    }
  }

  /**
   * VERBOSE level message to display.
   *
   * @param message message to display
   */
  public void verbose(final String message) {
    if (isVerboseEnabled()) {
      log("[VERBOSE]", message, GREEN);
    }
  }

  /**
   * ERROR level message to display.
   *
   * @param message message to display
   */
  public void error(final String message) {
    if (isErrorEnabled()) {
      log("[ERROR]", message, RED);
    }
  }

  /**
   * ERROR level message to display.
   *
   * @param message message to display
   * @param e exception associée
   */
  public void error(final String message, final Exception e) {
    if (isErrorEnabled()) {
      log("[ERROR]", message + " | " + e.getClass().getSimpleName(), RED);
    }
  }

  // =========================================================
  // CONFIG
  // =========================================================

  /** Reload the configuration from the {@link ConfigManager}. */
  public void refreshConfig() {
    final ConfigManager config = ConfigManager.getInstance();

    this.debugEnabled = Boolean.parseBoolean(config.getOption("debug", "false"));
    this.verboseEnabled = Boolean.parseBoolean(config.getOption("verbose", "false"));
  }

  // =========================================================
  // INTERNAL
  // =========================================================

  /**
   * Intern method to print a message.
   *
   * @param level log level
   * @param message the content to display
   * @param color color to display the level
   */
  private void log(final String level, final String message, final String color) {
    writer.println(color + level + RESET + " " + message);
  }

  // =========================================================
  // LEVEL CHECKS
  // =========================================================

  /**
   * Check if DEBUG level is activated.
   *
   * @return true if activated, false otherwise
   */
  public boolean isDebugEnabled() {
    return debugEnabled;
  }

  /**
   * Check if INFO level is activated.
   *
   * @return true (always active)
   */
  public boolean isInfoEnabled() {
    return true;
  }

  /**
   * Check if ERROR level is activated.
   *
   * @return true (always active)
   */
  public boolean isErrorEnabled() {
    return true;
  }

  /**
   * Check if VERBOSE level is activated.
   *
   * @return true if activated, false otherwise
   */
  public boolean isVerboseEnabled() {
    return verboseEnabled;
  }
}
