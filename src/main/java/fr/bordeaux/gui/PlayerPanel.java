package fr.bordeaux.gui;

import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Player;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;


public class PlayerPanel extends VBox {

    private final Label nameLabel;
    private  Label playerScoreLabel;
    private  Label wallsLabel;
    private  Label timeLabel;
    private  int playerIndex;

    public PlayerPanel(String playerName, GameState state, int playerIndex) {

        this.nameLabel = new Label(playerName);
        this.playerScoreLabel = new Label(" Score: 0");
        this.wallsLabel = new Label("Murs restants: 0");
        this.timeLabel = new Label("Temps: 0");
        this.playerIndex = playerIndex;

        setSpacing(15);
        setPadding(new Insets(15));

        getChildren().addAll(nameLabel, wallsLabel, timeLabel);

        setStyle("""
            -fx-border-color: black;
            -fx-background-color: #eeeeee;
            -fx-min-width: 150;
        """);

        state.addListener(() -> {
            Player p = state.getPlayers().get(playerIndex);
            update(p);
        });
    }

    /**
     * Update UI from Player object
     */
    public void update(Player player) {
        wallsLabel.setText("Murs restants: " + player.getRemainingWalls());
    }

}