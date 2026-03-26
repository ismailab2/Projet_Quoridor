package fr.bordeaux;

import fr.bordeaux.cli.CliApp;
import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.core.Board;
import fr.bordeaux.core.GameEngine;
import fr.bordeaux.core.GameState;
import fr.bordeaux.factories.*;
import fr.bordeaux.gui.QuoridorApp;
import fr.bordeaux.util.Logger;
import java.io.IOException;

/** Entry point of the Quoridor application. */
public final class MainApp {

  /** Logger principal de l'application */
  private static final Logger LOGGER = Logger.getInstance();

  /** Version de l'application */
  private static final String VERSION = "1.0.0";

  /** Taille du plateau */
  public static final int BOARD_SIZE = 9;

  /** Nombre de joueurs */
  public static final int PLAYER_SIZE = 2;

  /** Constructeur privé (classe utilitaire) */
  private MainApp() {}

  /**
   * Main method.
   *
   * @param args arguments de la ligne de commande
   */
  public static void main(final String[] args) {
    // initI18n();

    final ConfigManager config = loadConfig();
    final AppMode mode = parseArgs(args, config);
    validateMode(mode, config);
    applyConfigLogs(config);
    runApplication(mode, args, config);
  }

  /*
   private static void initI18n() {
     if (LOGGER.isInfoEnabled()) {
       LOGGER.info(I18n.get("test"));
     }
     //I18n.setLocale(Locale.ENGLISH);
   }

  */

  private static ConfigManager loadConfig() {
    final ConfigManager config = ConfigManager.getInstance();
    config.load();

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Configuration loaded");
    }

    return config;
  }

  private static AppMode parseArgs(final String[] args, final ConfigManager config) {
    boolean gui = false;
    boolean cli = false;

    for (int i = 0; i < args.length; i++) {
      final String arg = args[i];

      switch (arg) {
        case "-h":
        case "--help":
          printHelp();
          System.exit(0);
          break;

        case "-V":
        case "--version":
          logInfo("Quoridor version " + VERSION);
          System.exit(0);
          break;

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

        case "--ai":
          config.setOption("ai", "true");
          break;

        case "--ai-mode":
          i++;
          if (i < args.length) config.setOption("ai.mode", args[i]);
          break;

        case "--ai-minimax-depth":
          i++;
          if (i < args.length) config.setOption("ai.depth", args[i]);
          break;

        case "--ai-time":
          i++;
          if (i < args.length) config.setOption("ai.time", args[i]);
          break;

        case "--ai-minimax-scoring":
          i++;
          if (i < args.length) config.setOption("ai.scoring", args[i]);
          break;

        case "-b":
        case "--blitz":
          config.setOption("blitz", "true");
          break;

        case "-t":
        case "--time":
          i++;
          handleTimeArgument(args, i, config);
          break;

        default:
          logError("Option invalide -> " + arg);
          printHelp();
          System.exit(1);
          break;
      }
    }

    return new AppMode(gui, cli);
  }

  private static void handleTimeArgument(
      final String[] args, final int index, final ConfigManager config) {

    final boolean blitz = config.getBoolean("blitz", false);
    if (!blitz) {
      logInfo("--time nécessite d'activer le mode blitz. Il a donc été ignoré.");
    }
    if (index < args.length) {
      final String value = args[index];
      config.setOption("timeout", value);
    } else {
      logError("--time nécessite une valeur.");
      printHelp();
      System.exit(1);
    }
  }

  private static void validateMode(final AppMode mode, final ConfigManager config) {
    if (mode.gui() && mode.cli()) {
      logError("--gui et --cli sont incompatibles.");
      printHelp();
      System.exit(1);
    }

    final boolean useAi = config.getBoolean("ai", false);
    final boolean hasAiOptions =
        config.getOption("ai.depth", null) != null
            || config.getOption("ai.mode", null) != null
            || config.getOption("ai.time", null) != null
            || config.getOption("ai.scoring", null) != null;

    if (!useAi && hasAiOptions) {
      logInfo(
          "Avertissement : Des options d'IA ont été fournies mais le flag --ai n'est pas présent. Elles seront ignorées.");
    }
  }

  private static void applyConfigLogs(final ConfigManager config) {
    final boolean verbose = config.getBoolean("verbose", false);
    final boolean debug = config.getBoolean("debug", false);
    final boolean blitz = config.getBoolean("blitz", false);
    final int timeout = config.getInt("timeout", 30);

    if (verbose) {
      logInfo("Mode verbeux activé");
    }

    if (debug) {
      logInfo("Mode debug activé");
    }

    if (blitz) {
      logInfo("Mode blitz : " + timeout + " minutes");
    }
  }

  private static void runApplication(
      final AppMode mode, final String[] args, final ConfigManager config) {

    try {
      if (mode.gui()) {
        runGui(args);
      } else {
        runCli(config);
      }
      System.exit(0);
    } catch (IllegalStateException e) {
      logError("Erreur interne : " + e.getMessage());
      System.exit(1);
    }
  }

  private static void runGui(final String[] args) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Lancement GUI");
    }
    QuoridorApp.main(args);
  }

  private static void runCli(final ConfigManager config) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Lancement CLI");
    }

    final Board board = new Board(BOARD_SIZE);
    final GameState state = createGameState(board, config);

    final boolean isBlitz = config.getBoolean("blitz", false);
    state.setBlitz(isBlitz);

    if (isBlitz) {
      final int timeoutMinutes = config.getInt("timeout", 30);
      final long timeoutMillis = timeoutMinutes * 60 * 1000L;
      state.setLimitTimePlayer(timeoutMillis);
    }

    final GameEngine engine = new GameEngine(state);

    try {
      final CliApp cliApp = new CliApp(engine);
      cliApp.run();
    } catch (IllegalStateException | IOException e) {
      logError("Erreur CLI : " + e.getMessage());
    }
  }

  private static GameState createGameState(final Board board, final ConfigManager config) {
    if (PLAYER_SIZE == 2 || PLAYER_SIZE == 4) {
      return new GameState(board, PlayerFactory.createPlayers(config));
    }

    logError("Nombre de joueurs invalide.");
    System.exit(1);
    throw new IllegalStateException("Invalid player count");
  }

  private static void logInfo(final String message) {
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info(message);
    }
  }

  private static void logError(final String message) {
    if (LOGGER.isErrorEnabled()) {
      LOGGER.error(message);
    }
  }

  private static void printHelp() {
    logInfo("Usage: qoridor [OPTIONS]");
    logInfo("");

    logInfo("Options générales :");
    logInfo("  -h, --help        Affiche l'aide");
    logInfo("  -V, --version     Affiche la version");
    logInfo("  -v, --verbose     Mode verbeux");
    logInfo("  -d, --debug       Mode debug");

    logInfo("");
    logInfo("Modes :");
    logInfo("  -g, --gui         Interface graphique");
    logInfo("  -c, --cli         Mode CLI (défaut)");

    logInfo("");
    logInfo("Options blitz :");
    logInfo("  -b, --blitz       Active le mode blitz");
    logInfo("  -t, --time N      Temps en minutes (défaut 30)");
  }

  /** Représente le mode d'exécution. */
  private record AppMode(boolean gui, boolean cli) {}
}
