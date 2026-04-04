package fr.bordeaux.network;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

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

  /** Background listener thread responsible for reading messages from the server */
  private Thread listenerThread;

 /**
 * Synchronization queue used to transfer an expected response
 * from the listener thread to the calling method (ping, quit).
 */
  private final SynchronousQueue<String> syncResponse = new SynchronousQueue<>();

 /**
 * Indicates whether the next received message should be treated as
 * a direct reply to a synchronous command instead of an asynchronous one.
 */
  private volatile boolean expectingDirectReply = false;

  /**
   * Constructs a new GameClient.
   * This instance can be used to join a specific server or perform network discovery.
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
      System.out.println("Connected to " + host + ":" + port);

      startListener();
      return true;

    } catch (IOException e) {
      System.out.println("Connection failed: " + e.getMessage());
      return false;
    }
  }

/**
 * Starts the listener thread responsible for reading every line sent by the server.
 * Replies expected by synchronous commands are placed into the synchronization queue,
 * while other messages are printed directly.
 */
private void startListener() {
  listenerThread =
      new Thread(
          () -> {
            try {
              String line;
              while ((line = in.readLine()) != null) {
                if (expectingDirectReply) {
                 try {
                    boolean delivered = syncResponse.offer(line, 3, TimeUnit.SECONDS);
                    if (!delivered) {
                      System.out.println("Server: " + line);
                    }
                  } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                  } finally {
                    expectingDirectReply = false;
                  }
                } else {
                  System.out.println("Server: " + line);
                }
              }
            } catch (IOException e) {
              System.out.println("Connection closed: " + e.getMessage());
            }
          });
  listenerThread.setDaemon(true);
  listenerThread.start();
}

  /**
   * Sends "PING" to the server and waits for a response ("PONG"). The time between sending and
   * receiving is measured and displayed.
   */
  public void ping() {
    if (!checkConnection()) return;

    try {
      long start = System.currentTimeMillis();

      expectingDirectReply = true;
      System.out.println("Client: PING");
      out.println("PING");

      String res = syncResponse.poll(5, TimeUnit.SECONDS);
      long rtt = System.currentTimeMillis() - start;

      if (res != null) {
        System.out.println("Server: " + res + " TIME=" + rtt + "ms");
      } else {
        expectingDirectReply = false; 
        System.out.println("Ping timeout: no response within 5 seconds.");
    }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      System.out.println("Ping interrupted.");
    }
  }

  /**
   * Sends a "QUIT" command to the server, waits for the "BYE" response, and then closes the socket
   * connection.
   */
  public void quit() {
    if (checkConnection()){
      try {
        expectingDirectReply = true;
        System.out.println("Client: QUIT");
        out.println("QUIT");

        String res = syncResponse.poll(5, TimeUnit.SECONDS);
         if ("BYE".equals(res)) {
         System.out.println("Server: BYE");
       } else if (res != null) {
        System.out.println("Server: " + res);
       } else {
          expectingDirectReply = false; 
          System.out.println("No BYE received, closing anyway.");
        }

        socket.close();
      
    } catch (IOException | InterruptedException e) {
      if (e instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
  }
} 
    System.out.println("Client exited.");
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

                System.out.println("Broadcast sent, waiting for responses...");

                long start = System.currentTimeMillis();

                while (System.currentTimeMillis() - start < 5000) {
                  try {
                    byte[] buf = new byte[1024];
                    DatagramPacket rcv = new DatagramPacket(buf, buf.length);

                    ds.receive(rcv);

                    String serverInfo = new String(rcv.getData(), 0, rcv.getLength());
                    System.out.println("Server found: " + serverInfo);

                  } catch (SocketTimeoutException ignored) {
                    break;
                  }
                }

                System.out.println("Server discovery finished.");

              } catch (IOException e) {
                System.out.println("Server discovery failed: " + e.getMessage());
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
      System.out.println("Not connected to any server.");
      return false;
    }
    return true;
  }

  /**
   * Sends the player name to the server in order to register it in the current session.
   * @param name player name
   */
  public void hello(String name) {
    if (!checkConnection()) return;
    out.println("HELLO " + name);
  }

 /**
 * Sends a move to the server using the text format defined by the protocol.
 * @param move move to send
 */
  public void sendMove(String move) {
    if (!checkConnection()) return;
    out.println("MOVE " + move);
  }
}
