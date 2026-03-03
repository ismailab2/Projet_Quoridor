package fr.bordeaux.core;

import fr.bordeaux.AI.MinimaxPlayer;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a player in Quoridor. A player has a name, a pawn position, a number of remaining
 * walls, and a score.
 */
public abstract class Player {

  private final String name;
  private Position position;
  private int remainingWalls = 10;
  private final Position startingPosition;
  private int score = 0;

  public Player(String name, Position positionStart) {
    this.name = name;
    this.position = positionStart;
    this.startingPosition = positionStart;
  }

  // Constructor used for copy
  protected Player(String name, Position currentPos, Position startPos, int walls, int score) {
    this.name = name;
    this.position = new Position(currentPos.getX(), currentPos.getY());
    this.startingPosition = startPos;
    this.remainingWalls = walls;
    this.score = score;
  }

  public static List<Player> createDefaultPlayers() {
    List<Player> players = new ArrayList<>();
    Player p1 = new HumanPlayer("Human", new Position(0, 4));
    Player p2 = new MinimaxPlayer("Bob", new Position(8, 4), 2);
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

  public int getScore() {
    return score;
  }

  public void addScore(int points) {
    score += points;
  }

  public void resetScore() {
    score = 0;
  }

  public abstract Move getNextMove(GameState state);

  public abstract Boolean isAI();

  public abstract Player copy();

  @Override
  public String toString() {
    return String.format(
        "Player{name='%s', pos=%s, walls=%d, score=%d}", name, position, remainingWalls, score);
  }
}
