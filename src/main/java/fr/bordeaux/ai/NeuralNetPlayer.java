package fr.bordeaux.ai;

import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Move;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;
import java.util.HashMap;
import java.util.Map;

/** Represents an Ai player with MCTS */
public class NeuralNetPlayer extends AiPlayer {

  /**
   * Public constructor to initialize a Neural Network-based player.
   *
   * @param name Name of the player.
   * @param positionStart Initial starting position on the board.
   */
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

  /**
   * Private constructor used for internal state management or cloning.
   *
   * @param name Name of the player.
   * @param current Current position of the player on the board.
   * @param start The initial starting position of the player.
   * @param walls Number of walls the player has remaining.
   * @param score The current score or evaluation value for the player.
   */
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
  public Map<String, Object> getSettings() { // return all settings of neuralNetwork
    Map<String, Object> settings = new HashMap<>();
    settings.put("mode", "neural-network");
    return settings;
  }
}
