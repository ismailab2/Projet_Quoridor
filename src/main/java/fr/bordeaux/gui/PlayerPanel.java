package fr.bordeaux.gui;

import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Player;
import fr.bordeaux.util.TimeConvertor;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

/**
 * Display panel for a player's stats (name, time, walls, score). Updates automatically when its
 * {@link GameState} changes.
 */
public final class PlayerPanel extends VBox {
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
   * Constructs panel for player.
   *
   * @param playerName player name
   * @param state game state
   * @param playerIndex player index
   */
  public PlayerPanel(final String playerName, final GameState state, final int playerIndex) {
    super();
    this.playerIndex = playerIndex;

    final String accentColor = (playerIndex == 0) ? "#e74c3c" : "#2c3e50";

    final Region topBar = new Region();
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

    final Region separator = makeSeparator();

    timeLabel = new Label("00:00");
    final VBox timeBox = makeStatBox("Temps", timeLabel, accentColor);

    final Region separator2 = makeSeparator();

    wallsLabel = new Label("10");
    final VBox wallsBox = makeStatBox("Murs restants", wallsLabel, accentColor);

    final Region separator3 = makeSeparator();

    scoreLabel = new Label("0");
    final VBox scoreBox = makeStatBox("Score", scoreLabel, accentColor);

    final VBox content =
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
        () ->
            Platform.runLater(
                () -> {
                  final Player p = state.getPlayers().get(playerIndex);
                  update(p);
                }));
    state.addTimerListener(
        () ->
            Platform.runLater(
                () -> {
                  final Player p = state.getPlayers().get(playerIndex);
                  timeLabel.setText(TimeConvertor.formatTime(p.getRemainingTime()));
                }));
  }

  /**
   * Creates a vertical block with a title and a centered value.
   *
   * @param title The title of the stat (e.g., "Score").
   * @param valueLabel The label containing the stat value.
   * @param accentColor The main color to apply to the value label.
   * @return A {@link VBox} containing the title and value.
   */
  private VBox makeStatBox(final String title, final Label valueLabel, final String accentColor) {
    final Label titleLabel = new Label(title);
    titleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888888;");

    valueLabel.setStyle(
        "-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + accentColor + ";");

    final VBox box = new VBox(4, titleLabel, valueLabel);
    box.setAlignment(Pos.CENTER);
    return box;
  }

  /**
   * Creates a thin horizontal separator to visually separate sections of the panel.
   *
   * @return A {@link Region} representing the separator.
   */
  private Region makeSeparator() {
    final Region sep = new Region();
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
  public void update(final Player player) {
    if (player.isDisabled()) {
      this.setOpacity(0.4);
      timeLabel.setText("ÉLIMINÉ");
      timeLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 20px;");
    }
    else{
      wallsLabel.setText(String.valueOf(player.getRemainingWalls()));
      scoreLabel.setText(String.valueOf(player.getScore()));
    }

  }
}
