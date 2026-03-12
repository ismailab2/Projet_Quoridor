package fr.bordeaux.core;

/** Represents a human player */
public class HumanPlayer extends Player {

  public HumanPlayer(String name, Position positionStart) {
    super(name, positionStart);
  }

  @Override
  public Player copy() {
    return new HumanPlayer(
        this.getName(),
        this.getPosition(),
        this.getStartingPosition(),
        this.getRemainingWalls(),
        this.getScore());
  }

  private HumanPlayer(String name, Position current, Position start, int walls, int score) {
    super(name, current, start, walls, score);
  }

  @Override
  public Move getNextMove(GameState state) {
    return null;
  }

  @Override
  public String toString() {
    return "Human" + super.toString();
  }

  @Override
  public Boolean isAI() {
    return false;
  }
}
