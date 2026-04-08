package fr.bordeaux.util;

import fr.bordeaux.config.ConfigManager;
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

  /** Code ANSI pour reset le style */
  private static final String RESET = "\u001B[0m";

  /** Couleur INFO */
  private static final String CYAN = "\u001B[36m";

  /** Couleur DEBUG */
  private static final String YELLOW = "\u001B[33m";

  /** Couleur WARNING */
  private static final String YELLOW_BOLD = "\u001B[33;1m";

  /** Couleur VERBOSE / TRACE */
  private static final String GREEN = "\u001B[32m";

  /** Couleur ERROR */
  private static final String RED = "\u001B[31m";

  /** Singleton instance of the logger. */
  private static Logger instance;

  /** Flux de sortie (file or System.out) */
  private final PrintStream writer;

  /** Indique si le mode debug est actif */
  private boolean debugEnabled;

  /** Indique si le mode verbose est actif */
  private boolean verboseEnabled;

  /** Constructeur privé (console). Initialise le logger avec une sortie standard. */
  private Logger() {
    this.writer = null;
  }

  /**
   * Constructeur privé (fichier).
   *
   * @param logFile nom du fichier de log
   */
  private Logger(final String logFile) {
    PrintStream out;

    try {
      final File file = new File(System.getProperty("user.home"), logFile);
      out = new PrintStream(file);
    } catch (final IOException exception) {
      System.err.println("Erreur création fichier log, fallback console");
      out = null;
    }

    this.writer = out;
  }

  /**
   * Retourne l'instance Singleton du logger (console).
   *
   * @return instance unique du logger
   */
  public static synchronized Logger getInstance() {
    if (instance == null) {
      instance = new Logger();
      instance.refreshConfig();
    }
    return instance;
  }

  /**
   * Retourne l'instance Singleton du logger avec sortie fichier.
   *
   * @param logFile nom du fichier de log
   * @return instance unique du logger
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
   * Log un message de niveau INFO.
   *
   * @param message message à afficher
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
   * @param message message à afficher
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
   * @param message message détaillé
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
   * @param message message d'erreur
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

  /** Recharge la configuration depuis le {@link ConfigManager}. */
  public void refreshConfig() {
    final ConfigManager config = ConfigManager.getInstance();

    this.debugEnabled = Boolean.parseBoolean(config.getOption("debug", "false"));
    this.verboseEnabled = Boolean.parseBoolean(config.getOption("verbose", "false"));
  }

  // =========================================================
  // INTERNAL
  // =========================================================

  /**
   * Méthode interne d'écriture des logs.
   *
   * @param level niveau du log
   * @param message contenu du message
   * @param color couleur ANSI associée
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
   * Indique si le niveau DEBUG est activé.
   *
   * @return true si activé, sinon false
   */
  public boolean isDebugEnabled() {
    return debugEnabled;
  }

  /**
   * Indique si le niveau INFO est activé.
   *
   * @return true (toujours actif)
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
   * @return true (toujours actif)
   */
  public boolean isErrorEnabled() {
    return true;
  }

  /**
   * Indique si le niveau VERBOSE est activé.
   *
   * @return true si activé, sinon false
   */
  public boolean isVerboseEnabled() {
    return verboseEnabled;
  }
}
