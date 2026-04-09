package fr.bordeaux.main;

import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.network.GameClient;
import fr.bordeaux.network.MultiGameServer;
import fr.bordeaux.util.Logger;
import java.util.Scanner;

/** CLI interface for network features. Start/stop server, join, ping. */
public class NetworkLauncher implements AppLauncher {

  /** Default host used when no address is provided. */
  private static final String DEFAULT_HOST = "localhost";
  
  /** Default TCP port used when no port is provided. */
  private static final int DEFAULT_PORT = 12345;

  /** Multi-game server managing multiple simultaneous games and players. */
  private final MultiGameServer multiServer;

  /** Network client used to connect to and interact with remote servers. */
  private final GameClient client;

  /**
   * Constructs a new NetworkLauncher with specified network components.
   *
   * @param multiServer the MultiGameServer instance
   * @param client the GameClient instance
   */
  NetworkLauncher(final MultiGameServer multiServer, final GameClient client) {
    this.multiServer = multiServer;
    this.client = client;
  }

  /** Default constructor. Constructs a new NetworkLauncher instance. */
  public NetworkLauncher() {
    this(new MultiGameServer(), new GameClient());
  }

  /**
   * Runs command loop for network interaction.
   *
   * @param args CLI args (unused)
   * @param config configuration manager
   */
  @Override
  public void launch(final String[] args, final ConfigManager config) {
    final String serverMode = config.getOption("server", "false");
    final String serverPort = config.getOption("server-port", "12345");
    final String daemonMode = config.getOption("daemon", "false");

    if ("true".equals(serverMode)) {
      final int port = (serverPort != null) ? Integer.parseInt(serverPort) : 12345;
      multiServer.start(port);

      if ("true".equals(daemonMode)) {
        Logger.getInstance().info("Server started in daemon mode on port {0,number,0}", port);
        while (true) {
          try {
            Thread.sleep(1000);
          } catch (final InterruptedException exception) {
            Thread.currentThread().interrupt();
            break;
          }
        }
        return;
      }
    }

    Logger.getInstance().info("Commands:");
    Logger.getInstance().info("server start [port]");
    Logger.getInstance().info("server stop");
    Logger.getInstance().info("server list");
    Logger.getInstance().info("join [ip[:port]]");
    Logger.getInstance().info("ping");
    Logger.getInstance().info("quit");
    Logger.getInstance().info("server status");
    Logger.getInstance().info("players");
    Logger.getInstance().info("scoreboard");
    Logger.getInstance().info("new PLAYER1 PLAYER2 [PLAYER3 PLAYER4]");
    Logger.getInstance().info("hello NAME");
    Logger.getInstance().info("move MOVE");

    try (Scanner scanner = new Scanner(System.in)) {
      boolean running = true;
      while (running && scanner.hasNextLine()) {
        Logger.getInstance().prompt("> ");
        final String cmd = scanner.nextLine();

        try {
          if (cmd.startsWith("server start")) {
            final String[] parts = cmd.split(" ");
            final int port = (parts.length > 2) ? Integer.parseInt(parts[2]) : 12345;
            multiServer.start(port);
          } else if ("server stop".equals(cmd)) {
            multiServer.stop();
          } else if ("server status".equals(cmd)) {
            if (Logger.getInstance().isInfoEnabled()) {
              Logger.getInstance().info("{0}", multiServer.getServerStatus());
            }

          } else if ("players".equals(cmd)) {
            if (Logger.getInstance().isInfoEnabled()) {
              Logger.getInstance().info("{0}", multiServer.listPlayers());
            }

          } else if ("scoreboard".equals(cmd)) {
            if (Logger.getInstance().isInfoEnabled()) {
              Logger.getInstance().info("{0}", multiServer.getScoreboard());
            }

          } else if (cmd.startsWith("new ")) {
            final String[] parts = cmd.split(" ");
            if (parts.length != 3 && parts.length != 5) {
              Logger.getInstance().info("Usage: new PLAYER1 PLAYER2 [PLAYER3 PLAYER4]");
            } else {
              final String[] playerIds = new String[parts.length - 1];
              System.arraycopy(parts, 1, playerIds, 0, playerIds.length);
              if (Logger.getInstance().isInfoEnabled()) {
                Logger.getInstance().info("{0}", multiServer.createGame(playerIds));
              }
            }

          } else if (cmd.equals("join") || cmd.startsWith("join ")) {
            final String[] joinParts = cmd.trim().split("\\s+");
            final String target = joinParts.length > 1 ? joinParts[1] : DEFAULT_HOST;
            final String[] hostPort = target.split(":");
            final String host = hostPort[0];
            final int port = hostPort.length > 1 ? Integer.parseInt(hostPort[1]) : DEFAULT_PORT;
            final boolean success = client.join(host, port);
            if (!success) {
              Logger.getInstance().info("Failed to connect to {0}:{1,number,0}", host, port);
            }
          } else if (cmd.startsWith("hello ")) {
            final String name = cmd.substring("hello ".length()).trim();
            client.hello(name);
          } else if ("ping".equals(cmd)) {
            client.ping();
          } else if (cmd.startsWith("move ")) {
            final String move = cmd.substring("move ".length()).trim();
            client.sendMove(move);
          } else if ("server list".equals(cmd)) {
            Logger.getInstance().info("{0}", client.serverList());
          } else if ("quit".equals(cmd) || "exit".equals(cmd)) {
            if ("quit".equals(cmd)) {
              client.quit();
            }
            running = false;
          } else {
            Logger.getInstance().info("Unknown command");
          }
        } catch (final Exception exception) {
          if (Logger.getInstance().isInfoEnabled()) {
            Logger.getInstance().info("Error: {0}", exception.getMessage());
          }
        }
      }
    }
    Logger.getInstance().info("Network mode exited.");
  }
}
