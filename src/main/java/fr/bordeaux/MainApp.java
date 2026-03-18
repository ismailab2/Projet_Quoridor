package fr.bordeaux;

import fr.bordeaux.cli.CliApp;
import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.core.*;
import fr.bordeaux.gui.QuoridorApp;

/** Entry point of the Quoridor application. */
public class MainApp {

  /** 
  * Private constructor to prevent instantiation of this utility class.
  */
  private MainApp() {}

  private static final String VERSION = "1.0.0";
  /** The standard size of the Quoridor board (9x9). */
  public static final int BOARD_SIZE = 9;
  /** The supported number of players for this version. */
  public static final int PLAYER_SIZE = 2;

  /**
   * Main entry point. Parses command line arguments and launches either the GUI or CLI.
   *
   * @param args Command line arguments.
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
            printHelp();
            System.exit(1);
          }
          break;

        default:
          System.err.println("Erreur : option invalide -> " + arg);
          printHelp();
          System.exit(1);
      }
    }

    if (gui && cli) {
      System.err.println("Erreur : --gui et --cli sont incompatibles.");
      printHelp();
      System.exit(1);
    }

    if (!gui && !cli) {
      cli = true;
    }

    boolean verbose = config.getBoolean("verbose", false);
    boolean debug = config.getBoolean("debug", false);
    boolean blitz = config.getBoolean("blitz", false);
    int timeout = config.getInt("timeout", 30);

    if (verbose) {
      System.out.println("Mode verbeux activé");
    }

    if (debug) {
      System.err.println("Mode debug activé");
    }

    if (blitz) {
      System.out.println("Mode blitz : " + timeout + " minutes");
    }

    try {

      if (gui) {
        QuoridorApp.main(args);
      }

      if (cli) {
        Board board = new Board(BOARD_SIZE);

        GameState state;
        if (PLAYER_SIZE == 2) {
          state = new GameState(board, Player.createDefaultPlayers());
        } else {
          System.err.println("Erreur : nombre de joueurs invalide.");
          System.exit(1);
          return;
        }

        GameEngine engine = new GameEngine(state);
        CliApp cliApp = new CliApp(engine);
        cliApp.run();
      }

      System.exit(0);

    } catch (Exception e) {
      System.err.println("Erreur d'exécution : " + e.getMessage());
      System.exit(1);
    }
  }

  /** print the help to use programme */
  private static void printHelp() {

    System.out.println("Usage: qoridor [OPTIONS]");
    System.out.println();

    System.out.println("Options générales :");
    System.out.println("  -h, --help        Affiche l'aide");
    System.out.println("  -V, --version     Affiche la version");
    System.out.println("  -v, --verbose     Mode verbeux");
    System.out.println("  -d, --debug       Mode debug");

    System.out.println();
    System.out.println("Modes :");
    System.out.println("  -g, --gui         Interface graphique");
    System.out.println("  -c, --cli         Mode ligne de commande (défaut)");

    System.out.println();
    System.out.println("Options blitz :");
    System.out.println("  -b, --blitz       Active le mode blitz");
    System.out.println("  -t, --time N      Temps en minutes (défaut 30)");
  }
}
