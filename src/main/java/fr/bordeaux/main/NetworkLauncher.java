package fr.bordeaux.main;

import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.network.GameClient;
import fr.bordeaux.network.MultiGameServer;
import java.util.Scanner;

/**
 * This class provides a CLI interface to interact with the network features of Quoridor. Users can
 * start/stop a server, join a remote server, ping for latency testing, and discover servers on the
 * local network.
 *
 * <p>Supported commands:
 *
 * <ul>
 *   <li><b>server start [port]</b>: Start a TCP server on the specified port (default 12345).
 *   <li><b>server stop</b>: Stop the currently running server.
 *   <li><b>server list</b>: Discover servers broadcasting on the local network via UDP.
 *   <li><b>join ip[:port]</b>: Connect to a remote server at the specified IP and port (default
 *       12345).
 *   <li><b>ping</b>: Send a PING to the connected server and measure RTT.
 *   <li><b>quit</b>: Disconnect from the server and exit network mode.
 * </ul>
 */
@SuppressWarnings("PMD.SystemPrintln")
public class NetworkLauncher implements AppLauncher {

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
   * This method runs a command loop reading user input and executing the corresponding network
   * commands using {@link MultiGameServer} and {@link GameClient}.
   *
   * @param args Command-line arguments (not used here)
   * @param config Configuration manager instance
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
        System.out.println("Server started in daemon mode on port " + port);
        while (true) {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            break;
          }
        }
        return;
      }
    }

    System.out.println("Commands:");
    System.out.println("server start [port]");
    System.out.println("server stop");
    System.out.println("server list");
    System.out.println("join ip[:port]");
    System.out.println("ping");
    System.out.println("quit");
    System.out.println("server status");
    System.out.println("players");
    System.out.println("scoreboard");
    System.out.println("new PLAYER1 PLAYER2 [PLAYER3 PLAYER4]");
    System.out.println("hello NAME");
    System.out.println("move MOVE");

    try (Scanner sc = new Scanner(System.in)) {
      boolean running = true;
      while (running && sc.hasNextLine()) {
        System.out.print("> ");
        final String cmd = sc.nextLine();

        try {
          if (cmd.startsWith("server start")) {
            final String[] p = cmd.split(" ");
            final int port = (p.length > 2) ? Integer.parseInt(p[2]) : 12345;
            multiServer.start(port);
          } else if (cmd.equals("server stop")) {
            multiServer.stop();
          } else if (cmd.equals("server status")) {
            System.out.println(multiServer.getServerStatus());

          } else if (cmd.equals("players")) {
            System.out.println(multiServer.listPlayers());

          } else if (cmd.equals("scoreboard")) {
            System.out.println(multiServer.getScoreboard());

          } else if (cmd.startsWith("new ")) {
            final String[] parts = cmd.split(" ");
            if (parts.length != 3 && parts.length != 5) {
              System.out.println("Usage: new PLAYER1 PLAYER2 [PLAYER3 PLAYER4]");
            } else {
              final String[] playerIds = new String[parts.length - 1];
              System.arraycopy(parts, 1, playerIds, 0, playerIds.length);
              System.out.println(multiServer.createGame(playerIds));
            }

          } else if (cmd.startsWith("join")) {
            final String[] joinParts = cmd.split(" ");
            if (joinParts.length < 2) {
              System.out.println("Usage: join ip[:port]");
              continue;
            }
            final String target = joinParts[1];
            final String[] hp = target.split(":");
            final String host = hp[0];
            final int port = hp.length > 1 ? Integer.parseInt(hp[1]) : 12345;
            final boolean success = client.join(host, port);
            if (!success) {
              System.out.println("Failed to connect to " + host + ":" + port);
            }
          } else if (cmd.startsWith("hello ")) {
            final String name = cmd.substring("hello ".length()).trim();
            client.hello(name);
          } else if (cmd.equals("ping")) {
            client.ping();
          } else if (cmd.startsWith("move ")) {
            final String move = cmd.substring("move ".length()).trim();
            client.sendMove(move);
          } else if (cmd.equals("server list")) {
            client.serverList();
          } else if (cmd.equals("quit") || cmd.equals("exit")) {
            if (cmd.equals("quit")) {
              client.quit();
            }
            running = false;
          } else {
            System.out.println("Unknown command");
          }
        } catch (Exception e) {
          System.out.println("Error: " + e.getMessage());
        }
      }
    }
    System.out.println("Network mode exited.");
  }
}
