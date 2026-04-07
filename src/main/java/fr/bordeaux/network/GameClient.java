package fr.bordeaux.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

import fr.bordeaux.i18n.I18n;

/**
 * This class allows a client to connect to a Quoridor server via TCP, send simple commands (PING,
 * QUIT), and discover available servers on the local network using UDP broadcast.
 *
 * <p>Communication protocol: - Messages are ASCII strings terminated by '\n' - Commands include
 * PING, QUIT - Server responds with PONG, BYE, etc.
 */
public class GameClient {

  /** TCP socket used for the connection */
  private Socket socket;

  /** Input stream to read server responses */
  private BufferedReader in;

  /** Output stream to send commands to the server */
  private PrintWriter out;

  /**
   * Constructs a new GameClient. This instance can be used to join a specific server or perform
   * network discovery.
   */
  public GameClient() {
    // Explicit default constructor to satisfy Javadoc requirements
  }

  /**
   * Attempts to establish a connection to the given host and port with a timeout of 5 seconds.
   *
   * @param host Server hostname or IP address.
   * @param port Server TCP port.
   * @return true if the connection succeeds, false otherwise.
   */
  public boolean join(String host, int port) {
    try {
      socket = new Socket();
      socket.connect(new InetSocketAddress(host, port), 5000);

      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      out = new PrintWriter(socket.getOutputStream(), true);

      System.out.println(I18n.get("connected", host, ":") + port);
      return true;

    } catch (IOException e) {
      System.out.println(I18n.get("failedCo", ": ") + e.getMessage());
      return false;
    }
  }

  /**
   * Sends "PING" to the server and waits for a response ("PONG"). The time between sending and
   * receiving is measured and displayed.
   */
  public void ping() {
    if (!checkConnection()) return;

    try {
      long start = System.currentTimeMillis();

      System.out.println("Client: PING");
      out.println("PING");

      String res = in.readLine();

      long rtt = System.currentTimeMillis() - start;

      System.out.println(I18n.get("server", res, "TIME=") + rtt + "ms");

    } catch (IOException e) {
      System.out.println(I18n.get("pingError") + e.getMessage());
    }
  }

  /**
   * Sends a "QUIT" command to the server, waits for the "BYE" response, and then closes the socket
   * connection.
   */
  public void quit() {
    try {
      if (checkConnection()) {
        System.out.println("Client: QUIT");
        out.println("QUIT");

        String res = in.readLine(); // read BYE
        System.out.println(I18n.get("server", res));

        socket.close();
      }
    } catch (IOException ignored) {
    }

    System.out.println(I18n.get("clientQuit"));
  }

  /**
   * Sends a broadcast message ("DISCOVER") to the local network and listens for responses from
   * servers. Displays all discovered servers within a 5-second time window.
   *
   * <p>Runs in a separate thread to avoid blocking the main application.
   */
  public void serverList() {
    new Thread(
            () -> {
              try (DatagramSocket ds = new DatagramSocket()) {

                ds.setBroadcast(true);
                ds.setSoTimeout(5000);

                String msg = "DISCOVER";

                DatagramPacket packet =
                    new DatagramPacket(
                        msg.getBytes(),
                        msg.length(),
                        InetAddress.getByName("255.255.255.255"),
                        12346);

                ds.send(packet);

                System.out.println(I18n.get("broadcast"));

                long start = System.currentTimeMillis();

                while (System.currentTimeMillis() - start < 5000) {
                  try {
                    byte[] buf = new byte[1024];
                    DatagramPacket rcv = new DatagramPacket(buf, buf.length);

                    ds.receive(rcv);

                    String serverInfo = new String(rcv.getData(), 0, rcv.getLength());
                    System.out.println(I18n.get("found", serverInfo));

                  } catch (SocketTimeoutException ignored) {
                    break;
                  }
                }

                System.out.println(I18n.get("discoOver"));

              } catch (IOException e) {
                System.out.println(I18n.get("discoFail", ": ") + e.getMessage());
              }
            })
        .start();
  }

  /**
   * @brief Checks whether the client is currently connected.
   * @return true if the socket exists and is open, false otherwise.
   */
  private boolean checkConnection() {
    if (socket == null || socket.isClosed()) {
      System.out.println(I18n.get("notConnected"));
      return false;
    }
    return true;
  }
}
