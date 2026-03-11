package fr.bordeaux.AI;

import java.util.HashMap;
import java.util.Map;

import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Move;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;

/** Represents an Ai player with MCTS */
public class NeuralNetPlayer extends AiPlayer {

  public NeuralNetPlayer(String name, Position positionStart) {
    super(name, positionStart);
  }

  @Override
  public Player copy() {
    return new NeuralNetPlayer(
        this.getName(),
        this.getPosition(),
        this.getStartingPosition(),
        this.getRemainingWalls(),
        this.getScore());
  }

  private NeuralNetPlayer(String name, Position current, Position start, int walls, int score) {
    super(name, current, start, walls, score);
  }

  @Override
  public Move getNextMove(GameState state) {
    return null;
  }

  @Override
  public String toString() {
    return "AI " + super.toString();
  }

  @Override
  public Boolean isAI() {
    return true;
  }

  @Override
  public Map<String, Object> getSettings() { // return all settings of neuralNetwork
    Map<String, Object> settings = new HashMap<>();
    settings.put("mode", "neural-network");
    return settings;
  }
}
