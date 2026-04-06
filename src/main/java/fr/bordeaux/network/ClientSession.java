package fr.bordeaux.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/** Represents a connected client with socket, streams, name, and status. */
public class ClientSession {

  private final String id;
  private String name;
  private final Socket socket;
  private final BufferedReader in;
  private final PrintWriter out;
  private PlayerStatus status;
  private String currentGameId;
  private long lastPing;

  /**
   * Creates a new client session for one connected player.
   *
   * @param id unique client identifier
   * @param socket client socket
   * @param in input reader from the client
   * @param out output writer to the client
   */
  public ClientSession(String id, Socket socket, BufferedReader in, PrintWriter out) {
    this.id = id;
    this.socket = socket;
    this.in = in;
    this.out = out;
    this.name = id;
    this.status = PlayerStatus.IDLE;
    this.currentGameId = null;
    this.lastPing = System.currentTimeMillis();
  }

  /**
   * Sends a message to the client.
   *
   * @param message text to send
   */
  public void send(String message) {
    out.println(message);
  }

  /** Closes the client connection. */
  public void close() {
    try {
      socket.close();
    } catch (IOException ignored) {
    }
  }

  /**
   * Returns the unique identifier of this client.
   *
   * @return the client ID
   */
  public String getId() {
    return id;
  }

  /**
   * Returns the name of this client.
   *
   * @return the client's name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of this client.
   *
   * @param name new client name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the socket associated with this client.
   *
   * @return the client socket
   */
  public Socket getSocket() {
    return socket;
  }

  /**
   * Returns the input reader for this client.
   *
   * @return the input stream reader
   */
  public BufferedReader getIn() {
    return in;
  }

  /**
   * Returns the output writer for this client.
   *
   * @return the output stream writer
   */
  public PrintWriter getOut() {
    return out;
  }

  /**
   * Returns the current status of this client.
   *
   * @return the player status
   */
  public PlayerStatus getStatus() {
    return status;
  }

  /**
   * Sets the current status of this client.
   *
   * @param status new player status
   */
  public void setStatus(PlayerStatus status) {
    this.status = status;
  }

  /**
   * Returns the identifier of the current game this client is in, or null if none.
   *
   * @return current game ID or null
   */
  public String getCurrentGameId() {
    return currentGameId;
  }

  /**
   * Sets the current game identifier for this client.
   *
   * @param currentGameId new game ID
   */
  public void setCurrentGameId(String currentGameId) {
    this.currentGameId = currentGameId;
  }

  /**
   * Returns the timestamp of the last ping received from this client.
   *
   * @return timestamp of last ping
   */
  public long getLastPing() {
    return lastPing;
  }

  /**
   * Updates the timestamp of the last received ping.
   *
   * @param lastPing new timestamp
   */
  public void setLastPing(long lastPing) {
    this.lastPing = lastPing;
  }
}
