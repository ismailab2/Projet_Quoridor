package fr.bordeaux.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import fr.bordeaux.AI.MCTSPlayer;
import fr.bordeaux.AI.MinimaxPlayer;
import fr.bordeaux.AI.NeuralNetPlayer;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a player in Quoridor. A player has a name, a pawn position, a number of remaining
 * walls, and a score.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME, // Uses a name to identify a class
    include = JsonTypeInfo.As.PROPERTY, // Needs to be a property in the JSON
    property = "type" // The name of the property
    )
@JsonSubTypes({
  // Attributes the appropriate class to the name property
  @JsonSubTypes.Type(value = HumanPlayer.class, name = "human"),
  @JsonSubTypes.Type(value = MinimaxPlayer.class, name = "minimax"),
  @JsonSubTypes.Type(value = MCTSPlayer.class, name = "mcts"),
  @JsonSubTypes.Type(value = NeuralNetPlayer.class, name = "neural")
})
public abstract class Player {

  private final String name;
  private Position position;
  private int remainingWalls = 10;  
  private final Position startingPosition;
  private int score = 0;

  @JsonCreator
  public Player(
    @JsonProperty("name") String name, @JsonProperty("positionStart") Position positionStart) {
    this.name = name;
    this.position = positionStart;
    this.startingPosition = positionStart;
  }

  public static List<Player> createDefaultPlayers() {
    List<Player> players = new ArrayList<>();
    Player p1 = new HumanPlayer("Human", new Position(0, 4));
    Player p2 = new MinimaxPlayer("Bob", new Position(8, 4));
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

  @Override
  public String toString() {
    return String.format(
        "Player{name='%s', pos=%s, walls=%d, score=%d}", name, position, remainingWalls, score);
  }
}
