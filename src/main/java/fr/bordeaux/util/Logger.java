package fr.bordeaux.util;

import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.i18n.I18n;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.MessageFormat;

/**
 * Simple logger for the Quoridor application.
 *
 * <p>Supports console or file output (INFO, WARNING, DEBUG, VERBOSE, ERROR).
 *
 * <p>Thread-safe Singleton pattern.
 */
@SuppressWarnings({"PMD.SystemPrintln", "PMD.SingleMethodSingleton"})
public final class Logger {
  /** ANSI escape code to reset text formatting to default. */
  private static final String RESET = "\u001B[0m";

  /** Coulor for INFO message */
  private static final String CYAN = "\u001B[36m";

  /** Coulor for DEBUG message */
  private static final String YELLOW = "\u001B[33m";

  /** Couleur WARNING */
  private static final String YELLOW_BOLD = "\u001B[33;1m";

  /** Couleur VERBOSE / TRACE */
  private static final String GREEN = "\u001B[32m";

  /** Coulor for ERROR message */
  private static final String RED = "\u001B[31m";

  /** Singleton instance of the logger. */
  private static Logger instance;

  /** Flux de sortie (file or System.out) */
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
    this.writer = null;
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
    } catch (final IOException exception) {
      System.err.println(I18n.get("error", I18n.get("creatingLogFile")));
      out = null;
    }

    this.writer = out;
  }

  /**
   * Return the Logger Singleton instance.
   *
   * @return logger's unique instance
   */
  public static synchronized Logger getInstance() {
    if (instance == null) {
      instance = new Logger();
      instance.refreshConfig();
    }
    return instance;
  }

  /**
   * Return the Logger Singleton instance with the output file.
   *
   * @param logFile file where to print the logs
   * @return logger's unique instance
   */
  public static synchronized Logger getInstance(final String logFile) {
    if (instance == null) {
      instance = new Logger(logFile);
      instance.refreshConfig();
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
   * Log un message de niveau INFO avec des paramètres.
   *
   * @param format message format (MessageFormat style, e.g., "{0}")
   * @param args les arguments du message
   */
  public void info(final String format, final Object... args) {
    if (isInfoEnabled()) {
      log("[INFO]", MessageFormat.format(format, args), CYAN);
    }
  }

  /**
   * Log un message de niveau WARNING.
   *
   * @param message message à afficher
   */
  public void warning(final String message) {
    if (isWarningEnabled()) {
      log("[WARNING]", message, YELLOW_BOLD);
    }
  }

  /**
   * Log un message de niveau WARNING avec des paramètres.
   *
   * @param format le format du message
   * @param args les arguments du message
   */
  public void warning(final String format, final Object... args) {
    if (isWarningEnabled()) {
      log("[WARNING]", MessageFormat.format(format, args), YELLOW_BOLD);
    }
  }

  /**
   * Log un message de niveau DEBUG.
   *
   * @param message message to display
   */
  public void debug(final String message) {
    if (isDebugEnabled()) {
      log("[DEBUG]", message, YELLOW);
    }
  }

  /**
   * Log un message de niveau DEBUG avec des paramètres.
   *
   * @param format le format du message
   * @param args les arguments du message
   */
  public void debug(final String format, final Object... args) {
    if (isDebugEnabled()) {
      log("[DEBUG]", MessageFormat.format(format, args), YELLOW);
    }
  }

  /**
   * Log un message de niveau VERBOSE.
   *
   * @param message message to display
   */
  public void verbose(final String message) {
    if (isVerboseEnabled()) {
      log("[VERBOSE]", message, GREEN);
    }
  }

  /**
   * Log un message de niveau VERBOSE avec des paramètres.
   *
   * @param format le format du message
   * @param args les arguments du message
   */
  public void verbose(final String format, final Object... args) {
    if (isVerboseEnabled()) {
      log("[VERBOSE]", MessageFormat.format(format, args), GREEN);
    }
  }

  /**
   * Log un message de niveau TRACE.
   *
   * @param message message très détaillé
   */
  public void trace(final String message) {
    if (isVerboseEnabled()) {
      log("[TRACE]", message, GREEN);
    }
  }

  /**
   * Log un message d'erreur.
   *
   * @param message message to display
   */
  public void error(final String message) {
    if (isErrorEnabled()) {
      log("[ERROR]", message, RED);
    }
  }

  /**
   * Log un message de niveau ERROR avec des paramètres.
   *
   * @param format le format du message
   * @param args les arguments du message
   */
  public void error(final String format, final Object... args) {
    if (isErrorEnabled()) {
      log("[ERROR]", MessageFormat.format(format, args), RED);
    }
  }

  /**
   * Log une erreur avec exception associée.
   *
   * @param message message d'erreur
   * @param exception exception associée
   */
  public void error(final String message, final Throwable exception) {
    if (isErrorEnabled()) {
      log(
          "[ERROR]",
          message + " | " + exception.getClass().getSimpleName() + ": " + exception.getMessage(),
          RED);
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
    PrintStream stream = (writer != null) ? writer : System.out;
    // Si on écrit sur la sortie standard (console), on bascule ERROR/WARNING sur stderr
    if (writer == null && (level.contains("ERROR") || level.contains("WARNING"))) {
      stream = System.err;
    }
    if (stream != null) {
      stream.println(color + level + RESET + " " + message);
    }
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
   * Indique si le niveau WARNING est activé.
   *
   * @return true (toujours actif)
   */
  public boolean isWarningEnabled() {
    return true;
  }

  /**
   * Indique si le niveau ERROR est activé.
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
