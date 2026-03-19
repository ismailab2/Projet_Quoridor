package fr.bordeaux.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import fr.bordeaux.config.ConfigManager;

/**
 * A simple custom logger for the Quoridor project.
 * Handles writing logs to a file and/or the console.
 */
public class Logger {
    // ANSI codes for colors
    private static final String RESET = "\u001B[0m";
    private static final String CYAN = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static Logger instance;
    private PrintStream writer = System.out;
    private boolean debug;
    private boolean verbose;

    private Logger(String logFile) {
        try {
            File fileWriter = new File(System.getProperty("user.home") + File.separator + logFile);
            this.writer = new PrintStream(fileWriter);
            String debugger = ConfigManager.getInstance().getOption("debug", "true");
            this.debug = debugger.contains("true")?true:false;
            String verboser = ConfigManager.getInstance().getOption("verbose", "true");
            this.verbose = verboser.contains("true")?true:false;
        } catch (IOException e) {
            writer.println(e);
        }
    }

    private Logger() {
        this.writer = System.out;
        String debugger = ConfigManager.getInstance().getOption("debug", "true");
        this.debug = debugger.contains("true")?true:false;
        String verboser = ConfigManager.getInstance().getOption("verbose", "true");
        this.verbose = verboser.contains("true")?true:false;
    }

    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    public static Logger getInstance(String logFile) {
        if (instance == null) {
            instance = new Logger(logFile);
        }
        return instance;
    }

    public void info(String message) {
        log("[INFO]", message);
    }

    public void error(String message, Exception e) {
        log("[ERROR]", message + " | Exception: " + e.getMessage());
    }

    public void error(String message) {
        log("[ERROR]", message);
    }

    public void debug(String message) {
        String debugger = ConfigManager.getInstance().getOption("debug", "true");
        debug = debugger.contains("true")?true:false;
        if (debug) {
            log("[DEBUG]", message);
        }
    }

    public void verbose(String message) {
        String verboser = ConfigManager.getInstance().getOption("verbose", "true");
        verbose = verboser.contains("true")?true:false;
        if (verbose) {
            log("[VERBOSE]", message);
        }
    }

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