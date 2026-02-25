package fr.bordeaux.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Represents a human player */
public class HumanPlayer extends Player {

  @JsonCreator
  public HumanPlayer(
      @JsonProperty("name") String name, @JsonProperty("positionStart") Position positionStart) {
    super(name, positionStart);
  }

  @Override
  public Move getNextMove(GameState state) {
    return null;
  }

  @Override
  public String toString() {
    return "Human" + super.toString();
  }
}
