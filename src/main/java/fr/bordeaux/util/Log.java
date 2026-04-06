package fr.bordeaux.util;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Centralized logging for application events, errors, and debug info. */
public final class Log {

  /** Centralized Logger instance for the Quoridor application. */
  public static final Logger LOGGER = Logger.getLogger("Quoridor");

  static {
    final ConsoleHandler consoleHandler = new ConsoleHandler();
    consoleHandler.setLevel(Level.ALL);
    LOGGER.addHandler(consoleHandler);
    LOGGER.setUseParentHandlers(false);
    LOGGER.setLevel(Level.INFO);
  }

  /** Private constructor to prevent instantiation. */
  private Log() {}

  /**
   * Checks if the INFO level is enabled for the logger.
   *
   * @return true if INFO level is enabled
   */
  public static boolean isInfoEnabled() {
    return LOGGER.isLoggable(Level.INFO);
  }

  /**
   * Checks if the SEVERE (ERROR) level is enabled for the logger.
   *
   * @return true if SEVERE (ERROR) level is enabled
   */
  public static boolean isErrorEnabled() {
    return LOGGER.isLoggable(Level.SEVERE);
  }

  /**
   * Checks if the FINE (DEBUG) level is enabled for the logger.
   *
   * @return true if FINE (DEBUG) level is enabled
   */
  public static boolean isDebugEnabled() {
    return LOGGER.isLoggable(Level.FINE);
  }

  /**
   * Checks if the WARNING level is enabled for the logger.
   *
   * @return true if WARNING level is enabled
   */
  public static boolean isWarningEnabled() {
    return LOGGER.isLoggable(Level.WARNING);
  }

  /**
   * Sets verbose mode for logging.
   *
   * @param verbose true to enable verbose logging, false to disable
   */
  public static void setVerbose(final boolean verbose) {
    if (verbose) {
      LOGGER.setLevel(Level.INFO);
    } else {
      LOGGER.setLevel(Level.FINE);
    }
  }

  /**
   * Sets debug mode for logging.
   *
   * @param debug true to enable debug logging, false to leave current logging level
   */
  public static void setDebug(final boolean debug) {
    if (debug) {
      LOGGER.setLevel(Level.FINEST);
    }
  }

  /**
   * Logs an informational message.
   *
   * @param msg the message to log
   */
  public static void info(final String msg) {
    if (LOGGER.isLoggable(Level.INFO)) {
      LOGGER.info(msg);
    }
  }

  /**
   * Logs an informational message with parameters.
   *
   * @param msg the message to log
   * @param params parameters for the message
   */
  public static void info(final String msg, final Object... params) {
    if (LOGGER.isLoggable(Level.INFO)) {
      LOGGER.log(Level.INFO, msg, params);
    }
  }

  /**
   * Logs an error message with an associated exception.
   *
   * @param msg the error message
   * @param t the exception to log
   */
  public static void error(final String msg, final Throwable t) {
    if (LOGGER.isLoggable(Level.SEVERE)) {
      LOGGER.log(Level.SEVERE, msg, t);
    }
  }

  /**
   * Logs an error message with parameters.
   *
   * @param msg the error message
   * @param params parameters for the message
   */
  public static void error(final String msg, final Object... params) {
    if (LOGGER.isLoggable(Level.SEVERE)) {
      LOGGER.log(Level.SEVERE, msg, params);
    }
  }

  /**
   * Logs a debug-level message.
   *
   * @param msg the message to log
   */
  public static void debug(final String msg) {
    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.fine(msg);
    }
  }

  /**
   * Logs a debug-level message with parameters.
   *
   * @param msg the message to log
   * @param params parameters for the message
   */
  public static void debug(final String msg, final Object... params) {
    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.log(Level.FINE, msg, params);
    }
  }

  /**
   * Logs a trace-level message (finest level).
   *
   * @param msg the message to log
   */
  public static void trace(final String msg) {
    LOGGER.finest(msg);
  }

  /**
   * Logs a warning message.
   *
   * @param msg the message to log
   */
  public static void warning(final String msg) {
    if (LOGGER.isLoggable(Level.WARNING)) {
      LOGGER.warning(msg);
    }
  }

  /**
   * Logs a warning message with parameters.
   *
   * @param msg the message to log
   * @param params parameters for the message
   */
  public static void warning(final String msg, final Object... params) {
    if (LOGGER.isLoggable(Level.WARNING)) {
      LOGGER.log(Level.WARNING, msg, params);
    }
  }
}
