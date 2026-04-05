package fr.bordeaux.network;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

  @Test
  void hello_sendsPlayerName() throws Exception {
    int port = 12004;
    MockServer server = new MockServer(port);
    server.start();
    Thread.sleep(200);

    GameClient client = new GameClient();
    client.join("localhost", port);

    // Send player name to server
    client.hello("Alice");
    client.quit();
    server.join(1000);

    assertTrue(server.getReceivedMessages().contains("HELLO Alice"));
  }

  @Test
  void sendMove_sendsMoveText() throws Exception {
    int port = 12005;
    MockServer server = new MockServer(port);
    server.start();
    Thread.sleep(200);

    GameClient client = new GameClient();
    client.join("localhost", port);

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

    assertTrue(output.contains("Broadcast sent, waiting for responses..."));
    assertTrue(output.contains("Server found: testServer:9000"));
  }
}
