package fr.bordeaux.network;

import fr.bordeaux.core.Move;
import fr.bordeaux.util.Logger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/** Multi-client server for connectivity, creation, and communication. */
public class MultiGameServer {

  /** TCP server socket used to accept client connections. */
  private ServerSocket serverSocket;

  /** Indicates whether the server is currently running. */
  private final AtomicBoolean running;

  /** UDP broadcaster announcing the server on the local network. */
  private ServerBroadcaster broadcaster;

  /** Thread responsible for running the server UDP broadcast. */
  private Thread broadcastThread;

  /** TCP port used by the server. */
  private int port;

  /** Lock used to synchronize access to the server shared data. */
  private final Object lock = new Object();

  /** Connected clients indexed by their identifier. */
  private final Map<String, ClientSession> clients;

  /** Running games indexed by their identifier. */
  private final Map<String, ServerGameSession> games;

  /** Scoreboard of players known by the server. */
  private final Map<String, ScoreEntry> scoreboard;

  /** Counter used to generate unique client identifiers. */
  private int nextClientId = 1;

  /** Counter used to generate unique game identifiers. */
  private int nextGameId = 1;

  /** Maximum inactivity duration before a client is disconnected. */
  private static final long CLIENT_TIMEOUT_MS = 60000;

  /** Delay between timeout checks. */
  private static final long TIMEOUT_CHECK_INTERVAL_MS = 5000;

  private static final String CLIENT_ID_PREFIX = "C";
  private static final String GAME_ID_PREFIX = "G";
  private static final int MIN_PLAYERS = 2;
  private static final int MAX_PLAYERS = 4;

  /** Default constructor. */
  public MultiGameServer() {
    this.running = new AtomicBoolean(false);
    this.clients = new HashMap<>();
    this.games = new HashMap<>();
    this.scoreboard = new HashMap<>();
  }

