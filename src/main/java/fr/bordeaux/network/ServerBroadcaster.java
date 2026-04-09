<<<<<<< HEAD
package fr.bordeaux.network;

import fr.bordeaux.util.Logger;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

/** Sends UDP broadcast every 10s for server discovery on port 12346. */
@SuppressWarnings({"PMD.GuardLogStatement"})
public class ServerBroadcaster implements Runnable {

  /** Name of the server to broadcast */
  private final String name;

  /** TCP port of the server */
  private final int port;

  /** UDP port used for broadcasting server presence. */
  private int broadcastPort = 12_346;

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
    this(name, port, 10_000);
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

  /** Broadcasts server info via UDP every 10s. Stops on stop(). */
  @Override
  public void run() {
    try (DatagramSocket socket = new DatagramSocket()) {
      socket.setBroadcast(true);
      final InetAddress address = InetAddress.getLoopbackAddress();

      while (running.get()) {
        final String msg = name + ":" + port;
        final byte[] buf = msg.getBytes(StandardCharsets.UTF_8);

        final DatagramPacket packet = new DatagramPacket(buf, buf.length, address, broadcastPort);
        socket.send(packet);

        Thread.sleep(delayMs);
      }
    } catch (final IOException e) {
      Logger.getInstance().error("UDP Broadcast I/O error: {0}", e.getMessage());
    } catch (final InterruptedException e) {
      Logger.getInstance().info("UDP Broadband interrupted.");
      Thread.currentThread().interrupt();
    }
  }

  /** Sets running flag to false for graceful exit. */
  public void stop() {
    running.set(false);
  }

  /**
   * Sets the UDP port used for broadcasting server presence.
   *
   * @param port The UDP port to use.
   */
  public void setBroadcastPort(final int port) {
    this.broadcastPort = port;
  }
}
=======
package fr.bordeaux.network;

import fr.bordeaux.util.Logger;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

/** Sends UDP broadcast every 10s for server discovery on port 12346. */
@SuppressWarnings({"PMD.GuardLogStatement"})
public class ServerBroadcaster implements Runnable {

  /** Name of the server to broadcast */
  private final String name;

  /** TCP port of the server */
  private final int port;

  /** UDP port used for broadcasting server presence. */
  private static final int BROADCAST_PORT = 12_346;

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
    this(name, port, 10_000);
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

  /** Broadcasts server info via UDP every 10s. Stops on stop(). */
  @Override
  public void run() {
    try (DatagramSocket socket = new DatagramSocket()) {
      socket.setBroadcast(true);
      final InetAddress address = InetAddress.getLoopbackAddress();

      while (running.get()) {
        final String msg = name + ":" + port;
        final byte[] buf = msg.getBytes(StandardCharsets.UTF_8);

        final DatagramPacket packet = new DatagramPacket(buf, buf.length, address, BROADCAST_PORT);
        socket.send(packet);

        Thread.sleep(delayMs);
      }
    } catch (final IOException e) {
      Logger.getInstance().error("UDP Broadcast I/O error: {0}", e.getMessage());
    } catch (final InterruptedException e) {
      Logger.getInstance().info("UDP Broadband interrupted.");
      Thread.currentThread().interrupt();
    }
  }

  /** Sets running flag to false for graceful exit. */
  public void stop() {
    running.set(false);
  }
}
>>>>>>> 2d8a3a6a0ebd92ea8c6853a1166d601ec4c89456
