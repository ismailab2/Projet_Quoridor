package fr.bordeaux.gui;

import fr.bordeaux.core.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class BoardView extends GridPane {

  private final GameEngine engine;
  private final int cellSize = 100;
  private final int wallThickness = 10;

  public BoardView(GameEngine engine) {
    this.engine = engine;
    engine.getState().addListener(this::drawBoard);
    drawBoard();
  }

  public void drawBoard() {
    getChildren().clear();
    Board board = engine.getState().getBoard();
    int size = board.getSize();

    for (int row = 0; row < size; row++) {
      for (int col = 0; col < size; col++) {
        StackPane cell = new StackPane();
        cell.setPrefSize(cellSize, cellSize);
        cell.setStyle("-fx-border-color: black; -fx-background-color: beige;");

        int r = row;
        int c = col;
        cell.setOnMouseClicked(
            e -> {
              if (e.getButton() == MouseButton.PRIMARY) {
                handleCellClick(r, c);
              } else if (e.getButton() == MouseButton.SECONDARY) {
                handleWallClick(r, c);
              }
            });

        add(cell, col, row);
      }
    }

    drawWalls();
    drawPlayers();
  }

  private void drawPlayers() {
    for (Player player : engine.getState().getPlayers()) {
      Position pos = player.getPosition();
      Rectangle pawn = new Rectangle(cellSize * 0.6, cellSize * 0.6);
      pawn.setArcWidth(15);
      pawn.setArcHeight(15);
      pawn.setFill(player.getTypePlayer() == TypePlayer.HUMAN ? Color.BLUE : Color.RED);

      StackPane cell =
          (StackPane)
              getChildren().get(pos.getX() * engine.getState().getBoard().getSize() + pos.getY());
      cell.getChildren().clear();
      cell.getChildren().add(pawn);
    }
  }

  private void drawWalls() {
    Board board = engine.getState().getBoard();
    int size = board.getSize();

    // Vérifie toutes les positions possibles pour murs
    for (int row = 0; row < size - 1; row++) {
      for (int col = 0; col < size - 1; col++) {
        Position pos = new Position(row, col);

        // Horizontal
        if (board.isWallCollision(pos, Orientation.HORIZONTAL)) {
          Rectangle wall = new Rectangle(cellSize * 2, wallThickness, Color.BROWN);
          add(wall, col, row);
        }
        // Vertical
        if (board.isWallCollision(pos, Orientation.VERTICAL)) {
          Rectangle wall = new Rectangle(wallThickness, cellSize * 2, Color.BROWN);
          add(wall, col, row);
        }
      }
    }
  }

  private void handleCellClick(int row, int col) {
    Position pos = engine.getState().getCurrentPlayer().getPosition();

    Move move = Move.pawn(pos, new Position(row, col));

    if (engine.play(move)) {
      drawBoard();
    } else {
      System.out.println("Déplacement interdit !");
    }
  }

  private void handleWallClick(int row, int col) {
    // Exemple simple : clique secondaire ajoute mur horizontal
    Move wallMove = Move.wall(new Position(row, col), Orientation.HORIZONTAL);

    if (RuleChecker.isMoveLegal(engine.getState(), wallMove)) {
      engine.play(wallMove);
      drawBoard();
    } else {
      System.out.println("Mur impossible ici !");
    }
  }
}
