package fr.bordeaux.network;

import fr.bordeaux.core.Board;
import fr.bordeaux.core.Color;
import fr.bordeaux.core.GameEngine;
import fr.bordeaux.core.GameState;
import fr.bordeaux.core.HumanPlayer;
import fr.bordeaux.core.Move;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;
import java.util.ArrayList;
import java.util.List;

/** Represents one active Quoridor game hosted by the server. */
public class ServerGameSession {

  private final String gameId;
  private final GameState gameState;
  private final GameEngine gameEngine;
  private final List<ClientSession> players;

  /**
   * Creates a new server-side game session for the given connected players.
   *
   * @param gameId identifier of the game session
   * @param sessions client sessions participating in the game
   */
  public ServerGameSession(String gameId, List<ClientSession> sessions) {
    this.gameId = gameId;
    this.players = new ArrayList<>(sessions);

    Board board = new Board(9);
    List<Player> gamePlayers = new ArrayList<>();

    if (sessions.size() >= 1) {
      gamePlayers.add(new HumanPlayer(sessions.get(0).getName(), new Position(0, 4), Color.WHITE));
    }

    if (sessions.size() >= 2) {
      gamePlayers.add(new HumanPlayer(sessions.get(1).getName(), new Position(8, 4), Color.BLACK));
    }

    this.gameState = new GameState(board, gamePlayers, 0);
    this.gameEngine = new GameEngine(gameState);
  }

  public String getGameId() {
    return gameId;
  }

  public GameState getGameState() {
    return gameState;
  }

  public GameEngine getGameEngine() {
    return gameEngine;
  }

  public List<ClientSession> getPlayers() {
    return players;
  }

  public boolean containsPlayer(String playerId) {
    for (ClientSession player : players) {
      if (player.getId().equals(playerId)) {
        return true;
      }
    }
    return false;
  }

  public ClientSession getOpponent(String playerId) {
    for (ClientSession player : players) {
      if (!player.getId().equals(playerId)) {
        return player;
      }
    }
    return null;
  }

  public ClientSession getCurrentPlayerSession() {
    int currentIndex = gameState.getCurrentIndexPlayer();
    if (currentIndex < 0 || currentIndex >= players.size()) {
      return null;
    }
    return players.get(currentIndex);
  }

  /**
   * Returns the winning client session if a player has reached the goal.
   *
   * @return the winner session, or null if the game is not finished
   */
  public ClientSession getWinnerSession() {
    for (int i = 0; i < gameState.getPlayers().size(); i++) {
      Player player = gameState.getPlayers().get(i);
      if (gameState.hasReachedGoal(player)) {
        return players.get(i);
      }
    }
    return null;
  }

  /**
   * Checks whether the specified player is the current player.
   *
   * @param playerId identifier of the player
   * @return true if it is this player's turn
   */
  public boolean isPlayerTurn(String playerId) {
    ClientSession current = getCurrentPlayerSession();
    return current != null && current.getId().equals(playerId);
  }

  /**
   * Applies a move through the server-side game engine.
   *
   * @param move move to validate and apply
   * @return true if the move is legal and was applied
   */
  public boolean applyMove(Move move) {
    return gameEngine.play(move);
  }

  public boolean isFinished() {
    return gameState.isGameOver();
  }
}
