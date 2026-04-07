package fr.bordeaux.network;

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
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/** TCP client for Quoridor. Sends commands (PING, QUIT) and discovers servers via UDP broadcast. */
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

  /** Default constructor. */
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
      Logger.getInstance().info("Connected to {0}:{1,number,0}", host, port);

      startListener();
      return true;

    } catch (final IOException exception) {
      if (Logger.getInstance().isErrorEnabled()) {
        Logger.getInstance().error("Connection failed: {0}", exception.getMessage());
      }
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
                        Logger.getInstance().info("Server: {0}", line);
                      }
                    } catch (InterruptedException ie) {
                      Thread.currentThread().interrupt();
                    } finally {
                      expectingDirectReply.set(false);
                    }
                  } else {
                    Logger.getInstance().info("Server: {0}", line);
                  }
                }
              } catch (final IOException exception) {
                if (Logger.getInstance().isInfoEnabled()) {
                  Logger.getInstance().info("Connection closed: {0}", exception.getMessage());
                }
              }
            });
    listenerThread.setDaemon(true);
    listenerThread.start();
  }

  /** Sends "PING" and waits for "PONG". Measures RTT. */
  public void ping() {
    if (!checkConnection()) {
      return;
    }

    try {
      final long start = System.currentTimeMillis();

      expectingDirectReply.set(true);
      Logger.getInstance().info("Client: PING");
      writer.println("PING");

      final String res = syncResponse.poll(5, TimeUnit.SECONDS);
      final long rtt = System.currentTimeMillis() - start;

      if (res != null) {
        Logger.getInstance().info("Server: {0} TIME={1,number,0}ms", res, rtt);
      } else {
        expectingDirectReply.set(false);
        Logger.getInstance().info("Ping timeout: no response within 5 seconds.");
      }
    } catch (final InterruptedException exception) {
      Thread.currentThread().interrupt();
      Logger.getInstance().info("Ping interrupted.");
    }
  }

  /** Sends "QUIT", waits for "BYE", and closes socket. */
  public void quit() {
    if (checkConnection()) {
      try {
        expectingDirectReply.set(true);
        Logger.getInstance().info("Client: QUIT");
        writer.println("QUIT");

        final String res = syncResponse.poll(5, TimeUnit.SECONDS);
        if ("BYE".equals(res)) {
          Logger.getInstance().info("Server: BYE");
        } else if (res != null) {
          Logger.getInstance().info("Server: {0}", res);
        } else {
          expectingDirectReply.set(false);
          Logger.getInstance().info("No BYE received, closing anyway.");
        }

        socket.close();

      } catch (final IOException | InterruptedException exception) {
        if (exception instanceof InterruptedException) {
          Thread.currentThread().interrupt();
        }
      }
    }
    Logger.getInstance().info("Client exited.");
  }

  /** Broadcasts "DISCOVER" and listens for server responses for 5s. */
  public void serverList() {
    new Thread(
            () -> {
              try (DatagramSocket udpSocket = new DatagramSocket()) {

                udpSocket.setBroadcast(true);
                udpSocket.setSoTimeout(5000);

                final String msg = "DISCOVER";

                final DatagramPacket packet =
                    new DatagramPacket(
                        msg.getBytes(StandardCharsets.UTF_8),
                        msg.length(),
                        InetAddress.getByName("255.255.255.255"),
                        12346);

                udpSocket.send(packet);

                Logger.getInstance().info("Broadcast sent, waiting for responses...");

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
                            StandardCharsets.UTF_8);
                    Logger.getInstance().info("Server found: {0}", serverInfo);

                  } catch (final SocketTimeoutException ignored) {
                    break;
                  }
                }

                Logger.getInstance().info("Server discovery finished.");

              } catch (final IOException exception) {
                if (Logger.getInstance().isErrorEnabled()) {
                  Logger.getInstance()
                      .error("Server discovery failed: {0}", exception.getMessage());
                }
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
      Logger.getInstance().info("Not connected to any server.");
      return false;
    }
    return true;
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
  }
}
