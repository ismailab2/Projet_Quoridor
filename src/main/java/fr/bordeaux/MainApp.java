package fr.bordeaux;

import fr.bordeaux.cli.CliApp;
import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.core.Board;
import fr.bordeaux.core.GameEngine;
import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Player;
import fr.bordeaux.gui.QuoridorApp;
import fr.bordeaux.i18n.I18n;
import fr.bordeaux.util.Logger;
import java.util.Locale;

/** Entry point of the Quoridor application. */
public class MainApp {

  private static final Logger LOGGER = Logger.getInstance();

  private static final String VERSION = "1.0.0";

  /** Size of the game board (number of rows/columns). */
  public static final int BOARD_SIZE = 9;

  /** Number of players in the game. */
  public static final int PLAYER_SIZE = 2;

  /** Private constructor to prevent instantiation of this utility class. */
  private MainApp() {}

  /**
   * Main method. Entry point of the application. Handles command-line arguments, initializes
   * logging, and launches GUI or CLI.
   *
   * @param args Command-line arguments
   */
  public static void main(String[] args) {

    LOGGER.info(I18n.get("test"));
    I18n.setLocale(Locale.ENGLISH);
    LOGGER.info(I18n.get("test"));

    ConfigManager config = ConfigManager.getInstance();
    config.load();
    LOGGER.debug(I18n.get("verbose")+" "+I18n.get("mode")+": " + config.getOption("verbose", "default"));

    boolean gui = false;
    boolean cli = false;

    for (int i = 0; i < args.length; i++) {
      String arg = args[i];
      switch (arg) {
        case "-h":
        case "--help":
          printHelp();
          System.exit(0);
        case "-V":
        case "--version":
          LOGGER.info(I18n.get("quoridorVersion")+": " + VERSION);
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
            LOGGER.error(" --time "+I18n.get("needsValue"));
            printHelp();
            System.exit(1);
          }
          break;
        default:
          LOGGER.error(I18n.get("invalidOpt")+" -> " + arg);
          printHelp();
          System.exit(1);
      }
    }

    if (gui && cli) {
      LOGGER.error(" --gui & --cli "+I18n.get("incompatibleOpt"));
      printHelp();
      System.exit(1);
    }

    if (!gui && !cli) cli = true;

    boolean verbose = config.getBoolean("verbose", false);
    boolean debug = config.getBoolean("debug", false);
    boolean blitz = config.getBoolean("blitz", false);
    int timeout = config.getInt("timeout", 30);

    if (verbose) {
      LOGGER.info(I18n.get("mode", "verbose", "activ"));
    }

    if (debug) {
      LOGGER.info(I18n.get("mode", "debug", "activ"));
    }

    if (blitz) {
      LOGGER.info(I18n.get("mode")+" blitz : " + timeout + " minutes");
    }

    try {
      if (gui) {
        LOGGER.debug(I18n.get("launching")+": GUI");
        QuoridorApp.main(args);
      }

      if (cli) {
        LOGGER.debug(I18n.get("launching")+": CLI");
        Board board = new Board(BOARD_SIZE);
        GameState state;
        if (PLAYER_SIZE == 2 || PLAYER_SIZE == 4) {
          state = new GameState(board, Player.createDefaultPlayers());
        } else {
          LOGGER.error(" Invalid number of players.");
          System.exit(1);
          return;
        }
        GameEngine engine = new GameEngine(state);
        try {
          CliApp cliApp = new CliApp(engine);
          cliApp.run();
        } catch (Exception e) {
          LOGGER.error("CLI. Cause: " + e.getClass().getSimpleName());
        }
      }

      System.exit(0);

    } catch (Exception e) {
      LOGGER.error(" Brutal interruption. Cause: " + e.getClass().getSimpleName());
      System.exit(1);
    }
  }

  /** Prints the command-line help information. */
  private static void printHelp() {
    LOGGER.info("Usage: qoridor [OPTIONS]");
    LOGGER.info("");

    LOGGER.info("Options générales :");
    LOGGER.info("  -h, --help        Affiche l'aide");
    LOGGER.info("  -V, --version     Affiche la version");
    LOGGER.info("  -v, --verbose     Mode verbeux");
    LOGGER.info("  -d, --debug       Mode debug");

    LOGGER.info("");
    LOGGER.info("Modes :");
    LOGGER.info("  -g, --gui         Interface graphique");
    LOGGER.info("  -c, --cli         Mode ligne de commande (défaut)");

    LOGGER.info("");
    LOGGER.info("Options blitz :");
    LOGGER.info("  -b, --blitz       Active le mode blitz");
    LOGGER.info("  -t, --time N      Temps en minutes (défaut 30)");
  }
}
