package fr.bordeaux.network;

import fr.bordeaux.i18n.I18n;
import fr.bordeaux.util.Logger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/** TCP client for Quoridor. Sends commands (PING, QUIT) and discovers servers via UDP broadcast. */
@SuppressWarnings({"PMD.GuardLogStatement"})
public class GameClient {

  /** TCP socket used for the connection */
  private Socket socket;

  /** Input stream to read server responses */
  private BufferedReader reader;

  /** Output stream to send commands to the server */
  private PrintWriter writer;

  /** Background listener thread responsible for reading messages from the server */
  private Thread listenerThread;

  /** Sync queue to transfer response from listener thread to caller. */
  private final SynchronousQueue<String> syncResponse;

  /** Flag to treat next message as a direct reply to a sync command. */
  private final AtomicBoolean expectingDirectReply;

  /** Handler notified when an asynchronous message is received from the server. */
  private Consumer<String> serverMessageHandler;

  /** UDP port used for server discovery. */
  private int discoveryPort = 12346;

  /** Discovery listening window in milliseconds. */
  private static final long DISCOVERY_WINDOW_MS = 5000L;

  /** Delay after which a server is considered expired. */
  private static final long SERVER_EXPIRATION_MS = 30000L;

  /** Servers discovered recently on the local network. */
  private final Map<String, Long> discoveredServers = new ConcurrentHashMap<>();

  /** Constructs a GameClient for joining servers or discovery. */
  public GameClient() {
    this.syncResponse = new SynchronousQueue<>();
    this.expectingDirectReply = new AtomicBoolean(false);
  }

