package fr.bordeaux.util;


import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for logging application events, error messages, and debug information. This class
 * provides a centralized mechanism to output runtime information to the console or persistent log
 * files.
 */
public class Log {

    public static final Logger logger = Logger.getLogger("Quoridor");

    static {
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        logger.addHandler(consoleHandler);
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.INFO);
    }

  /** Constructs a new Logger instance. */
  public Log() {}

    public static void setVerbose(boolean verbose) {
      if (verbose) {
          logger.setLevel(Level.INFO);
      }
      else  {
          logger.setLevel(Level.FINE);
      }
    }

    public static void setDebug(boolean debug) {
      if (debug) {
          logger.setLevel(Level.FINEST);
      }
    }

    public static void info(String msg) { logger.info(msg); }
    public static void error(String msg, Throwable t) { logger.log(Level.SEVERE, msg, t); }
    public static void debug(String msg) { logger.fine(msg); }
    public static void trace(String msg) { logger.finest(msg); }
    public static void warning(String msg) { logger.warning(msg); }

}
