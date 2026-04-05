package fr.bordeaux.network;

import fr.bordeaux.core.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Multi-client Quoridor server allowing several players to connect, create games, and exchange
 * network messages.
 */
public class MultiGameServer {

  /** TCP server socket used to accept client connections. */
  private ServerSocket serverSocket;

  /** Indicates whether the server is currently running. */
  private volatile boolean running = false;

  /** UDP broadcaster announcing the server on the local network. */
  private ServerBroadcaster broadcaster;

  /** Thread responsible for running the server UDP broadcast. */
  private Thread broadcastThread;

  /** TCP port used by the server. */
  private int port;

  /** Lock used to synchronize access to the server shared data. */
  private final Object lock = new Object();

  /** Connected clients indexed by their identifier. */
  private final Map<String, ClientSession> clients = new HashMap<>();

  /** Running games indexed by their identifier. */
  private final Map<String, ServerGameSession> games = new HashMap<>();

  /** Scoreboard of players known by the server. */
  private final Map<String, ScoreEntry> scoreboard = new HashMap<>();

  /** Counter used to generate unique client identifiers. */
  private int nextClientId = 1;

  /** Counter used to generate unique game identifiers. */
  private int nextGameId = 1;

  /** Maximum inactivity duration before a client is disconnected. */
  private static final long CLIENT_TIMEOUT_MS = 60000;

  /** Delay between timeout checks. */
  private static final long TIMEOUT_CHECK_INTERVAL_MS = 5000;

  /** Default constructor. */
  public MultiGameServer() {}

  /**
   * Starts the multi-client server on the given TCP port.
   *
   * @param port TCP port used by the server
   */
  public void start(int port) {
    new Thread(
            () -> {
              try {
                this.port = port;
                serverSocket = new ServerSocket(port);
                running = true;

                System.out.println("Server started on port " + port);

                broadcaster = new ServerBroadcaster("Quoridor", port);
                broadcastThread = new Thread(broadcaster);
                broadcastThread.start();
                startTimeoutChecker();

                while (running) {
                  Socket socket = serverSocket.accept();

                  BufferedReader in =
                      new BufferedReader(new InputStreamReader(socket.getInputStream()));
                  PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                  String clientId;
                  synchronized (lock) {
                    clientId = "C" + nextClientId++;
                  }

                  ClientSession client = new ClientSession(clientId, socket, in, out);

                  synchronized (lock) {
                    clients.put(client.getId(), client);
                    scoreboard.put(
                        client.getId(), new ScoreEntry(client.getId(), client.getName()));
                  }

                  System.out.println("Client connected: " + clientId);

                  new Thread(() -> handleClient(client)).start();
                }

              } catch (IOException e) {
                if (running) {
                  System.out.println("Server error: " + e.getMessage());
                }
              }
            })
        .start();
  }

  /**
   * Handles all commands received from one connected client.
   *
   * @param client the connected client session
   */
  private void handleClient(ClientSession client) {
    try {
      String line;

      while ((line = client.getIn().readLine()) != null && running) {
        client.setLastPing(System.currentTimeMillis());
        System.out.println(client.getId() + " -> " + line);

        if (line.startsWith("HELLO ")) {
          processHello(client, line);
        } else if (line.equals("PING")) {
          processPing(client);
        } else if (line.equals("QUIT")) {
          processQuit(client);
          break;
        } else if (line.startsWith("MOVE ")) {
          processMove(client, line);
        } else {
          client.send("ERROR unknown command");
        }
      }

    } catch (IOException e) {
      System.out.println("Client disconnected: " + e.getMessage());
    } finally {
      unregisterClient(client);
    }
  }

  /**
   * Removes a client from the server and cleans up its active game if needed.
   *
   * @param client the disconnected client
   */
  private void unregisterClient(ClientSession client) {
    boolean removed;
    synchronized (lock) {
      removed = clients.remove(client.getId()) != null;
      if (!removed) {
        return;
      }

      String gameId = client.getCurrentGameId();
      if (gameId != null) {
        ServerGameSession game = games.get(gameId);
        if (game != null) {
          for (ClientSession player : game.getPlayers()) {
            if (!player.getId().equals(client.getId())) {
              player.setStatus(PlayerStatus.IDLE);
              player.setCurrentGameId(null);
              player.send("ERROR opponent disconnected");
            }
          }
          games.remove(gameId);
        }
      }
    }

    client.close();
    System.out.println("Client disconnected: " + client.getId());
  }

  /**
   * Registers the player name sent by a client and confirms the connection.
   *
   * @param client the connected client
   * @param line the received HELLO command
   */
  private void processHello(ClientSession client, String line) {
    String name = line.substring("HELLO ".length()).trim();
    if (name.isEmpty()) {
      client.send("ERROR invalid name");
      return;
    }

    synchronized (lock) {
      client.setName(name);
      ScoreEntry entry = scoreboard.get(client.getId());
      if (entry != null) {
        entry.setPlayerName(name);
      }
    }

    client.send("WELCOME " + client.getId());
  }

  /** Handles the PING command by sending PONG back to the client. */
  private void processPing(ClientSession client) {
    client.send("PONG");
  }

  /** Handles a QUIT command. */
  private void processQuit(ClientSession client) {
    client.send("BYE");
  }

