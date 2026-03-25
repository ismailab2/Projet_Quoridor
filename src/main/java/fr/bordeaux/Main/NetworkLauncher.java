package fr.bordeaux.Main;

import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.network.GameClient;
import fr.bordeaux.network.GameServer;
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
public class NetworkLauncher implements AppLauncher {

    /**
     * Default constructor.
     * Constructs a new NetworkLauncher instance.
     */
    public NetworkLauncher() {
    }

  /**
   * This method runs a command loop reading user input and executing the corresponding network
   * commands using {@link GameServer} and {@link GameClient}.
   *
   * @param args Command-line arguments (not used here)
   * @param config Configuration manager instance
   */
  @Override
  public void launch(String[] args, ConfigManager config) {
    Scanner sc = new Scanner(System.in);

    GameServer server = new GameServer();
    GameClient client = new GameClient();

    System.out.println("Commands:");
    System.out.println("server start [port]");
    System.out.println("server stop");
    System.out.println("server list");
    System.out.println("join ip[:port]");
    System.out.println("ping");
    System.out.println("quit");

    while (true) {
      System.out.print("> ");
      String cmd = sc.nextLine();

      try {
        if (cmd.startsWith("server start")) {
          String[] p = cmd.split(" ");
          int port = (p.length > 2) ? Integer.parseInt(p[2]) : 12345;
          server.start(port);
        } else if (cmd.equals("server stop")) {
          server.stop();
        } else if (cmd.startsWith("join")) {
          String target = cmd.split(" ")[1];
          String[] hp = target.split(":");
          String host = hp[0];
          int port = hp.length > 1 ? Integer.parseInt(hp[1]) : 12345;
          boolean success = client.join(host, port);
          if (!success) {
            System.out.println("Failed to connect to " + host + ":" + port);
          }
        } else if (cmd.equals("ping")) {
          client.ping();
        } else if (cmd.equals("server list")) {
          client.serverList();
        } else if (cmd.equals("quit")) {
          client.quit();
          break;
        } else {
          System.out.println("Unknown command");
        }
      } catch (Exception e) {
        System.out.println("Error: " + e.getMessage());
      }
    }

    sc.close();
    System.out.println("Network mode exited.");
  }
}
