package fr.bordeaux.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a player in Quoridor. A player has a name, a pawn position, a number of remaining
 * walls, and a score.
 */
public class Player {
  private final String name;
  private Position position;
  private int remainingWalls = 10;
  private final Position startingPosition;
  private final TypePlayer typePlayer;
  private int score = 0;

  @JsonCreator
  public Player(
      @JsonProperty("name") String name,
      @JsonProperty("positionStart") Position positionStart,
      @JsonProperty("typaPlayer") TypePlayer typePlayer) {
    this.name = name;
    this.position = positionStart;
    this.startingPosition = positionStart;
    this.typePlayer = typePlayer;
  }

  public static List<Player> createDefaultPlayers() {
    List<Player> players = new ArrayList<>();
    Player p1 = new Player("Human", new Position(0, 4), TypePlayer.HUMAN);
    Player p2 = new Player("Bob", new Position(8, 4), TypePlayer.IA);
    players.add(p1);
    players.add(p2);
    return players;
  }

  public String getName() {
    return name;
  }

  public Position getStartingPosition() {
    return startingPosition;
  }

  public Position getPosition() {
    return position;
  }

  public void setPosition(Position position) {
    this.position = position;
  }

  public int getRemainingWalls() {
    return remainingWalls;
  }

  public void useWall() {
    if (remainingWalls > 0) remainingWalls--;
  }

  public void giveBackWall() {
    remainingWalls++;
  }

  public TypePlayer getTypePlayer() {
    return typePlayer;
  }

  public int getScore() {
    return score;
  }

  public void addScore(int points) {
    score += points;
  }

  public void resetScore() {
    score = 0;
  }

  @Override
  public String toString() {
    return String.format(
        "Player{name='%s', pos=%s, walls=%d, score=%d}", name, position, remainingWalls, score);
  }
}
