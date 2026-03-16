package fr.bordeaux.gui;

import fr.bordeaux.core.*;
import fr.bordeaux.graph.Graph;
import java.util.List;
import javafx.animation.FadeTransition;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class BoardView extends GridPane {

    private final GameEngine engine;
    private final int cellSize = 100;
    private final int wallThickness = 10;

    private StackPane[][] cells;
    private Rectangle[][] pawns;

    public BoardView(GameEngine engine) {
        this.engine = engine;

        int size = engine.getState().getBoard().getSize();
        cells = new StackPane[size][size];
        pawns = new Rectangle[engine.getState().getPlayers().size()][size];

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

                cells[row][col] = cell;

                int r = row;
                int c = col;

                cell.setOnMouseClicked(
                        e -> {
                            if (e.getButton() == MouseButton.PRIMARY) {
                                handleCellClick(r, c);
                            } else {
                                handleWallClick(r, c, e.getButton());
                            }
                        });

                add(cell, col, row);
            }
        }

        drawWalls();
        drawPlayers();
        highlightCurrentPlayer();
    }

    public void drawPlayers() {

        List<Player> players = engine.getState().getPlayers();

        for (int i = 0; i < players.size(); i++) {

            Player player = players.get(i);
            Position pos = player.getPosition();

            Rectangle pawn = new Rectangle(cellSize * 0.6, cellSize * 0.6);

            pawn.setArcWidth(15);
            pawn.setArcHeight(15);

            if (i == 0) {
                pawn.setFill(Color.RED);
            } else {
                pawn.setFill(Color.BLACK);
            }

            pawns[i][0] = pawn;

            StackPane cell = cells[pos.getX()][pos.getY()];
            cell.getChildren().add(pawn);

            pawn.setOnMouseClicked(
                    e -> {
                        if (e.getButton() == MouseButton.PRIMARY) {
                            highlightAccessibleCells(player);
                        }
                    });
        }
    }

    public void drawWalls() {

        Board board = engine.getState().getBoard();
        Graph graph = board.getGraph();

        int size = board.getSize();

        for (int row = 0; row < size - 1; row++) {
            for (int col = 0; col < size - 1; col++) {

                int u1 = board.id(row, col);
                int u2 = board.id(row, col + 1);
                int v1 = board.id(row + 1, col);
                int v2 = board.id(row + 1, col + 1);

                // mur horizontal
                if (!graph.hasEdge(u1, v1) && !graph.hasEdge(u2, v2)) {

                    Rectangle wall = new Rectangle(cellSize , wallThickness);
                    wall.setFill(Color.BROWN);

                    add(wall, col, row);
                }

                // mur vertical
                if (!graph.hasEdge(u1, u2) && !graph.hasEdge(v1, v2)) {

                    Rectangle wall = new Rectangle(wallThickness, cellSize );
                    wall.setFill(Color.BROWN);

                    add(wall, col, row);
                }
            }
        }
    }

    public void highlightCurrentPlayer() {

        Player current = engine.getState().getCurrentPlayer();
        Position pos = current.getPosition();

        StackPane cell = cells[pos.getX()][pos.getY()];

        if (cell.getChildren().isEmpty()) return;

        Rectangle pawn = (Rectangle) cell.getChildren().get(0);

        FadeTransition ft = new FadeTransition(Duration.seconds(0.5), pawn);

        ft.setFromValue(1.0);
        ft.setToValue(0.2);

        ft.setCycleCount(FadeTransition.INDEFINITE);
        ft.setAutoReverse(true);

        ft.play();
    }

    public void highlightAccessibleCells(Player player) {

        for (StackPane[] row : cells)
            for (StackPane cell : row)
                cell.setStyle("-fx-border-color: black; -fx-background-color: beige;");

        List<Position> moves = engine.getState().getLegalMoves(player);

        for (Position pos : moves) {

            StackPane cell = cells[pos.getX()][pos.getY()];
            cell.setStyle("-fx-border-color: black; -fx-background-color: lightgreen;");
        }
    }

    public void handleCellClick(int row, int col) {

        Player current = engine.getState().getCurrentPlayer();

        Move move =
                Move.pawn(
                        current.getPosition(),
                        new Position(row, col));

        if (engine.play(move)) {
            drawBoard();
        }
    }

    public void handleWallClick(int row, int col, MouseButton button) {

        Orientation orientation;

        if (button == MouseButton.SECONDARY) {
            orientation = Orientation.HORIZONTAL;
        } else {
            orientation = Orientation.VERTICAL;
        }

        Move wallMove =
                Move.wall(
                        new Position(row, col),
                        orientation);

        if (RuleChecker.isMoveLegal(engine.getState(), wallMove)) {

            engine.play(wallMove);
            drawBoard();
        }
    }

    public Rectangle[][] getPawns() {
        return pawns;
    }
}