  /**
   * Connects to host/port with a 5s timeout.
   *
   * @param host Server hostname or IP.
   * @param port Server TCP port.
   * @return true if success.
   */
  public boolean join(final String host, final int port) {
    try {
      socket = new Socket();
      socket.connect(new InetSocketAddress(host, port), 5000);

      reader =
          new BufferedReader(
              new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
      writer = new PrintWriter(socket.getOutputStream(), true);
      Logger.getInstance().info(I18n.get("connected") + " {0}:{1,number,0}", host, port);

      startListener();
      return true;

    } catch (final IOException exception) {
      Logger.getInstance().error(I18n.get("failedCo", ": ") + exception.getMessage());
      return false;
    }
  }

  /** Starts listener thread for server messages. Handles sync/async responses. */
  private void startListener() {
    listenerThread =
        new Thread(
            () -> {
              try {
                String line;
                while ((line = reader.readLine()) != null) {
                  if (expectingDirectReply.get()) {
                    try {
                      final boolean delivered = syncResponse.offer(line, 3, TimeUnit.SECONDS);
                      if (!delivered) {
                        Logger.getInstance().info(I18n.get("server", line));
                      }
                      notifyServerMessage(line);
                    } catch (InterruptedException ie) {
                      Thread.currentThread().interrupt();
                    } finally {
                      expectingDirectReply.set(false);
                    }
                  } else {
                    Logger.getInstance().verbose("Net In: {0}", line);
                    Logger.getInstance().info(I18n.get("server", line));
                    notifyServerMessage(line);
                  }
                }
              } catch (final IOException exception) {
                Logger.getInstance().info(I18n.get("connecClosed", exception.getMessage()));
              }
            });
    listenerThread.setDaemon(true);
    listenerThread.start();
  }

  /**
   * Notifies the registered handler that a server message was received.
   *
   * @param message message received from the server
   */
  private void notifyServerMessage(final String message) {
    if (serverMessageHandler != null) {
      serverMessageHandler.accept(message);
    }
  }

  /**
   * Sets the handler called when an asynchronous message is received from the server.
   *
   * @param serverMessageHandler handler receiving the server message
   */
  public void setServerMessageHandler(final Consumer<String> serverMessageHandler) {
    this.serverMessageHandler = serverMessageHandler;
  }

  /**
   * Sends "PING" to the server and waits for a response ("PONG"). The time between sending and
   * receiving is measured and displayed.
   *
   * @return a status message indicating the result of the ping command.
   */
  public String ping() {

    String message;
    if (!checkConnection()) {
      message = I18n.get("notConnected");
    } else {
      try {
        final long start = System.currentTimeMillis();

        expectingDirectReply.set(true);
        Logger.getInstance().info("Client: PING");
        Logger.getInstance().verbose("Net Out: PING");
        writer.println("PING");

        final String res = syncResponse.poll(5, TimeUnit.SECONDS);
        final long rtt = System.currentTimeMillis() - start;

        if (res != null) {
          message = MessageFormat.format(I18n.get("server") + " {0} TIME={1,number,0}ms", res, rtt);
          Logger.getInstance().info(message);
        } else {
          expectingDirectReply.set(false);
          message = I18n.get("pingTimeout");
          Logger.getInstance().info(message);
        }
      } catch (final InterruptedException exception) {
        Thread.currentThread().interrupt();
        message = I18n.get("pingInterrupt");
        Logger.getInstance().info(message);
      }
    }

    return message;
  }

  /**
   * Sends a "QUIT" command to the server, waits for the "BYE" response, and then closes the socket
   * connection.
   *
   * @return a message indicating the final status of the connection termination.
   */
  public String quit() {
    String message = "Client exited.";
    if (checkConnection()) {
      try {
        expectingDirectReply.set(true);
        Logger.getInstance().info("Client: QUIT");
        Logger.getInstance().verbose("Net Out: QUIT");
        writer.println("QUIT");

        final String res = syncResponse.poll(5, TimeUnit.SECONDS);
        if ("BYE".equals(res)) {
          message = I18n.get("server", " BYE");
          Logger.getInstance().info(message);
        } else if (res != null) {
          Logger.getInstance().info(I18n.get("server", res));
        } else {
          expectingDirectReply.set(false);
          message = I18n.get("noBye");
          Logger.getInstance().info(message);
        }

        socket.close();
        socket = null;

      } catch (final IOException | InterruptedException exception) {
        if (exception instanceof InterruptedException) {
          Thread.currentThread().interrupt();
        }
      }
    }

    Logger.getInstance().info(I18n.get("clientQuit"));
    return message;
  }

  /**
   * Broadcasts "DISCOVER" and listens for server responses for 5s.
   *
   * @return a message indicating the result of the server list command.
   */
  public String serverList() {
    try (DatagramSocket udpSocket = new DatagramSocket()) {

      udpSocket.setBroadcast(true);
      udpSocket.setSoTimeout(5000);

      final String msg = "DISCOVER";

      final DatagramPacket broadcastPacket =
          new DatagramPacket(
              msg.getBytes(StandardCharsets.UTF_8),
              msg.length(),
              InetAddress.getByName("255.255.255.255"),
              discoveryPort);

      udpSocket.send(broadcastPacket);

      final DatagramPacket localhostPacket =
          new DatagramPacket(
              msg.getBytes(StandardCharsets.UTF_8),
              msg.length(),
              InetAddress.getByName("127.0.0.1"),
              discoveryPort);

      udpSocket.send(localhostPacket);

      final long start = System.currentTimeMillis();

      while (System.currentTimeMillis() - start < 5000) {
        try {
          final byte[] buf = new byte[1024];
          final DatagramPacket receivedPacket = new DatagramPacket(buf, buf.length);

          udpSocket.receive(receivedPacket);

          final String serverInfo =
              new String(
                      receivedPacket.getData(),
                      0,
                      receivedPacket.getLength(),
                      StandardCharsets.UTF_8)
                  .trim();

          discoveredServers.put(serverInfo, System.currentTimeMillis());
        } catch (final SocketTimeoutException ignored) {
          break;
        }
      }
    } catch (final IOException exception) {
      Logger.getInstance().error(I18n.get("discoFail", ": ", exception.getMessage()));
    }

    final long now = System.currentTimeMillis();
    discoveredServers.entrySet().removeIf(entry -> now - entry.getValue() > 30000);

    if (discoveredServers.isEmpty()) {
      return "No servers discovered.";
    }

    return String.join("\n", discoveredServers.keySet());
  }

  /**
   * @brief Checks whether the client is currently connected.
   * @return true if the socket exists and is open, false otherwise.
   */
  private boolean checkConnection() {
    if (!isConnected()) {
      Logger.getInstance().info(I18n.get("notConnected"));
      return false;
    }
    return true;
  }

  /**
   * Indicates whether the TCP socket is currently connected and open.
   *
   * @return true if the socket exists, is connected, and is not closed; false otherwise
   */
  public boolean isConnected() {
    return socket != null && socket.isConnected() && !socket.isClosed();
  }

  /**
   * Registers player name with server.
   *
   * @param name player name
   */
  public void hello(final String name) {
    if (!checkConnection()) {
      return;
    }
    writer.println("HELLO " + name);
    Logger.getInstance().verbose("Net Out: HELLO {0}", name);
  }

  /**
   * Sends a move to the server using the text format defined by the protocol.
   *
   * @param move move to send
   */
  public void sendMove(final String move) {
    if (!checkConnection()) {
      return;
    }
    writer.println("MOVE " + move);
    Logger.getInstance().verbose("Net Out: MOVE {0}", move);
  }

  /**
   * Sets the UDP port used for server discovery.
   *
   * @param port The UDP port to use.
   */
  public void setDiscoveryPort(final int port) {
    this.discoveryPort = port;
  }
}
