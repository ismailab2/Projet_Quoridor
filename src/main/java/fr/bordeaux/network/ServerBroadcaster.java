package fr.bordeaux.network;

import fr.bordeaux.util.Log;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class sends a UDP broadcast message every 10 seconds containing the server name and its TCP
 * port. This allows clients on the local network to discover available game servers.
 *
 * <p>The broadcast is sent to the address 255.255.255.255 on port 12346.
 */
public class ServerBroadcaster implements Runnable {

  /** Name of the server to broadcast */
  private final String name;

  /** TCP port of the server */
  private final int port;

  /** UDP port used for broadcasting server presence. */
  private static final int BROADCAST_PORT = 12346;

  /** Flag controlling the broadcasting loop */
  private final AtomicBoolean running = new AtomicBoolean(true);

  /** time */
  private final int delayMs;

  /**
   * Broadcasts the server's presence on the local network every 10 seconds.
   *
   * @param name Name of the server.
   * @param port TCP port of the server.
   */
  public ServerBroadcaster(final String name, final int port) {
    this(name, port, 10000);
  }

  /**
   * Broadcasts the server's presence on the local network every 10 seconds.
   *
   * @param name Name of the server.
   * @param port TCP port of the server.
   * @param delayMs time.
   */
  public ServerBroadcaster(final String name, final int port, final int delayMs) {
    this.name = name;
    this.port = port;
    this.delayMs = delayMs;
  }

  /**
   * Opens a UDP socket and continuously sends broadcast packets containing the server information
   * every 10 seconds.
   *
   * <p>The loop stops when {@link #stop()} is called.
   */
  @Override
  public void run() {
    try (DatagramSocket socket = new DatagramSocket()) {
      socket.setBroadcast(true);
      final InetAddress address = InetAddress.getByName("127.0.0.1"); // localhost pour test

      while (running.get()) {
        final String msg = name + ":" + port;
        final byte[] buf = msg.getBytes(StandardCharsets.UTF_8);

        final DatagramPacket packet = new DatagramPacket(buf, buf.length, address, BROADCAST_PORT);
        socket.send(packet);

        Thread.sleep(delayMs);
      }
    } catch (final IOException e) {
      if (Log.isErrorEnabled()) {
        Log.error("UDP Broadcast I/O error: {0}", e.getMessage());
      }
    } catch (final InterruptedException e) {
      Log.info("UDP Broadband interrupted.");
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Sets the running flag to false, causing the thread to exit gracefully on the next iteration.
   */
  public void stop() {
    running.set(false);
  }
}
