package fr.bordeaux;

import fr.bordeaux.cli.CliApp;
import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.core.Board;
import fr.bordeaux.core.GameEngine;
import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Player;
import fr.bordeaux.gui.QuoridorApp;
import fr.bordeaux.util.Log;

/** Entry point of the Quoridor application. */
public class MainApp {

  /** Version number of the Quoridor application. */
  private static final String VERSION = "1.0.0";

  /** Size of the game board (number of rows/columns). */
  public static final int BOARD_SIZE = 9;

  /** Number of players in the game. */
  public static final int PLAYER_SIZE = 2;

  private MainApp() {}

  /**
   * Main method. Entry point of the application. Handles command-line arguments, initializes
   * logging, and launches GUI or CLI.
   *
   * @param args Command-line arguments
   */
  public static void main(String[] args) {
    ConfigManager config = ConfigManager.getInstance();
    config.load();
    boolean gui = false;
    boolean cli = false;

    for (int i = 0; i < args.length; i++) {
      String arg = args[i];
      switch (arg) {
        case "-h", "--help" -> {
          printHelp();
          System.exit(0);
        }
        case "-V", "--version" -> {
          Log.info("Quoridor version " + VERSION);
          System.exit(0);
        }
        case "-v", "--verbose" -> config.setOption("verbose", "true");
        case "-d", "--debug" -> config.setOption("debug", "true");
        case "-g", "--gui" -> gui = true;
        case "-c", "--cli" -> cli = true;
        case "-b", "--blitz" -> config.setOption("blitz", "true");
        case "-t", "--time" -> {
          if (i + 1 < args.length) {
            String value = args[++i];
            config.setOption("timeout", value);
          } else {
            Log.error("Erreur : --time nécessite une valeur.", null);
            printHelp();
            System.exit(1);
          }
        }
        default -> {
          Log.error("Erreur : option invalide -> " + arg, null);
          printHelp();
          System.exit(1);
        }
      }
    }

    if (gui && cli) {
      Log.error("Erreur : --gui et --cli sont incompatibles.", null);
      printHelp();
      System.exit(1);
    }

    if (!gui && !cli) cli = true;

    boolean verbose = config.getBoolean("verbose", false);
    boolean debug = config.getBoolean("debug", false);
    boolean blitz = config.getBoolean("blitz", false);
    int timeout = config.getInt("timeout", 30);

    Log.setVerbose(verbose);
    Log.setDebug(debug);

    if (verbose) Log.info("Mode verbeux activé");
    if (debug) Log.debug("Mode debug activé");
    if (blitz) Log.info("Mode blitz : " + timeout + " minutes");

    try {
      if (gui) {
        Log.info("Lancement GUI");
        QuoridorApp.main(args);
      }

      if (cli) {
        Log.info("Lancement CLI");
        Board board = new Board(BOARD_SIZE);
        GameState state;
        if (PLAYER_SIZE == 2) {
          state = new GameState(board, Player.createDefaultPlayers());
        } else {
          Log.error("Erreur : nombre de joueurs invalide.", null);
          System.exit(1);
          return;
        }
        GameEngine engine = new GameEngine(state);
        try {
          CliApp cliApp = new CliApp(engine);
          cliApp.run();
        } catch (Exception e) {
          Log.error("Erreur dans CLI", e);
        }
      }

      System.exit(0);

    } catch (Exception e) {
      Log.error("Erreur d'exécution", e);
      System.exit(1);
    }
  }

  /** Prints the command-line help information. */
  private static void printHelp() {
    Log.info("Usage: qoridor [OPTIONS]\n");
    Log.info("Options générales :");
    Log.info("  -h, --help        Affiche l'aide");
    Log.info("  -V, --version     Affiche la version");
    Log.info("  -v, --verbose     Mode verbeux");
    Log.info("  -d, --debug       Mode debug\n");

    Log.info("Modes :");
    Log.info("  -g, --gui         Interface graphique");
    Log.info("  -c, --cli         Mode ligne de commande (par défaut)\n");

    Log.info("Options blitz :");
    Log.info("  -b, --blitz       Active le mode blitz");
    Log.info("  -t, --time N      Temps en minutes (par défaut 30)");
  }
}
