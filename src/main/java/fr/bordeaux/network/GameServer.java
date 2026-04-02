package fr.bordeaux.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

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
  public GameServer() {}

  /** TCP server socket */
  private ServerSocket serverSocket;

  /** Connected client socket (single client only) */
  private Socket clientSocket;

  /** Indicates whether the server is running */
  private volatile boolean running = false;

  /** UDP broadcaster for server discovery */
  private ServerBroadcaster broadcaster;

  /** Thread running the broadcaster */
  private Thread broadcastThread;

  /** Timestamp of the last received PING */
  private long lastPing = System.currentTimeMillis();

  /**
   * Creates a TCP server socket, starts the UDP broadcaster, launches the timeout checker, and
   * waits for a client connection.
   *
   * @param port TCP port to listen on.
   */
  public void start(int port) {
    new Thread(
            () -> {
              try {
                serverSocket = new ServerSocket(port);
                running = true;

                System.out.println("Server started on port " + port);

                broadcaster = new ServerBroadcaster("Quoridor", port);
                broadcastThread = new Thread(broadcaster);
                broadcastThread.start();

                startTimeoutChecker();

                clientSocket = serverSocket.accept();
                System.out.println("Client connected");

                new Thread(() -> handleClient(clientSocket)).start();
                // handleClient(clientSocket);

              } catch (IOException e) {
                System.out.println("Port already used or error: " + e.getMessage());
              }
            })
        .start();
  }

  /**
   * Periodically checks if the client has sent a PING within the last 60 seconds. If not, the
   * client is disconnected and the server stops.
   */
  private void startTimeoutChecker() {
    new Thread(
            () -> {
              while (running) {

                if (System.currentTimeMillis() - lastPing > 60000) {
                  System.out.println("Client timeout");
                  stop();
                  break;
                }

                try {
                  Thread.sleep(5000);
                } catch (InterruptedException ignored) {
                }
              }
            })
        .start();
  }

  /**
   * Reads incoming messages and responds according to the protocol: - PING → updates lastPing and
   * sends PONG - QUIT → sends BYE and disconnects client - Other → sends OK
   *
   * @param socket Client socket.
   */
  private void handleClient(Socket socket) {
    try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

      String line;

      while ((line = in.readLine()) != null && running) {
        System.out.println("CLIENT -> " + line);

        if (line.startsWith("PING")) {
          lastPing = System.currentTimeMillis();
          out.println("PONG");

        } else if (line.equals("QUIT")) {
          out.println("BYE");
          break;

        } else {
          out.println("OK");
        }
      }

    } catch (IOException e) {
      System.out.println("Client disconnected");
    }

    stop();
  }

  /**
   * Stops the UDP broadcaster, interrupts its thread, closes all sockets, and releases resources.
   */
  public void stop() {
    running = false;

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

    System.out.println("Server stopped.");
  }
}
