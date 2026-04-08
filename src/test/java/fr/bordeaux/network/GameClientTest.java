package fr.bordeaux.network;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.bordeaux.i18n.I18n;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GameClientTest {

  static class MockServer extends Thread {
    private final int port;
    private final List<String> receivedMessages = new ArrayList<>();

    public MockServer() {
      this.port = 0;
    }

    private int actualPort;

    public int getPort() {
      return actualPort;
    }

    public void run() {
      try (ServerSocket serverSocket = new ServerSocket(0)) {
        actualPort = serverSocket.getLocalPort();
        synchronized (this) {
          this.notifyAll();
        }
        Socket client = serverSocket.accept();
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        PrintWriter out = new PrintWriter(client.getOutputStream(), true);
        String line = null;
        while ((line = in.readLine()) != null) {
          receivedMessages.add(line);
          if (line.startsWith("PING")) {
            out.println("PONG");
          } else if (line.startsWith("HELLO ")) {
            out.println("WELCOME");
          } else if (line.startsWith("MOVE ")) {
            out.println("OK");
          } else if (line.equals("QUIT")) {
            out.println("BYE");
            break;
          }
        }
        client.close();
      } catch (IOException ignored) {
      }
    }

    public List<String> getReceivedMessages() {
      return receivedMessages;
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
    MockServer server = new MockServer();
    server.start();
    synchronized (server) {
      if (server.getPort() == 0) {
        server.wait(1000);
      }
    }
    int port = server.getPort();
    GameClient client = new GameClient();
    assertTrue(client.join("localhost", port), "Join should succeed");
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
    assertTrue(outContent.toString().contains(I18n.get("notCo")));
  }

  @Test
  void testPingSuccess() throws Exception {
    MockServer server = new MockServer();
    server.start();
    synchronized (server) {
      if (server.getPort() == 0) {
        server.wait(1000);
      }
    }
    int port = server.getPort();

    GameClient client = new GameClient();
    assertTrue(client.join("localhost", port), "Join should succeed");
    client.ping();
    String output = outContent.toString();
    assertTrue(output.contains("PONG"), "Output should contain PONG, but was: " + output);
  }

  @Test
  void testQuitWithoutConnection() throws Exception {
    GameClient client = new GameClient();
    client.quit();
    assertTrue(outContent.toString().contains(I18n.get("clientQuit")));
  }

  @Test
  void testQuitSuccess() throws Exception {
    MockServer server = new MockServer();
    server.start();
    synchronized (server) {
      if (server.getPort() == 0) {
        server.wait(1000);
      }
    }
    int port = server.getPort();

    GameClient client = new GameClient();
    assertTrue(client.join("localhost", port), "Join should succeed");
    client.quit();
    String output = outContent.toString();
    assertTrue(output.contains("BYE"));
    assertTrue(output.contains(I18n.get("clientQuit")));
  }

  @Test
  void testPingAfterClose() throws Exception {
    MockServer server = new MockServer();
    server.start();
    synchronized (server) {
      if (server.getPort() == 0) {
        server.wait(1000);
      }
    }
    int port = server.getPort();

    GameClient client = new GameClient();
    assertTrue(client.join("localhost", port), "Join should succeed");

    client.quit();

    client.ping();

    assertTrue(outContent.toString().contains(I18n.get("notCo")));
  }

  @Test
  void hello_sendsPlayerName() throws Exception {
    MockServer server = new MockServer();
    server.start();
    synchronized (server) {
      if (server.getPort() == 0) {
        server.wait(1000);
      }
    }
    int port = server.getPort();

    GameClient client = new GameClient();
    assertTrue(client.join("localhost", port), "Join should succeed");

    // Send player name to server
    client.hello("Alice");
    client.quit();
    server.join(1000);

    assertTrue(server.getReceivedMessages().contains("HELLO Alice"));
  }

  @Test
  void sendMove_sendsMoveText() throws Exception {
    MockServer server = new MockServer();
    server.start();
    synchronized (server) {
      if (server.getPort() == 0) {
        server.wait(1000);
      }
    }
    int port = server.getPort();

    GameClient client = new GameClient();
    assertTrue(client.join("localhost", port), "Join should succeed");

    // Send one move to server
    client.sendMove("e2-e3");
    client.quit();
    server.join(1000);

    assertTrue(server.getReceivedMessages().contains("MOVE e2-e3"));
  }

  @Test
  void serverList_findsOneServer() throws Exception {
    Thread fakeServer =
        new Thread(
            () -> {
              try (DatagramSocket socket =
                  new DatagramSocket(12346, InetAddress.getByName("0.0.0.0"))) {

                // Wait for DISCOVER message
                byte[] buffer = new byte[1024];
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);

                String message = new String(request.getData(), 0, request.getLength());

                // Send back one fake server
                if ("DISCOVER".equals(message)) {
                  byte[] response = "testServer:9000".getBytes();
                  DatagramPacket reply =
                      new DatagramPacket(
                          response, response.length, request.getAddress(), request.getPort());
                  socket.send(reply);
                }

              } catch (IOException ignored) {
              }
            });

    fakeServer.start();

    GameClient client = new GameClient();
    client.serverList();

    Thread.sleep(1000);
    fakeServer.join(1000);

    String output = outContent.toString();

    assertTrue(output.contains(I18n.get("broadcast")));
    assertTrue(output.contains(I18n.get("found") + "testServer:9000"));
  }

  // Tests reseau client ajoutes

  @Test
  void connectedFalseByDefault() {
    GameClient client = new GameClient();

    assertFalse(client.isConnected());
  }

  @Test
  void helloWithoutConnection() {
    GameClient client = new GameClient();

    assertDoesNotThrow(() -> client.hello("Alice"));
    assertTrue(outContent.toString().contains(I18n.get("notCo")));
  }

  @Test
  void sendMoveWithoutConnection() {
    GameClient client = new GameClient();

    assertDoesNotThrow(() -> client.sendMove("e1-e2"));
    assertTrue(outContent.toString().contains(I18n.get("notCo")));
  }

  /* @Test
  void handlerReceivesMessage() throws Exception {
    MockServer server = new MockServer();
    server.start();
    synchronized (server) {
      if (server.getPort() == 0) {
        server.wait(1000);
      }
    }

    GameClient client = new GameClient();
    List<String> messages = new ArrayList<>();
    client.setServerMessageHandler(messages::add);

    assertTrue(client.join("localhost", server.getPort()));

    client.hello("Alice");
    Thread.sleep(300);

    assertTrue(messages.contains("WELCOME"));

    client.quit();
  } */
}
