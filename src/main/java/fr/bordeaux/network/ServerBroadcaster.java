package fr.bordeaux.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

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

  /** Flag controlling the broadcasting loop */
  private volatile boolean running = true;

  /** time */
  private final int delayMs;

  /**
   * Broadcasts the server's presence on the local network every 10 seconds.
   *
   * @param name Name of the server.
   * @param port TCP port of the server.
   */
  public ServerBroadcaster(String name, int port) {
    this(name, port, 10000);
  }

  /**
   * Broadcasts the server's presence on the local network every 10 seconds.
   *
   * @param name Name of the server.
   * @param port TCP port of the server.
   * @param delayMs time.
   */
  public ServerBroadcaster(String name, int port, int delayMs) {
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
      InetAddress address = InetAddress.getByName("127.0.0.1"); // localhost pour test

      while (running) {
        String msg = name + ":" + port;
        byte[] buf = msg.getBytes();

        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 12346);
        socket.send(packet);

        Thread.sleep(delayMs);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Sets the running flag to false, causing the thread to exit gracefully on the next iteration.
   */
  public void stop() {
    running = false;
  }
}
