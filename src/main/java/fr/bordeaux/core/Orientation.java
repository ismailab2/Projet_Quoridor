package fr.bordeaux.core;

public enum Orientation {
  HORIZONTAL,
  VERTICAL;

  @Override
  public String toString() {
    return (this == HORIZONTAL) ? "h" : "v";
  }
}
