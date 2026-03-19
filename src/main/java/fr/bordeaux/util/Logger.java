package fr.bordeaux.util;

import fr.bordeaux.config.ConfigManager;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 * A simple custom logger for the Quoridor project. Handles writing logs to a file and/or the
 * console.
 */
public class Logger {
  /** ANSI escape code to reset text formatting to default. */
  private static final String RESET = "\u001B[0m";

  /** ANSI escape code for Cyan color, used for INFO logs. */
  private static final String CYAN = "\u001B[36m";

  /** ANSI escape code for Yellow color, used for DEBUG logs. */
  private static final String YELLOW = "\u001B[33m";

  /** ANSI escape code for Green color, used for VERBOSE logs. */
  private static final String GREEN = "\u001B[32m";

  /** ANSI escape code for Red color, used for ERROR logs. */
  private static final String RED = "\u001B[31m";

  /** The unique instance of the Logger (Singleton pattern). */
  private static Logger instance;

  /** The output stream where logs are written. Defaults to System.out. */
  private PrintStream writer = System.out;

  /** Flag indicating if debug-level logging is enabled. */
  private boolean debug;

  /** Flag indicating if verbose-level logging is enabled. */
  private boolean verbose;

  /**
   * Private constructor for file-based logging. Initializes settings from {@link ConfigManager} and
   * attempts to create a log file in the user's home directory.
   *
   * @param logFile The name of the file where logs should be saved.
   */
  private Logger(String logFile) {
    try {
      File fileWriter = new File(System.getProperty("user.home") + File.separator + logFile);
      this.writer = new PrintStream(fileWriter);
      String debugger = ConfigManager.getInstance().getOption("debug", "true");
      this.debug = debugger.contains("true") ? true : false;
      String verboser = ConfigManager.getInstance().getOption("verbose", "true");
      this.verbose = verboser.contains("true") ? true : false;
    } catch (IOException e) {
      writer.println(e);
    }
  }

  /**
   * Private constructor for console-based logging. Initializes settings from {@link ConfigManager}
   * and sets output to System.out.
   */
  private Logger() {
    this.writer = System.out;
    String debugger = ConfigManager.getInstance().getOption("debug", "true");
    this.debug = debugger.contains("true") ? true : false;
    String verboser = ConfigManager.getInstance().getOption("verbose", "true");
    this.verbose = verboser.contains("true") ? true : false;
  }

  /**
   * Provides access to the Singleton instance. If no instance exists, it creates one using the
   * default console constructor.
   *
   * @return The unique instance of the Logger.
   */
  public static Logger getInstance() {
    if (instance == null) {
      instance = new Logger();
    }
    return instance;
  }

  /**
   * Provides access to the Singleton instance with file output. If no instance exists, it creates
   * one targeting the specified file.
   *
   * @param logFile The filename for the log output.
   * @return The unique instance of the Logger.
   */
  public static Logger getInstance(String logFile) {
    if (instance == null) {
      instance = new Logger(logFile);
    }
    return instance;
  }

  /**
   * Logs an informational message.
   *
   * @param message The message to be logged.
   */
  public void info(String message) {
    log("[INFO]", message);
  }

  /**
   * Logs an error message along with the exception details.
   *
   * @param message The error description.
   * @param e The exception associated with the error.
   */
  public void error(String message, Exception e) {
    log("[ERROR]", message + " | Exception: " + e.getMessage());
  }

  /**
   * Logs a simple error message.
   *
   * @param message The error message to be logged.
   */
  public void error(String message) {
    log("[ERROR]", message);
  }

  /**
   * Logs a debug message if the "debug" option is enabled in the configuration. Re-checks
   * configuration settings before logging.
   *
   * @param message The debug message.
   */
  public void debug(String message) {
    String debugger = ConfigManager.getInstance().getOption("debug", "true");
    debug = debugger.contains("true") ? true : false;
    if (debug) {
      log("[DEBUG]", message);
    }
  }

  /**
   * Logs a verbose message if the "verbose" option is enabled in the configuration. Re-checks
   * configuration settings before logging.
   *
   * @param message The detailed verbose message.
   */
  public void verbose(String message) {
    String verboser = ConfigManager.getInstance().getOption("verbose", "true");
    verbose = verboser.contains("true") ? true : false;
    if (verbose) {
      log("[VERBOSE]", message);
    }
  }

  /**
   * Core internal method that formats and writes the log message to the defined {@code writer}.
   * Applies ANSI colors based on the log level.
   *
   * @param level The log level prefix (e.g., [INFO], [ERROR]).
   * @param message The actual content of the log.
   */
  private void log(String level, String message) {
    // Write to output
    if (writer != null) {
      String color;
      switch (level) {
        case "[INFO]":
          color = CYAN;
          break;
        case "[DEBUG]":
          color = YELLOW;
          break;
        case "[VERBOSE]":
          color = GREEN;
          break;
        case "[ERROR]":
          color = RED;
          break;
        default:
          color = RESET;
          break;
      }
      writer.println(color + level + RESET + " " + message);
    }
  }
}