  /** Validates and applies a move sent by a client. */
  private void processMove(ClientSession client, String line) {
    String moveText = line.substring("MOVE ".length()).trim();

    synchronized (lock) {
      String gameId = client.getCurrentGameId();
      if (gameId == null) {
        client.send("ERROR not in game");
        return;
      }

      ServerGameSession game = games.get(gameId);
      if (game == null) {
        client.send("ERROR game not found");
        return;
      }

      if (!game.isPlayerTurn(client.getId())) {
        client.send("ERROR not your turn");
        return;
      }

      try {
        Move move = NetworkMoveParser.parseMove(moveText);

        if (!game.applyMove(move)) {
          client.send("ERROR illegal move");
          return;
        }

        client.send("OK");

        for (ClientSession otherPlayer : game.getOtherPlayers(client.getId())) {
          otherPlayer.send("OPPONENT_MOVE " + moveText);
        }

        ClientSession winner = game.getWinnerSession();
        if (winner != null) {
          finishGame(game, winner);
        }

      } catch (IllegalArgumentException e) {
        client.send("ERROR invalid move format");
      }
    }
  }

  /**
   * Ends a game, updates the scoreboard and resets player states.
   *
   * @param game the finished game session
   * @param winner the winning client
   */
  private void finishGame(ServerGameSession game, ClientSession winner) {
    ScoreEntry winnerScore = scoreboard.get(winner.getId());
    if (winnerScore != null) {
      winnerScore.recordWin();
    }

    for (ClientSession player : game.getPlayers()) {
      if (!player.getId().equals(winner.getId())) {
        ScoreEntry loserScore = scoreboard.get(player.getId());
        if (loserScore != null) {
          loserScore.recordLoss();
        }
      }
    }

    for (ClientSession player : game.getPlayers()) {
      player.setStatus(PlayerStatus.IDLE);
      player.setCurrentGameId(null);

      if (player.getId().equals(winner.getId())) {
        player.send("GAME_OVER WIN");
      } else {
        player.send("GAME_OVER LOSE");
      }
    }

    games.remove(game.getGameId());
  }

  /**
   * Returns a textual summary of the server state.
   *
   * @return the port, number of connected clients and number of running games
   */
  public String getServerStatus() {
    synchronized (lock) {
      return "Port: "
          + port
          + "\nConnected clients: "
          + clients.size()
          + "\nRunning games: "
          + games.size();
    }
  }

  /**
   * Lists all connected players with their identifier, name and status.
   *
   * @return a textual representation of connected players
   */
  public String listPlayers() {
    synchronized (lock) {
      if (clients.isEmpty()) {
        return "No connected players.";
      }

      StringBuilder sb = new StringBuilder();
      for (ClientSession client : clients.values()) {
        sb.append(client.getId())
            .append(" ")
            .append(client.getName())
            .append(" ")
            .append(client.getStatus())
            .append("\n");
      }
      return sb.toString().trim();
    }
  }

  /**
   * Returns the current scoreboard of players who have played on the server.
   *
   * @return a textual representation of wins, losses and played games
   */
  public String getScoreboard() {
    synchronized (lock) {
      if (scoreboard.isEmpty()) {
        return "Scoreboard is empty.";
      }

      StringBuilder sb = new StringBuilder();
      for (ScoreEntry entry : scoreboard.values()) {
        sb.append(entry).append("\n");
      }
      return sb.toString().trim();
    }
  }

  /**
   * Creates a new game between the given available players.
   *
   * @param playerIds identifiers of the players
   * @return a success or error message
   */
  public String createGame(String... playerIds) {
    synchronized (lock) {
      if (playerIds.length != 2 && playerIds.length != 4) {
        return "ERROR need exactly 2 or 4 players";
      }

      java.util.Set<String> seenIds = new java.util.HashSet<>();
      java.util.List<ClientSession> sessions = new java.util.ArrayList<>();

      for (String playerId : playerIds) {
        if (!seenIds.add(playerId)) {
          return "ERROR duplicate player";
        }

        ClientSession player = clients.get(playerId);
        if (player == null) {
          return "ERROR player not found";
        }

        if (player.getStatus() != PlayerStatus.IDLE) {
          return "ERROR player not available";
        }

        sessions.add(player);
      }

      String gameId = "G" + nextGameId++;
      ServerGameSession game = new ServerGameSession(gameId, sessions);

      games.put(gameId, game);

      for (int i = 0; i < sessions.size(); i++) {
        ClientSession player = sessions.get(i);
        player.setStatus(PlayerStatus.INGAME);
        player.setCurrentGameId(gameId);
        player.send("GAME_START " + gameId + (i == 0 ? " YOUR_TURN" : " WAIT"));
      }

      return "Game created: " + gameId;
    }
  }

  /** Stops the server and closes all active client connections. */
  public void stop() {
    running = false;

    try {
      if (broadcaster != null) {
        broadcaster.stop();
      }

      if (broadcastThread != null && broadcastThread.isAlive()) {
        broadcastThread.interrupt();
      }

      synchronized (lock) {
        for (ClientSession client : clients.values()) {
          client.send("SERVER_STOPPED");
          client.close();
        }
        clients.clear();
        games.clear();
      }

      if (serverSocket != null) {
        serverSocket.close();
      }

    } catch (IOException ignored) {
    }

    System.out.println("Server stopped.");
  }

  /** Disconnects every client that has been inactive for more than one minute. */
  private void startTimeoutChecker() {
    Thread timeoutThread =
        new Thread(
            () -> {
              while (running) {
                List<ClientSession> expiredClients = new ArrayList<>();

                synchronized (lock) {
                  long now = System.currentTimeMillis();
                  for (ClientSession client : clients.values()) {
                    if (now - client.getLastPing() > CLIENT_TIMEOUT_MS) {
                      expiredClients.add(client);
                    }
                  }
                }

                for (ClientSession client : expiredClients) {
                  client.send("ERROR timeout");
                  client.close();
                  unregisterClient(client);
                }

                try {
                  Thread.sleep(TIMEOUT_CHECK_INTERVAL_MS);
                } catch (InterruptedException ignored) {
                  Thread.currentThread().interrupt();
                  return;
                }
              }
            });
    timeoutThread.setDaemon(true);
    timeoutThread.start();
  }
}
