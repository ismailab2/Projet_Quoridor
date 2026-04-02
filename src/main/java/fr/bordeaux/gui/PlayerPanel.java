package fr.bordeaux.gui;

import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Player;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

/**
 * Represents the display panel for a player in the graphical interface.
 *
 * <p>This panel shows:
 *
 * <ul>
 *   <li>The player's name
 *   <li>The remaining time (minutes and seconds)
 *   <li>The number of walls left
 *   <li>The current score of the player
 * </ul>
 *
 * It automatically updates its information when changes occur in the {@link GameState}.
 */
public class PlayerPanel extends VBox {
  /** Label displaying the player's name. */
  private final Label nameLabel;

  /** Label displaying the player's score. */
  private final Label scoreLabel;

  /** Label displaying the number of walls remaining for the player. */
  private final Label wallsLabel;

  /** Label displaying the remaining time for the player. */
  private final Label timeLabel;

  /** Index of the player in the list of players in the GameState. */
  private final int playerIndex;

  /**
   * Constructs a panel for a given player.
   *
   * @param playerName The name of the player to display.
   * @param state The game state the player belongs to, used to automatically update the panel.
   * @param playerIndex The index of the player in the {@link GameState}'s list of players.
   */
  public PlayerPanel(String playerName, GameState state, int playerIndex) {
    this.playerIndex = playerIndex;

    String accentColor = (playerIndex == 0) ? "#e74c3c" : "#2c3e50";

    Region topBar = new Region();
    topBar.setPrefHeight(6);
    topBar.setMaxWidth(Double.MAX_VALUE);
    topBar.setStyle("-fx-background-color: " + accentColor + ";");

    nameLabel = new Label(playerName);
    nameLabel.setStyle(
        """
            -fx-font-size: 18px;
            -fx-font-weight: bold;
            -fx-text-fill: #222222;
        """);

    Region separator = makeSeparator();

    timeLabel = new Label("00:00");
    VBox timeBox = makeStatBox("Temps", timeLabel, accentColor);

    Region separator2 = makeSeparator();

    wallsLabel = new Label("10");
    VBox wallsBox = makeStatBox("Murs restants", wallsLabel, accentColor);

    Region separator3 = makeSeparator();

    scoreLabel = new Label("0");
    VBox scoreBox = makeStatBox("Score", scoreLabel, accentColor);

    VBox content =
        new VBox(16, nameLabel, separator, timeBox, separator2, wallsBox, separator3, scoreBox);
    content.setPadding(new Insets(15));
    content.setAlignment(Pos.TOP_CENTER);
    VBox.setVgrow(content, Priority.ALWAYS);

    setSpacing(0);
    setPadding(new Insets(0));
    setAlignment(Pos.TOP_CENTER);
    getChildren().addAll(topBar, content);

    setStyle(
        """
            -fx-border-color: #cccccc;
            -fx-border-width: 1;
            -fx-border-radius: 6;
            -fx-background-color: #f9f9f9;
            -fx-background-radius: 6;
            -fx-min-width: 180;
            -fx-max-width: 250;
            -fx-pref-width: 200;
        """);

    state.addListener(
        () -> {
          Player p = state.getPlayers().get(playerIndex);
          update(p);
        });
  }

  /**
   * Creates a vertical block with a title and a centered value.
   *
   * @param title The title of the stat (e.g., "Score").
   * @param valueLabel The label containing the stat value.
   * @param accentColor The main color to apply to the value label.
   * @return A {@link VBox} containing the title and value.
   */
  private VBox makeStatBox(String title, Label valueLabel, String accentColor) {
    Label titleLabel = new Label(title);
    titleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888888;");

    valueLabel.setStyle(
        "-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + accentColor + ";");

    VBox box = new VBox(4, titleLabel, valueLabel);
    box.setAlignment(Pos.CENTER);
    return box;
  }

  /**
   * Creates a thin horizontal separator to visually separate sections of the panel.
   *
   * @return A {@link Region} representing the separator.
   */
  private Region makeSeparator() {
    Region sep = new Region();
    sep.setPrefHeight(1);
    sep.setMaxWidth(Double.MAX_VALUE);
    sep.setStyle("-fx-background-color: #e0e0e0;");
    return sep;
  }

  /**
   * Updates the panel's information based on the current state of the player.
   *
   * @param player The player whose information should be displayed.
   */
  public void update(Player player) {
    wallsLabel.setText(String.valueOf(player.getRemainingWalls()));
    scoreLabel.setText(String.valueOf(player.getScore()));
    timeLabel.setText(formatTime((int) player.getRemainingTime()));
  }

  /**
   * Formats a time given in seconds into a "mm:ss" string.
   *
   * @param seconds The time in seconds.
   * @return A formatted string representing the time.
   */
  private String formatTime(int seconds) {
    return String.format("%02d:%02d", seconds / 60, seconds % 60);
  }
}