  /**
   * Starts the multi-client server on the given TCP port.
   *
   * @param port TCP port used by the server
   */
  public void start(final int port) {
    final Thread serverThread =
        new Thread(
            () -> {
              try {
                this.port = port;
                serverSocket = new ServerSocket(port);
                running.set(true);

                Logger.getInstance().info("Server started on port " + port);

                broadcaster = new ServerBroadcaster("Quoridor", port);
                broadcastThread = new Thread(broadcaster);
                broadcastThread.start();
                startTimeoutChecker();

                while (running.get()) {
                  final Socket socket = serverSocket.accept();

                  final BufferedReader reader =
                      new BufferedReader(
                          new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                  final PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

                  final String clientId;
                  synchronized (lock) {
                    clientId = CLIENT_ID_PREFIX + nextClientId;
                    nextClientId++;
                  }

                  final ClientSession client = new ClientSession(clientId, socket, reader, writer);

                  synchronized (lock) {
                    final String cid = client.getId();
                    clients.put(cid, client);
                    scoreboard.put(cid, new ScoreEntry(cid, client.getName()));
                  }

                  if (Logger.getInstance().isInfoEnabled()) {
                    Logger.getInstance().info("Client connected: {0}", clientId);
                  }

                  final Thread clientThread =
                      new Thread(
                          () -> {
                            handleClient(client);
                          });
                  clientThread.start();
                }

              } catch (final IOException e) {
                if (running.get()) {
                  if (Logger.getInstance().isErrorEnabled()) {
                    Logger.getInstance().error("Server error: {0}", e.getMessage());
                  }
                }
              }
            });
    serverThread.start();
  }

  /**
   * Handles all commands received from one connected client.
   *
   * @param client the connected client session
   */
  private void handleClient(final ClientSession client) {
    try {
      final BufferedReader reader = client.getIn();
      while (this.running.get()) {
        final String line = reader.readLine();
        if (line == null) {
          break;
        }
        client.setLastPing(System.currentTimeMillis());
        if (Logger.getInstance().isInfoEnabled()) {
          Logger.getInstance().info("{0} -> {1}", client.getId(), line);
        }

        if (line.startsWith("HELLO ")) {
          processHello(client, line);
        } else if ("PING".equals(line)) {
          processPing(client);
        } else if ("QUIT".equals(line)) {
          processQuit(client);
          break;
        } else if (line.startsWith("MOVE ")) {
          processMove(client, line);
        } else {
          client.send("ERROR unknown command");
        }
      }
    } catch (final IOException e) {
      if (Logger.getInstance().isInfoEnabled()) {
        Logger.getInstance().info("Client disconnected: {0}", e.getMessage());
      }
    } finally {
      unregisterClient(client);
    }
  }

  /**
   * Removes a client from the server and cleans up its active game if needed.
   *
   * @param client the disconnected client
   */
  private void unregisterClient(final ClientSession client) {
    final boolean removed;
    final String clientId = client.getId();
    synchronized (lock) {
      removed = clients.remove(clientId) != null;
      if (removed) {
        final String gameId = client.getCurrentGameId();
        if (gameId != null) {
          final ServerGameSession game = games.get(gameId);
          if (game != null) {
            for (final ClientSession player : game.getPlayers()) {
              if (!player.getId().equals(clientId)) {
                player.setStatus(PlayerStatus.IDLE);
                player.setCurrentGameId(null);
                player.send("ERROR opponent disconnected");
              }
            }
            games.remove(gameId);
          }
        }
      }
    }

    client.close();
    if (Logger.getInstance().isInfoEnabled()) {
      Logger.getInstance().info("Client disconnected: {0}", clientId);
    }
  }

  /**
   * Registers the player name sent by a client and confirms the connection.
   *
   * @param client the connected client
   * @param line the received HELLO command
   */
  private void processHello(final ClientSession client, final String line) {
    final String name = line.substring("HELLO ".length()).trim();
    if (name.isEmpty()) {
      client.send("ERROR invalid name");
    } else {
      final String clientId = client.getId();
      synchronized (lock) {
        client.setName(name);
        final ScoreEntry entry = scoreboard.get(clientId);
        if (entry != null) {
          entry.setPlayerName(name);
        }
      }
      client.send("WELCOME " + clientId);
    }
  }

  /** Handles the PING command by sending PONG back to the client. */
  private void processPing(final ClientSession client) {
    client.send("PONG");
  }

  /** Handles a QUIT command. */
  private void processQuit(final ClientSession client) {
    client.send("BYE");
  }

  /** Validates and applies a move sent by a client. */
  private void processMove(final ClientSession client, final String line) {
    final String moveText = line.substring("MOVE ".length()).trim();
    final String clientId = client.getId();

    synchronized (lock) {
      final String gameId = client.getCurrentGameId();
      if (gameId == null) {
        client.send("ERROR not in game");
      } else {
        final ServerGameSession game = games.get(gameId);
        if (game == null) {
          client.send("ERROR game not found");
        } else if (!game.isPlayerTurn(clientId)) {
          client.send("ERROR not your turn");
        } else {
          try {
            final Move move = NetworkMoveParser.parseMove(moveText);
            if (!game.applyMove(move)) {
              client.send("ERROR illegal move");
            } else {
              client.send("OK");
              for (final ClientSession otherPlayer : game.getOtherPlayers(clientId)) {
                otherPlayer.send("OPPONENT_MOVE " + moveText);
              }
              final ClientSession winner = game.getWinnerSession();
              if (winner != null) {
                finishGame(game, winner);
              }
            }
          } catch (final IllegalArgumentException e) {
            client.send("ERROR invalid move format");
          }
        }
      }
    }
  }

  /**
   * Ends a game, updates the scoreboard and resets player states.
   *
   * @param game the finished game session
   * @param winner the winning client
   */
  private void finishGame(final ServerGameSession game, final ClientSession winner) {
    final String winnerId = winner.getId();
    final ScoreEntry winnerScore = scoreboard.get(winnerId);
    if (winnerScore != null) {
      winnerScore.recordWin();
    }

    for (final ClientSession player : game.getPlayers()) {
      final String pid = player.getId();
      if (!pid.equals(winnerId)) {
        final ScoreEntry loserScore = scoreboard.get(pid);
        if (loserScore != null) {
          loserScore.recordLoss();
        }
      }
    }

    for (final ClientSession player : game.getPlayers()) {
      player.setStatus(PlayerStatus.IDLE);
      player.setCurrentGameId(null);

      if (player.getId().equals(winnerId)) {
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

      final StringBuilder statusBuilder = new StringBuilder();
      for (final ClientSession client : clients.values()) {
        statusBuilder
            .append(client.getId())
            .append(" ")
            .append(client.getName())
            .append(" ")
            .append(client.getStatus())
            .append("\n");
      }
      return statusBuilder.toString().trim();
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

      final StringBuilder scoreBuilder = new StringBuilder();
      for (final ScoreEntry entry : scoreboard.values()) {
        scoreBuilder.append(entry).append("\n");
      }
      return scoreBuilder.toString().trim();
    }
  }

  /**
   * Creates a new game between the given available players.
   *
   * @param playerIds identifiers of the players
   * @return a success or error message
   */
  public String createGame(final String... playerIds) {
    String result = "";
    synchronized (lock) {
      final int playerCount = playerIds.length;
      if (playerCount < MIN_PLAYERS || playerCount > MAX_PLAYERS) {
        result = "ERROR need between 2 and 4 players";
      } else {
        final java.util.Set<String> seenIds = new java.util.HashSet<>();
        final List<ClientSession> sessions = new ArrayList<>();
        boolean errorFound = false;

        for (final String playerId : playerIds) {
          if (!seenIds.add(playerId)) {
            result = "ERROR duplicate player";
            errorFound = true;
            break;
          }

          final ClientSession player = clients.get(playerId);
          if (player == null) {
            result = "ERROR player not found";
            errorFound = true;
            break;
          }

          if (player.getStatus() != PlayerStatus.IDLE) {
            result = "ERROR player not available";
            errorFound = true;
            break;
          }

          sessions.add(player);
        }

        if (!errorFound) {
          final String gameId = GAME_ID_PREFIX + nextGameId;
          nextGameId++;
          final ServerGameSession game = new ServerGameSession(gameId, sessions);
          games.put(gameId, game);

          for (int index = 0; index < sessions.size(); index++) {
            final ClientSession session = sessions.get(index);
            session.setStatus(PlayerStatus.INGAME);
            session.setCurrentGameId(gameId);
            final String turnInfo = (index == 0) ? " YOUR_TURN" : " WAIT";
            session.send("GAME_START " + gameId + turnInfo);
          }
          result = "Game created: " + gameId;
        }
      }
    }
    return result;
  }

  /** Stops the server and closes all active client connections. */
  public void stop() {
    running.set(false);

    try {
      if (broadcaster != null) {
        broadcaster.stop();
      }

      if (broadcastThread != null && broadcastThread.isAlive()) {
        broadcastThread.interrupt();
      }

      synchronized (lock) {
        for (final ClientSession client : clients.values()) {
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

    Logger.getInstance().info("Server stopped.");
  }

  /** Disconnects every client that has been inactive for more than one minute. */
  private void startTimeoutChecker() {
    final Thread timeoutThread =
        new Thread(
            () -> {
              while (running.get()) {
                final List<ClientSession> expiredClients = new ArrayList<>();

                synchronized (lock) {
                  final long now = System.currentTimeMillis();
                  for (final ClientSession client : clients.values()) {
                    if (now - client.getLastPing() > CLIENT_TIMEOUT_MS) {
                      expiredClients.add(client);
                    }
                  }
                }

                for (final ClientSession client : expiredClients) {
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
            },
            "TimeoutChecker");
    timeoutThread.setDaemon(true);
    timeoutThread.start();
  }
}
