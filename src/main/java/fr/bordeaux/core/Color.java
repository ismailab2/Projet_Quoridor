package fr.bordeaux.core;

import fr.bordeaux.i18n.I18n;

/** Represents player colors in the Quoridor game. */
public enum Color {
  /** White color (ID 1). */
  WHITE(1),
  /** Black color (ID 2). */
  BLACK(2),
  /** Blue color (ID 3). */
  BLUE(3),
  /** Red color (ID 4). */
  RED(4);

  private final int id;

  /**
   * Constructs a color with an ID.
   *
   * @param id the unique integer ID.
   */
  Color(final int id) {
    this.id = id;
  }

  /**
   * Retrieves the integer ID of the color.
   *
   * @return the integer ID of the color.
   */
  public int getId() {
    return id;
  }

  /**
   * Returns the Color associated with the given ID.
   *
   * @param id the integer ID to look up.
   * @return the matching Color.
   * @throws IllegalArgumentException if the ID is invalid.
   */
  public static Color fromId(final int id) {
    for (Color color : values()) {
      if (color.id == id) {
        return color;
      }
    }
    throw new IllegalArgumentException(I18n.get("unknownColor") + id);
  }
}
