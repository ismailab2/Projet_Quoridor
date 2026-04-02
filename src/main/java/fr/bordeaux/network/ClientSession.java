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

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Socket getSocket() {
    return socket;
  }

  public BufferedReader getIn() {
    return in;
  }

  public PrintWriter getOut() {
    return out;
  }

  public PlayerStatus getStatus() {
    return status;
  }

  public void setStatus(PlayerStatus status) {
    this.status = status;
  }

  public String getCurrentGameId() {
    return currentGameId;
  }

  public void setCurrentGameId(String currentGameId) {
    this.currentGameId = currentGameId;
  }

  public long getLastPing() {
    return lastPing;
  }

  public void setLastPing(long lastPing) {
    this.lastPing = lastPing;
  }
}
