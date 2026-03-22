package fr.bordeaux.core;

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

  /**
   * Constructs a new Player with a name and an initial starting position.
   *
   * @param name The name of the player.
   * @param positionStart The initial starting position on the board.
   */
  public Player(String name, Position positionStart) {
    this.name = name;
    this.position = positionStart;
    this.startingPosition = positionStart;
  }

  /**
   * Protected constructor used for creating deep copies of a player.
   *
   * @param name The name of the player.
   * @param currentPos The current position of the player's pawn.
   * @param startPos The player's initial starting position.
   * @param walls The number of remaining walls.
   * @param score The current player score.
   */
  protected Player(String name, Position currentPos, Position startPos, int walls, int score) {
    this.name = name;
    this.position = new Position(currentPos.getX(), currentPos.getY());
    this.startingPosition = startPos;
    this.remainingWalls = walls;
    this.score = score;
  }

  /**
   * Creates a default list of players for a standard 2-player game.
   *
   * @return A list containing two {@link HumanPlayer} instances.
   */
  public static List<Player> createDefaultPlayers() {
    List<Player> players = new ArrayList<>();
    Player p1 = new HumanPlayer("Human", new Position(0, 4));
    Player p2 = new HumanPlayer("Bob", new Position(8, 4));
    players.add(p1);
    players.add(p2);
    return players;
  }

  /**
   * Gets the name of the player.
   *
   * @return The player's name.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the initial starting position of the player.
   *
   * @return The starting {@link Position}.
   */
  public Position getStartingPosition() {
    return startingPosition;
  }

  /**
   * Gets the current position of the player's pawn.
   *
   * @return The current {@link Position}.
   */
  public Position getPosition() {
    return position;
  }

  /**
   * Sets the current position of the player's pawn.
   *
   * @param position The new {@link Position} to set.
   */
  public void setPosition(Position position) {
    this.position = position;
  }

  /**
   * Gets the number of walls the player has remaining.
   *
   * @return The count of available walls.
   */
  public int getRemainingWalls() {
    return remainingWalls;
  }

  /**
   * Sets the number of walls remaining
   *
   * @param walls The count of wanted walls
   */
  public void setRemainingWalls(int walls) {
    this.remainingWalls = walls;
  }

  /** Decrements the count of remaining walls if the player has any left. */
  public void useWall() {
    if (remainingWalls > 0) remainingWalls--;
  }

  /** Increments the count of remaining walls. Typically used during an undo operation. */
  public void giveBackWall() {
    remainingWalls++;
  }

  /**
   * Gets the current score of the player.
   *
   * @return The player's score.
   */
  public int getScore() {
    return score;
  }

  /**
   * Adds points to the player's current score.
   *
   * @param points The number of points to add.
   */
  public void addScore(int points) {
    score += points;
  }

  /** Resets the player's score to zero. */
  public void resetScore() {
    score = 0;
  }

  /**
   * Abstract method to determine the player's next move.
   *
   * @param state The current state of the game.
   * @return The selected {@link Move}.
   */
  public abstract Move getNextMove(GameState state);

  /**
   * Checks if the player is controlled by AI.
   *
   * @return {@code true} if the player is an AI, {@code false} otherwise.
   */
  public abstract Boolean isAI();

  /**
   * Creates a deep copy of the player.
   *
   * @return A new {@link Player} instance with the same internal state.
   */
  public abstract Player copy();

  /**
   * Returns a string representation of the player.
   *
   * @return A formatted string with player details.
   */
  @Override
  public String toString() {
    return String.format(
        "Player{name='%s', pos=%s, walls=%d, score=%d}", name, position, remainingWalls, score);
  }
}
