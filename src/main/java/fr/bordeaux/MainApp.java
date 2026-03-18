package fr.bordeaux;

import fr.bordeaux.cli.CliApp;
import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.core.*;
import fr.bordeaux.gui.QuoridorApp;

/**
 * Entry point of the Quoridor application. This class handles command-line argument parsing,
 * configuration loading, and initializes either the Graphical User Interface (GUI) or the Command
 * Line Interface (CLI) based on user input.
 */
public class MainApp {

  /** Private constructor to prevent instantiation of the main application class. */
  private MainApp() {
    // This class is not intended to be instantiated.
  }

  private static final String VERSION = "1.0.0";

  /**
   * Main entry point of the program. Parses command-line arguments to set game modes (GUI, CLI,
   * Blitz) and configures global settings like verbosity and debug mode.
   *
   * @param args Command-line arguments provided at startup.
   */
  public static void main(String[] args) {
    // Logger configurations before using
    // Display only message level (ex: [INFO] Message / [DEBUG] Message)
    System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
    System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
    System.setProperty("org.slf4j.simpleLogger.showDateTime", "false");
    System.setProperty(
        "org.slf4j.simpleLogger.showThreadName", "false"); // Hide file of main() method
    System.setProperty("org.slf4j.simpleLogger.showLogName", "false"); // Hide class name
    System.setProperty("org.slf4j.simpleLogger.showShortLogName", "false");
    System.setProperty("org.slf4j.simpleLogger.levelInBrackets", "true");

    ConfigManager config = ConfigManager.getInstance();

    config.load();
    System.out.println(config.getOption("verbose", "default"));

    boolean gui = false;
    boolean cli = true;

    if (args.length == 0) {
      System.out.println("Le programme se lance...");
    }

    for (int i = 0; i < args.length; i++) {

      String arg = args[i];

      switch (arg) {
        case "-h":
        case "--help":
          printHelp();
          System.exit(0);

        case "-V":
        case "--version":
          System.out.println("Qoridor version " + VERSION);
          System.exit(0);

        case "-v":
        case "--verbose":
          config.setOption("verbose", "true");
          break;

        case "-d":
        case "--debug":
          config.setOption("debug", "true");
          break;

        case "-g":
        case "--gui":
          gui = true;
          break;

        case "-c":
        case "--cli":
          cli = true;
          break;

        case "-b":
        case "--blitz":
          config.setOption("blitz", "true");
          break;

        case "-t":
        case "--time":
          if (i + 1 < args.length) {

            String value = args[++i];
            config.setOption("timeout", value);

          } else {

            System.err.println("Erreur : --time nécessite une valeur.");
            System.exit(1);
          }
          break;

        default:
          System.err.println("Option invalide : " + arg);
          printHelp();
          System.exit(1);
      }
    }

    boolean verbose = config.getBoolean("verbose", false);
    boolean debug = config.getBoolean("debug", false);
    boolean blitz = config.getBoolean("blitz", false);
    int timeout = config.getInt("timeout", 30);

    if (verbose) {
      System.out.println("Verbose mode activé");
    }

    if (debug) {
      System.out.println("Debug mode activé");
    }

    if (blitz) {
      System.out.println("Mode blitz : " + timeout + " minutes");
    }

    if (gui) {
      QuoridorApp.main(args);
    }

    if (cli) {
      System.out.println("CLI mode started");
      int size = 9;
      Board board = new Board(size);
      GameState state = new GameState(board, Player.createDefaultPlayers());
      GameEngine engine = new GameEngine(state);
      try {
        CliApp cli1 = new CliApp(engine);
        cli1.run();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Displays the help message in the console. Lists all available options and command-line switches
   * for the user.
   */
  private static void printHelp() {

    System.out.println("Usage: qoridor [OPTIONS]");
    System.out.println();

    System.out.println("Options générales :");
    System.out.println("  -h --help        Affiche l'aide");
    System.out.println("  -V --version     Affiche la version");
    System.out.println("  -v --verbose     Mode verbeux");
    System.out.println("  -d --debug       Mode debug");

    System.out.println();
    System.out.println("Modes :");
    System.out.println("  -g --gui         Interface graphique");
    System.out.println("  -b --blitz       Mode blitz");

    System.out.println();
    System.out.println("Options blitz :");
    System.out.println("  -t --time N      Temps en minutes (défaut 30)");
  }
}
