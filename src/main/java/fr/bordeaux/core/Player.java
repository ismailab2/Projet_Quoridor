package fr.bordeaux.core;

import java.util.ArrayList;
import java.util.List;

/** Player in Quoridor with name, position, walls, and score. */
public abstract class Player {

  private final Color color;
  private final String name;
  private Position position;
  private int remainingWalls = 10;
  private long remainingTime = 30 * 60 * 1000; // Time in milliseconds
  private final Position startingPosition;
  private int score = 0;
  private boolean disabled = false;

  /**
   * Constructs a new Player with a name and starting position.
   *
   * @param name The name of the player.
   * @param positionStart The initial starting position on the board.
   * @param color The color of the player.
   */
  public Player(final String name, final Position positionStart, final Color color) {
    this.name = name;
    this.position = positionStart;
    this.startingPosition = positionStart;
    this.color = color;
  }

  /**
   * Internal constructor for state restoration.
   *
   * @param name player name
   * @param currentPos current position
   * @param startPos starting position
   * @param walls remaining walls
   * @param score current score
   * @param color player color
   */
  protected Player(
      final String name,
      final Position currentPos,
      final Position startPos,
      final int walls,
      final int score,
      final Color color) {
    this.name = name;
    this.position = new Position(currentPos.getX(), currentPos.getY());
    this.startingPosition = startPos;
    this.remainingWalls = walls;
    this.score = score;
    this.color = color;
  }

  /**
   * Creates a default list of players for a standard 2-player game.
   *
   * @return A list containing two {@link HumanPlayer} instances.
   */
  public static List<Player> createDefaultPlayers() {
    final List<Player> players = new ArrayList<>();
    final Player player1 = new HumanPlayer("Human", new Position(0, 4), Color.WHITE);
    final Player player2 = new HumanPlayer("Bob", new Position(8, 4), Color.BLACK);
    players.add(player1);
    players.add(player2);
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
  public void setPosition(final Position position) {
    this.position = position;
  }

    /**
   * Gets the current disable boolean of the player's pawn.
   *
   * @return The current disable boolean.
   */
  public Boolean isDisabled() {
    return disabled;
  }

  /**
   * Sets the current disable boolean of the player's pawn.
   *
   * @param disBoolean The new boolean to set.
   */
  public void setDisabled(Boolean disBoolean) {
    this.disabled = disBoolean;
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
  public void setRemainingWalls(final int walls) {
    this.remainingWalls = walls;
  }

  /** Decrements the count of remaining walls if the player has any left. */
  public void useWall() {
    if (remainingWalls > 0) remainingWalls--;
  }

  /** Increments the count of remaining walls (for undo). */
  public void giveBackWall() {
    remainingWalls++;
  }

  /**
   * Retrieves the color assigned to the player.
   *
   * @return The color of the player.
   */
  public Color getColor() {
    return color;
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
   * Returns the player's remaining reflection time.
   *
   * @return The remaining time in milliseconds.
   */
  public long getRemainingTime() {
    return remainingTime;
  }

  /**
   * Sets the player's remaining reflection time.
   *
   * @param remainingTime The new remaining time in milliseconds.
   */
  public void setRemainingTime(final long remainingTime) {
    this.remainingTime = remainingTime;
  }

  /**
   * Adds points to the player's current score.
   *
   * @param points The number of points to add.
   */
  public void addScore(final int points) {
    score += points;
  }

  /** Resets the player's score to zero. */
  public void resetScore() {
    score = 0;
  }

  /**
   * Determines the player's next move.
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
