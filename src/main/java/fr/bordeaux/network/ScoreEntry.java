package fr.bordeaux.network;

/** Stores scoreboard statistics for one player. */
public class ScoreEntry {

  private final String playerId;
  private String playerName;
  private int wins;
  private int losses;
  private int gamesPlayed;

  /**
   * Creates a new scoreboard entry for a player.
   *
   * @param playerId player identifier
   * @param playerName player name
   */
  public ScoreEntry(String playerId, String playerName) {
    this.playerId = playerId;
    this.playerName = playerName;
    this.wins = 0;
    this.losses = 0;
    this.gamesPlayed = 0;
  }

  /** Records a win and increments the number of played games. */
  public void recordWin() {
    wins++;
    gamesPlayed++;
  }

  /** Records a loss and increments the number of played games. */
  public void recordLoss() {
    losses++;
    gamesPlayed++;
  }

  /**
   * Returns the player identifier.
   *
   * @return player ID
   */
  public String getPlayerId() {
    return playerId;
  }

  /**
   * Returns the player name.
   *
   * @return player name
   */
  public String getPlayerName() {
    return playerName;
  }

  /**
   * Updates the player name.
   *
   * @param playerName new player name
   */
  public void setPlayerName(String playerName) {
    this.playerName = playerName;
  }

  /**
   * Returns the number of wins the player has.
   *
   * @return number of wins
   */
  public int getWins() {
    return wins;
  }

  /**
   * Returns the number of losses the player has.
   *
   * @return number of losses
   */
  public int getLosses() {
    return losses;
  }

  /**
   * Returns the total number of games played by the player.
   *
   * @return total games played
   */
  public int getGamesPlayed() {
    return gamesPlayed;
  }

  @Override
  public String toString() {
    return playerId + " " + playerName + " W:" + wins + " L:" + losses + " G:" + gamesPlayed;
  }
}
