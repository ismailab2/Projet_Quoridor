package fr.bordeaux.util;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for logging application events, error messages, and debug information. Provides a
 * centralized mechanism to output runtime information to the console or log files.
 */
public class Log {

  /** Centralized Logger instance for the Quoridor application. */
  public static final Logger logger = Logger.getLogger("Quoridor");

  static {
    ConsoleHandler consoleHandler = new ConsoleHandler();
    consoleHandler.setLevel(Level.ALL);
    logger.addHandler(consoleHandler);
    logger.setUseParentHandlers(false);
    logger.setLevel(Level.INFO);
  }

  /** Constructs a new Log instance. */
  public Log() {}

  /**
   * Sets verbose mode. In verbose mode, info-level messages are logged; otherwise, only fine-level
   * messages.
   *
   * @param verbose true to enable verbose logging, false to disable
   */
  public static void setVerbose(boolean verbose) {
    if (verbose) {
      logger.setLevel(Level.INFO);
    } else {
      logger.setLevel(Level.FINE);
    }
  }

  /**
   * Sets debug mode. In debug mode, trace-level messages are logged.
   *
   * @param debug true to enable debug logging, false to leave current logging level
   */
  public static void setDebug(boolean debug) {
    if (debug) {
      logger.setLevel(Level.FINEST);
    }
  }

  /**
   * Logs an informational message.
   *
   * @param msg the message to log
   */
  public static void info(String msg) {
    logger.info(msg);
  }

  /**
   * Logs an error message with an associated exception.
   *
   * @param msg the error message
   * @param t the exception to log
   */
  public static void error(String msg, Throwable t) {
    logger.log(Level.SEVERE, msg, t);
  }

  /**
   * Logs a debug-level message.
   *
   * @param msg the message to log
   */
  public static void debug(String msg) {
    logger.fine(msg);
  }

  /**
   * Logs a trace-level message (finest level).
   *
   * @param msg the message to log
   */
  public static void trace(String msg) {
    logger.finest(msg);
  }

  /**
   * Logs a warning message.
   *
   * @param msg the message to log
   */
  public static void warning(String msg) {
    logger.warning(msg);
  }
}
