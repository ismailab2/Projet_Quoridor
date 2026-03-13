package fr.bordeaux.ai;

import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Move;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RandomPlayer extends AiPlayer {

  public RandomPlayer(String name, Position positionStart) {
    super(name, positionStart);
  }

  @Override
  public Move getNextMove(GameState state) {
    List<Move> l = state.generateLegalMoves();
    Random random = new Random();
    return l.get(random.nextInt(l.size()));
  }

  @Override
  public String toString() {
    return "Random AI " + super.toString();
  }

  @Override
  public Player copy() {
    return new RandomPlayer(
        this.getName(),
        this.getPosition(),
        this.getStartingPosition(),
        this.getRemainingWalls(),
        this.getScore());
  }

  private RandomPlayer(String name, Position current, Position start, int walls, int score) {
    super(name, current, start, walls, score);
  }

  @Override
  public Boolean isAI() {
    return true;
  }

  @Override
  public Map<String, Object> getSettings() {
    Map<String, Object> settings = new HashMap<>();
    settings.put("mode", "random");
    return settings;
  }
}
