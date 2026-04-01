package fr.bordeaux.core;

/** Represents a human player */
public class HumanPlayer extends Player {

  /**
   * Constructs a new HumanPlayer with a name and a starting position.
   *
   * @param name The name of the player.
   * @param positionStart The initial starting position on the board.
   * @param color The color of the player.
   * @param walls The amount of walls the player has.
   */
  public HumanPlayer(String name, Position positionStart, Color color, int walls) {
    super(name, positionStart, color, walls);
  }

  @Override
  public Player copy() {
    return new HumanPlayer(
        this.getName(),
        this.getPosition(),
        this.getStartingPosition(),
        this.getRemainingWalls(),
        this.getScore(),
        this.getColor());
  }

  /**
   * Private constructor used for internal state management and cloning.
   *
   * @param name The name of the player.
   * @param current The current position of the player on the board.
   * @param start The initial starting position.
   * @param walls The number of remaining walls.
   * @param score The current score of the player.
   */
  private HumanPlayer(
      String name, Position current, Position start, int walls, int score, Color color) {
    super(name, current, start, walls, score, color);
  }

  /**
   * Human players do not automate moves. Moves are expected to be handled via the user interface.
   *
   * @param state The current game state.
   * @return Always {@code null} as humans decide moves externally.
   */
  @Override
  public Move getNextMove(GameState state) {
    return null;
  }

  /**
   * Returns a string representation of the human player.
   *
   * @return A string starting with "Human" followed by player details.
   */
  @Override
  public String toString() {
    return "Human" + super.toString();
  }

  /**
   * Indicates that this player is not an AI.
   *
   * @return {@code false} as this is a human player.
   */
  @Override
  public Boolean isAI() {
    return false;
  }
}
