package fr.bordeaux.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/** Represents one connected client on the server side. */
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
   * Creates a client session for one connected player.
   *
   * @param id client identifier
   * @param socket client socket
   * @param in input reader
   * @param out output writer
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
   * Sends a text message to the client.
   *
   * @param message message to send
   */
  public void send(String message) {
    out.println(message);
  }

  /** Closes the client socket. */
  public void close() {
    try {
      socket.close();
    } catch (IOException ignored) {
    }
  }

  /**
   * Returns the client identifier.
   *
   * @return the client identifier
   */
  public String getId() {
    return id;
  }

  /**
   * Returns the player name.
   *
   * @return the player name
   */
  public String getName() {
    return name;
  }

  /**
   * Updates the player name.
   *
   * @param name new player name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the client socket.
   *
   * @return the client socket
   */
  public Socket getSocket() {
    return socket;
  }

  /**
   * Returns the input reader associated with the client.
   *
   * @return the input reader associated with the client
   */
  public BufferedReader getIn() {
    return in;
  }

  /**
   * Returns the output writer associated with the client.
   *
   * @return the output writer associated with the client
   */
  public PrintWriter getOut() {
    return out;
  }

  /**
   * Returns the current player status.
   *
   * @return the current player status
   */
  public PlayerStatus getStatus() {
    return status;
  }

  /**
   * Updates the player status.
   *
   * @param status new player status
   */
  public void setStatus(PlayerStatus status) {
    this.status = status;
  }

  /**
   * Returns the current game identifier.
   *
   * @return the current game identifier, or null if none
   */
  public String getCurrentGameId() {
    return currentGameId;
  }

  /**
   * Updates the current game identifier.
   *
   * @param currentGameId new game identifier
   */
  public void setCurrentGameId(String currentGameId) {
    this.currentGameId = currentGameId;
  }

  /**
   * Returns the timestamp of the last received ping.
   *
   * @return timestamp of the last received ping
   */
  public long getLastPing() {
    return lastPing;
  }

  /**
   * Modifie l'horodatage du dernier ping reçu.
   *
   * @param lastPing nouvel horodatage
   */
  public void setLastPing(long lastPing) {
    this.lastPing = lastPing;
  }
}
