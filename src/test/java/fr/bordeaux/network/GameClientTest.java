package fr.bordeaux.network;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GameClientTest {

  static class MockServer extends Thread {
    private int port;

    public MockServer(int port) {
      this.port = port;
    }

    public void run() {
      try (ServerSocket serverSocket = new ServerSocket(port)) {
        Socket client = serverSocket.accept();
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        PrintWriter out = new PrintWriter(client.getOutputStream(), true);
        String line = null;
        while ((line = in.readLine()) != null) {
          if (line.startsWith("PING")) {
            out.println("PONG");
          } else if (line.equals("QUIT")) {
            out.println("BYE");
            break;
          }
        }
        client.close();
      } catch (IOException ignored) {
      }
    }
  }

  private ByteArrayOutputStream outContent;

  @BeforeEach
  public void setUp() throws IOException {
    outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));
  }

  @AfterEach
  void restore() {
    System.setOut(System.out);
  }

  @Test
  void testJoinSuccess() throws Exception {
    int port = 12000;
    MockServer server = new MockServer(port);
    server.start();
    Thread.sleep(200);
    GameClient client = new GameClient();
    assertTrue(client.join("localhost", port));
  }

  @Test
  void testJoinFail() throws Exception {
    GameClient client = new GameClient();
    assertFalse(client.join("localhost", 9999));
  }

  @Test
  void testJoinInvalideHost() throws Exception {
    GameClient client = new GameClient();
    assertFalse(client.join("localhost_invalide", 9999));
  }

  @Test
  void testPingWithoutConnection() throws Exception {
    GameClient client = new GameClient();
    client.ping();
    assertTrue(outContent.toString().contains("Not connected"));
  }

  @Test
  void testPingSuccess() throws Exception {
    int port = 12001;
    MockServer server = new MockServer(port);
    server.start();
    Thread.sleep(200);

    GameClient client = new GameClient();
    client.join("localhost", port);
    client.ping();
    assertTrue(outContent.toString().contains("PONG"));
  }

  @Test
  void testQuitWithoutConnection() throws Exception {
    GameClient client = new GameClient();
    client.quit();
    assertTrue(outContent.toString().contains("Client exited"));
  }

  @Test
  void testQuitSuccess() throws Exception {
    int port = 12002;
    MockServer server = new MockServer(port);
    server.start();
    Thread.sleep(200);

    GameClient client = new GameClient();
    client.join("localhost", port);
    client.quit();
    String output = outContent.toString();
    assertTrue(output.contains("BYE"));
    assertTrue(output.contains("Client exited"));
  }

  @Test
  void testPingAfterClose() throws Exception {
    int port = 12003;

    new MockServer(port).start();
    Thread.sleep(200);

    GameClient client = new GameClient();
    client.join("localhost", port);

    client.quit();

    client.ping();

    assertTrue(outContent.toString().contains("Not connected"));
  }
}
