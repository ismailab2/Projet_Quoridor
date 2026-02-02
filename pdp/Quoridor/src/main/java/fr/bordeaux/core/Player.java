package fr.bordeaux.core;

public class Player {
  Position position;

  public Player(Position position) {
    this.position = position;
  }

  public Position getPlayerPosition() {
    return position;
  }

  public void setPlayerPosition(Position position) {
    this.position = position;
  }

  public boolean hasWon() {
    // nous allons implementés la logique de qui a gagner ici
    return true;
  }

  @Override
  public String toString() {
    return "Player{" + "position=" + position + '}';
  }
}
