package fr.bordeaux.core;

public class Grid {
  private Position position;
  private Player player; // pion sur la case
  private boolean topWall;
  private boolean bottomWall;
  private boolean leftWall;
  private boolean rightWall;

  public Grid(Position position) {
    this.position = position;
    this.player = null;
    this.topWall = false;
    this.bottomWall = false;
    this.leftWall = false;
    this.rightWall = false;
  }

  public Position getPosition() {
    return position;
  }

  public Player getPlayer() {
    return player;
  }

  public void setPlayer(Player player) {
    this.player = player;
  }

  public boolean hasTopWall() {
    return topWall;
  }

  public void setTopWall(boolean topWall) {
    this.topWall = topWall;
  }

  public boolean hasBottomWall() {
    return bottomWall;
  }

  public void setBottomWall(boolean bottomWall) {
    this.bottomWall = bottomWall;
  }

  public boolean hasLeftWall() {
    return leftWall;
  }

  public void setLeftWall(boolean leftWall) {
    this.leftWall = leftWall;
  }

  public boolean hasRightWall() {
    return rightWall;
  }

  public void setRightWall(boolean rightWall) {
    this.rightWall = rightWall;
  }

  @Override
  public String toString() {
    return player != null ? player.getName().charAt(0) + "" : ".";
  }
}
