package fr.bordeaux.network;

/** Stores scoreboard statistics for one player. */
public class ScoreEntry {

  private final String playerId;
  private String playerName;
  private int wins;
  private int losses;
  private int gamesPlayed;

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

  public String getPlayerId() {
    return playerId;
  }

  public String getPlayerName() {
    return playerName;
  }

  public void setPlayerName(String playerName) {
    this.playerName = playerName;
  }

  public int getWins() {
    return wins;
  }

  public int getLosses() {
    return losses;
  }

  public int getGamesPlayed() {
    return gamesPlayed;
  }

  @Override
  public String toString() {
    return playerId + " " + playerName + " W:" + wins + " L:" + losses + " G:" + gamesPlayed;
  }
}
