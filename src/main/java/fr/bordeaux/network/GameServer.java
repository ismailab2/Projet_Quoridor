package fr.bordeaux.network;

import fr.bordeaux.util.Logger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import fr.bordeaux.i18n.I18n;

/**
 * This class implements a simple TCP server capable of: - Accepting a single client connection -
 * Handling basic commands (PING, QUIT) - Broadcasting its presence via UDP - Detecting client
 * timeouts (1 minute)
 *
 * <p>Communication protocol: - Messages are ASCII strings terminated by '\n' - Supported commands:
 * - PING → responds with PONG - QUIT → responds with BYE and disconnects client - Other → responds
 * with OK
 */
public class GameServer {

  /** Default constructor. Initializes a new GameServer instance. */
  public GameServer() {
    this.running = new AtomicBoolean(false);
    this.lastPing = System.currentTimeMillis();
  }

  /** TCP server socket */
  private ServerSocket serverSocket;

  /** Connected client socket (single client only) */
  private Socket clientSocket;

  /** Indicates whether the server is running */
  private final AtomicBoolean running;

  /** UDP broadcaster for server discovery */
  private ServerBroadcaster broadcaster;

  /** Thread running the broadcaster */
  private Thread broadcastThread;

  /** Timestamp of the last received PING */
  private long lastPing;

  /**
   * Starts server on port. Launches broadcaster and timeout checker.
   *
   * @param port TCP port to listen on.
   */
  public void start(final int port) {
    new Thread(
            () -> {
              try {
                serverSocket = new ServerSocket(port);
                running.set(true);

                System.out.println(I18n.get("startedPort") + port);

                broadcaster = new ServerBroadcaster("Quoridor", port);
                broadcastThread = new Thread(broadcaster);
                broadcastThread.start();

                startTimeoutChecker();

                clientSocket = serverSocket.accept();
                System.out.println(I18n.get("connectedClient"));

                new Thread(() -> handleClient(clientSocket)).start();
                // handleClient(clientSocket);

              } catch (IOException e) {
                System.out.println(I18n.get("alreadyUsed") + e.getMessage());
              }
            })
        .start();
  }

  /** Disconnects client if no PING within 60s. */
  private void startTimeoutChecker() {
    new Thread(
            () -> {
              while (running.get()) {

                if (System.currentTimeMillis() - lastPing > 60000) {
                  Logger.getInstance().info(I18n.get("clientTimeout"));
                  stop();
                  break;
                }

                try {
                  Thread.sleep(5000);
                } catch (InterruptedException ignored) {
                  Thread.currentThread().interrupt();
                }
              }
            })
        .start();
  }

  /**
   * Reads and handles messages (PING, QUIT, MOVE).
   *
   * @param socket Client socket.
   */
  private void handleClient(final Socket socket) {
    try (BufferedReader reader =
            new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        PrintWriter writer =
            new PrintWriter(
                new java.io.OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8),
                true)) {

      String line;

      while ((line = reader.readLine()) != null && running.get()) {
        Logger.getInstance().info("CLIENT -> {0}", line);

        if (line.startsWith("PING")) {
          lastPing = System.currentTimeMillis();
          writer.println("PONG");

        } else if ("QUIT".equals(line)) {
          writer.println("BYE");
          break;

        } else {
          writer.println("OK");
        }
      }

    } catch (IOException e) {
      System.out.println(I18n.get("disconnected"));
    }

    stop();
  }

  /** Stops broadcaster, closes sockets, and releases resources. */
  public void stop() {
    running.set(false);

    try {
      if (broadcaster != null) {
        broadcaster.stop();
      }

      if (broadcastThread != null && broadcastThread.isAlive()) {
        broadcastThread.interrupt();
      }

      if (clientSocket != null) {
        clientSocket.close();
      }

      if (serverSocket != null) {
        serverSocket.close();
      }

    } catch (IOException ignored) {
    }

    System.out.println(I18n.get("stopped"));
  }
}
