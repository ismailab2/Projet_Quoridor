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

  /** Code ANSI pour reset le style */
  private static final String RESET = "\u001B[0m";

  /** Couleur INFO */
  private static final String CYAN = "\u001B[36m";

  /** Couleur DEBUG */
  private static final String YELLOW = "\u001B[33m";

  /** Couleur VERBOSE */
  private static final String GREEN = "\u001B[32m";

  /** Couleur ERROR */
  private static final String RED = "\u001B[31m";

  /** Instance unique du Logger */
  private static volatile Logger instance;

  /** Flux de sortie */
  private final PrintStream writer;

  /** Indique si le mode debug est actif */
  private boolean debugEnabled;

  /** Indique si le mode verbose est actif */
  private boolean verboseEnabled;

  /** Constructeur privé (console). Initialise le logger avec une sortie standard. */
  private Logger() {
    this.writer = System.out;
    refreshConfig();
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
    } catch (IOException e) {
      System.out.println("Erreur création fichier log, fallback console");
      out = System.out;
    }

    this.writer = out;
    refreshConfig();
  }

  /**
   * Retourne l'instance Singleton du logger (console).
   *
   * @return instance unique du logger
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
   * Retourne l'instance Singleton du logger avec sortie fichier.
   *
   * @param logFile nom du fichier de log
   * @return instance unique du logger
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
   * Log une erreur avec exception associée.
   *
   * @param message message d'erreur
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
    writer.println(color + level + RESET + " " + message);
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
