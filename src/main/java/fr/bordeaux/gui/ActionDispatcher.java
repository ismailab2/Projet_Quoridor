package fr.bordeaux.gui;

import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.core.GameEngine;
import java.io.IOException;
import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import fr.bordeaux.network.GameClient;
import fr.bordeaux.network.MultiGameServer;

/**
 * Dispatches actions from the GUI to the GameEngine or other dialogs.
 *
 * <p>This class serves as a bridge between GUI events (menu items, shortcuts) and the underlying
 * game logic or dialogs. Each action corresponds to a method in the GameEngine or a GUI dialog.
 */
public class ActionDispatcher {

  /** Reference to the game engine to execute game-related actions */
  private final GameEngine engine;

  private final FileHandler fileHandler;

  private final ConfigManager configManager = ConfigManager.getInstance();

  private final MultiGameServer multiServer = new MultiGameServer();

  private final GameClient networkClient = new GameClient();

  /**
   * Constructs an ActionDispatcher with a given GameEngine.
   *
   * @param engine The GameEngine instance to control the game.
   * @param fileHandler The FileHandler used to save and load.
   */
  public ActionDispatcher(GameEngine engine, FileHandler fileHandler) {
    this.engine = engine;
    this.fileHandler = fileHandler;
  }

  /**
   * Dispatches a given action to the appropriate handler.
   *
   * <p>Depending on the action type, this method may: - Call GameEngine methods (newGame, loadGame,
   * saveGame, undo, redo, pause, hint) - Show GUI dialogs (configuration, info) - Exit the
   * application
   *
   * @param action The Action enum representing the user command.
   * @throws IOException If an I/O error occurs during command execution.
   */
  public void dispatch(Action action) throws IOException {
    switch (action) {
      case NEW_GAME -> engine.newGame(configManager);
      case LOAD_GAME -> {
        String path = fileHandler.chooseLoadFile();
        if (path != null) engine.loadGame(path);
      }
      case SAVE_GAME -> {
        String path = fileHandler.chooseSaveFile();
        if (path != null) engine.saveGame(path);
      }
      case CONFIGURATION -> ConfigDialog.show();
      case INFO -> InfoDialog.show();
      case REDO -> engine.redo();
      case UNDO -> engine.undo();
      case PAUSE -> engine.pause();
      case HINT -> engine.hint();
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
      case QUIT -> Platform.exit();
    }
  }

  private void startServer() {
    String input = promptUser("Start Server", "Server Port", "Enter the TCP port:", "12345");
    if (input == null) {
      return;
    }

    int port = Integer.parseInt(input.trim());
    multiServer.start(port);
    showMessage("Network", "Server", "Server started on port " + port + ".");
  }

  private void createNetworkGame() {
    String input =
        promptUser(
            "New Network Game",
            "Player IDs",
            "Enter 2 or 4 player IDs separated by spaces:",
            "C1 C2");
    if (input == null || input.isBlank()) {
      return;
    }

    String[] ids = input.trim().split("\\s+");
    showMessage("Network", "New Game", multiServer.createGame(ids));
  }

  private void joinServer() {
    String input =
        promptUser("Join Server", "Host and Port", "Enter host[:port]:", "localhost:12345");
    if (input == null || input.isBlank()) {
      return;
    }

    String[] hp = input.trim().split(":");
    String host = hp[0];
    int port = hp.length > 1 ? Integer.parseInt(hp[1]) : 12345;

    if (networkClient.join(host, port)) {
      showMessage("Network", "Join Server", "Connected to " + host + ":" + port + ".");
    } else {
      showMessage("Network", "Join Server", "Failed to connect to " + host + ":" + port + ".");
    }
  }

  private void setNetworkName() {
    String input = promptUser("Set Player Name", "Player Name", "Enter your player name:", "");
    if (input == null || input.isBlank()) {
      return;
    }

    networkClient.hello(input.trim());
    showMessage("Network", "Player Name", "Name sent to server.");
  }

  protected String promptUser(String title, String header, String content, String defaultValue) {
    TextInputDialog dialog = new TextInputDialog(defaultValue);
    dialog.setTitle(title);
    dialog.setHeaderText(header);
    dialog.setContentText(content);
    Optional<String> result = dialog.showAndWait();
    return result.orElse(null);
  }

  protected void showMessage(String title, String header, String content) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(header);
    alert.setContentText(content);
    alert.show();
  }
}
