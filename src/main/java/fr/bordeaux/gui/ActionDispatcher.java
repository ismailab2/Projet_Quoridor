package fr.bordeaux.gui;

import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.core.GameEngine;
import fr.bordeaux.core.Player;
import fr.bordeaux.main.PlayerFactory;
import fr.bordeaux.network.GameClient;
import fr.bordeaux.network.MultiGameServer;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;

/**
 * Dispatches actions from the GUI to the GameEngine or other dialogs.
 *
 * <p>This class serves as a bridge between GUI events (menu items, shortcuts) and the underlying
 * game logic or dialogs. Each action corresponds to a method in the GameEngine or a GUI dialog.
 */
public class ActionDispatcher {

  /** Reference to the game engine to execute game-related actions */
  private final GameEngine engine;

  /** The file handler for loading and saving games. */
  private final FileHandler fileHandler;

  /** The configuration manager for game settings. */
  private final ConfigManager configManager = ConfigManager.getInstance();

  /** The server instance for multiplayer games. */
  private final MultiGameServer multiServer = new MultiGameServer();

  /** The client instance for multiplayer games. */
  private final GameClient networkClient = new GameClient();

  /** Default TCP port for network games. */
  private static final int DEFAULT_PORT = 12_345;

  /** Number of milliseconds in a second. */
  private static final long MS_PER_SEC = 1000L;

  /** Number of seconds in a minute. */
  private static final int SEC_PER_MIN = 60;

  /**
   * Constructs an ActionDispatcher with a given GameEngine.
   *
   * @param engine The GameEngine instance to control the game.
   * @param fileHandler The FileHandler used to save and load.
   */
  public ActionDispatcher(final GameEngine engine, final FileHandler fileHandler) {
    this.engine = engine;
    this.fileHandler = fileHandler;
  }

  /**
   * Dispatches a given action to the appropriate handler.
   *
   * <p>Depending on the action type, this method may:
   *
   * <ul>
   *   <li>Call GameEngine methods (newGame, loadGame, saveGame, undo, redo, pause, hint)
   *   <li>Show GUI dialogs (configuration, info)
   *   <li>Exit the application
   * </ul>
   *
   * @param action The Action enum representing the user command.
   * @throws IOException If an I/O error occurs during command execution.
   */
  public void dispatch(final Action action) throws IOException {
    switch (action) {
      case NEW_GAME -> handleNewGame();
      case LOAD_GAME -> handleLoadGame();
      case SAVE_GAME -> handleSaveGame();
      case CONFIGURATION -> ConfigDialog.show();
      case INFO -> InfoDialog.show();
      case REDO -> engine.redo();
      case UNDO -> engine.undo();
      case PAUSE -> engine.pause();
      case HINT -> engine.hint();
      case QUIT -> Platform.exit();
      default -> handleNetworkAction(action);
    }
  }

  private void handleLoadGame() throws IOException {
    final String path = fileHandler.chooseLoadFile();
    if (path != null) {
      engine.loadGame(path);
    }
  }

  private void handleSaveGame() throws IOException {
    final String path = fileHandler.chooseSaveFile();
    if (path != null) {
      engine.saveGame(path);
    }
  }

  private void handleNetworkAction(final Action action) {
    switch (action) {
      case START_SERVER -> startServer();
      case STOP_SERVER -> {
        multiServer.stop();
        showMessage("Network", "Server", "Server stopped.");
      }
      case SERVER_LIST -> {
        networkClient.serverList();
        showMessage(
            "Network",
            "Server List",
            "Server discovery started. Check the console for discovered servers.");
      }
      case SERVER_STATUS -> showMessage("Network", "Server Status", multiServer.getServerStatus());
      case PLAYERS -> showMessage("Network", "Connected Players", multiServer.listPlayers());
      case SCOREBOARD -> showMessage("Network", "Scoreboard", multiServer.getScoreboard());
      case NEW_NETWORK_GAME -> createNetworkGame();
      case JOIN_SERVER -> joinServer();
      case SET_NETWORK_NAME -> setNetworkName();
      case PING_SERVER -> networkClient.ping();
      case QUIT_SERVER -> networkClient.quit();
      default -> {}
    }
  }

  private void handleNewGame() {
    final int size = configManager.getInt("board-size", 9);
    final List<Player> players = PlayerFactory.createPlayers(configManager);
    final boolean isBlitz = configManager.getBoolean("blitz", false);
    final Long timeout;
    if (isBlitz) {
      final int timeoutMinutes = configManager.getInt("timeout", 30);
      timeout = (long) timeoutMinutes * SEC_PER_MIN * MS_PER_SEC;
    } else {
      timeout = null;
    }
    engine.newGame(size, players, timeout);
  }

  private void startServer() {
    final String input =
        promptUser(
            "Start Server", "Server Port", "Enter the TCP port:", String.valueOf(DEFAULT_PORT));
    if (input == null) {
      return;
    }

    final int port = Integer.parseInt(input.trim());
    multiServer.start(port);
    showMessage("Network", "Server", "Server started on port " + port + ".");
  }

  private void createNetworkGame() {
    final String input =
        promptUser(
            "New Network Game",
            "Player IDs",
            "Enter 2 or 4 player IDs separated by spaces:",
            "C1 C2");
    if (input == null || input.isBlank()) {
      return;
    }

    final String[] ids = input.trim().split("\\s+");
    showMessage("Network", "New Game", multiServer.createGame(ids));
  }

  private void joinServer() {
    final String input =
        promptUser(
            "Join Server", "Host and Port", "Enter host[:port]:", "localhost:" + DEFAULT_PORT);
    if (input == null || input.isBlank()) {
      return;
    }

    final String[] hostPort = input.trim().split(":");
    final String host = hostPort[0];
    final int port;
    if (hostPort.length > 1) {
      port = Integer.parseInt(hostPort[1]);
    } else {
      port = DEFAULT_PORT;
    }

    if (networkClient.join(host, port)) {
      showMessage("Network", "Join Server", "Connected to " + host + ":" + port + ".");
    } else {
      showMessage("Network", "Join Server", "Failed to connect to " + host + ":" + port + ".");
    }
  }

  private void setNetworkName() {
    final String input =
        promptUser("Set Player Name", "Player Name", "Enter your player name:", "");
    if (input == null || input.isBlank()) {
      return;
    }

    networkClient.hello(input.trim());
    showMessage("Network", "Player Name", "Name sent to server.");
  }

  /**
   * Prompts the user for a text input using a JavaFX TextInputDialog.
   *
   * @param title the title of the dialog
   * @param header the header text of the dialog
   * @param content the content text of the dialog
   * @param defaultValue the default value to display in the text field
   * @return the user input as a String, or null if the user cancelled
   */
  protected String promptUser(
      final String title, final String header, final String content, final String defaultValue) {
    final TextInputDialog dialog = new TextInputDialog(defaultValue);
    dialog.setTitle(title);
    dialog.setHeaderText(header);
    dialog.setContentText(content);
    final Optional<String> result = dialog.showAndWait();
    return result.orElse(null);
  }

  /**
   * Displays an information message to the user using a JavaFX Alert.
   *
   * @param title the title of the alert
   * @param header the header text of the alert
   * @param content the content text (description) of the alert
   */
  protected void showMessage(final String title, final String header, final String content) {
    final Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(header);
    alert.setContentText(content);
    alert.show();
  }
}
