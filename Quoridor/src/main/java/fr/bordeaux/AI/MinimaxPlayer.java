package fr.bordeaux.AI;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Move;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;

/** Represents an Ai player with Minimax */
public class MinimaxPlayer extends Player {

  @JsonCreator
  public MinimaxPlayer(
      @JsonProperty("name") String name, @JsonProperty("positionStart") Position positionStart) {
    super(name, positionStart);
  }

  @Override
  public Move getNextMove(GameState state) {
    return null;
  }

  @Override
  public Player copy() {
    return new MinimaxPlayer(
            this.getName(),
            this.getPosition(),
            this.getStartingPosition(),
            this.getRemainingWalls(),
            this.getScore()
    );
  }

  private MinimaxPlayer(String name, Position current, Position start, int walls, int score) {
    super(name, current, start, walls, score);
  }

  @Override
  public String toString() {
    return "AI " + super.toString();
  }

  @Override
  public Boolean isAI() {
    return true;
  }
}
