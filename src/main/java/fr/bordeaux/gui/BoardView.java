package fr.bordeaux.gui;

import fr.bordeaux.core.*;
import fr.bordeaux.graph.Graph;
import java.util.List;
import javafx.animation.FadeTransition;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Graphical representation of the Quoridor board. It is responsible for drawing cells, pawns, walls
 * and handling board interactions.
 */
public class BoardView extends GridPane {

  private final GameEngine engine;
  private final int cellSize = 100;
  private final int wallThickness = 10;

  private StackPane[][] cells;
  private Rectangle[][] pawns;

  /**
   * Creates a board view linked to the given game engine.
   *
   * @param engine game engine used to read and update the game state
   */
  public BoardView(GameEngine engine) {
    this.engine = engine;

    int size = engine.getState().getBoard().getSize();

    // Force rows and cols to never change their size.
    for (int i = 0; i < size; i++) {
      ColumnConstraints col = new ColumnConstraints(cellSize);
      this.getColumnConstraints().add(col);

      RowConstraints row = new RowConstraints(cellSize);
      this.getRowConstraints().add(row);
    }
    cells = new StackPane[size][size];
    pawns = new Rectangle[engine.getState().getPlayers().size()][size];

    engine.getState().addListener(this::drawBoard);
    drawBoard();
  }

  /** Redraws the complete board, including cells, players and walls. */
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
    // drawWallSlots();
    drawWalls();
    drawPlayers();
    highlightCurrentPlayer();
  }

  /** Draws all players on the board. */
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

  /** Draws all walls currently placed on the board. */
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

        // horizontal wall (between row and row+1)
        if (!graph.hasEdge(u1, v1) && !graph.hasEdge(u2, v2)) {
          Rectangle wall = new Rectangle(cellSize * 2, wallThickness);
          wall.setFill(Color.BROWN);
          wall.setMouseTransparent(true);
          add(wall, col, row, 2, 1);
          GridPane.setValignment(wall, VPos.BOTTOM);
          wall.setTranslateY(wallThickness / 2.0);
        }

        // vertical wall (between col and col+1)
        if (!graph.hasEdge(u1, u2) && !graph.hasEdge(v1, v2)) {
          Rectangle wall = new Rectangle(wallThickness, cellSize * 2);
          wall.setFill(Color.BROWN);
          wall.setMouseTransparent(true);
          add(wall, col, row, 1, 2);
          GridPane.setHalignment(wall, HPos.RIGHT);
          wall.setTranslateX(wallThickness / 2.0);
        }
      }
    }
  }

  /** Highlights the pawn of the current player. */
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

  /**
   * Highlights the cells accessible to the given player.
   *
   * @param player player whose available moves are highlighted
   */
  public void highlightAccessibleCells(Player player) {
    if (this.engine.getState().getCurrentPlayer() != player) return;

    for (StackPane[] row : cells)
      for (StackPane cell : row)
        cell.setStyle("-fx-border-color: black; -fx-background-color: beige;");
    List<Position> moves = engine.getState().getLegalMoves(player);

    for (Position pos : moves) {

      StackPane cell = cells[pos.getX()][pos.getY()];
      cell.setStyle("-fx-border-color: black; -fx-background-color: lightgreen;");
    }
  }

  /**
   * Handles a click on a board cell.
   *
   * @param row clicked row
   * @param col clicked column
   */
  public void handleCellClick(int row, int col) {

    Player current = engine.getState().getCurrentPlayer();

    Move move = Move.pawn(current.getPosition(), new Position(row, col));

    if (engine.play(move)) {
      drawBoard();
    }
  }

  /**
   * Handles a click on a wall placement area.
   *
   * @param row clicked row
   * @param col clicked column
   * @param button mouse button used for the click
   */
  public void handleWallClick(int row, int col, MouseButton button) {

    Orientation orientation;

    if (button == MouseButton.SECONDARY) {
      orientation = Orientation.HORIZONTAL;
    } else {
      orientation = Orientation.VERTICAL;
    }

    Move wallMove = Move.wall(new Position(row, col), orientation);

    if (RuleChecker.isMoveLegal(engine.getState(), wallMove)) {

      engine.play(wallMove);
      drawBoard();
    }
  }

  // Le code en dessous permet de mettre en surbrillance les bords des cases quand on passe la
  // souris dessus, et on a juste à faire
  // clic droit pour placer un mur, tu peux le garder tel quel ou changer, c'est mieux que d'avoir
  // des touches différentes pour placer
  // les murs.
  /**
   * Creates and places invisible interactive rectangles (slots) on the board's borders.
   * These slots detect mouse hovering to show a preview and handle right-clicks
   * to place walls.
   *
  private void drawWallSlots() {
    Board board = engine.getState().getBoard();
    int size = board.getSize();

    // Iterate up to size-1 because a wall occupies the space between two cells
    for (int row = 0; row < size - 1; row++) {
      for (int col = 0; col < size - 1; col++) {

        // --- HORIZONTAL SLOT ---
        // Width: 2 cells, Height: wall thickness
        Rectangle hSlot = new Rectangle(cellSize * 2, wallThickness);
        hSlot.setFill(Color.TRANSPARENT); // Invisible until hovered

        setupWallInteractions(hSlot, row, col, Orientation.HORIZONTAL);

        // Span across 2 columns and 1 row
        add(hSlot, col, row, 2, 1);
        GridPane.setValignment(hSlot, VPos.BOTTOM);
        // Vertical offset to center it exactly on the grid line
        hSlot.setTranslateY(wallThickness / 2.0);

        // --- VERTICAL SLOT ---
        // Width: wall thickness, Height: 2 cells
        Rectangle vSlot = new Rectangle(wallThickness, cellSize * 2);
        vSlot.setFill(Color.TRANSPARENT);

        setupWallInteractions(vSlot, row, col, Orientation.VERTICAL);

        // Span across 1 column and 2 rows
        add(vSlot, col, row, 1, 2);
        GridPane.setHalignment(vSlot, HPos.RIGHT);
        // Horizontal offset to center it exactly on the grid line
        vSlot.setTranslateX(wallThickness / 2.0);
      }
    }
  }*/

  /**
   * Sets up mouse event handlers for a wall slot, including hover effects and click actions.
   *
   * @param slot The rectangle representing the interactive area
   * @param row The starting row position for the potential wall
   * @param col The starting column position for the potential wall
   * @param orientation The orientation (HORIZONTAL/VERTICAL) of the wall
   *     <p>private void setupWallInteractions(Rectangle slot, int row, int col, Orientation
   *     orientation) {
   *     <p>// Hover effect: show a semi-transparent brown preview (0.4 opacity)
   *     slot.setOnMouseEntered(e -> { slot.setFill(Color.rgb(139, 69, 19, 0.4)); });
   *     <p>// Hide the preview when the mouse leaves the area slot.setOnMouseExited(e -> {
   *     slot.setFill(Color.TRANSPARENT); });
   *     <p>// Handle click action slot.setOnMouseClicked(e -> { // Use right-click (SECONDARY) for
   *     wall placement if (e.getButton() == MouseButton.SECONDARY) { Move wallMove = Move.wall(new
   *     Position(row, col), orientation);
   *     <p>// Check move legality through the engine before applying if (engine.play(wallMove)) {
   *     drawBoard(); // Full redraw of the UI to reflect changes } } }); }
   */
  /**
   * Returns the array containing the pawn shapes displayed on the board.
   *
   * @return two-dimensional array of pawn rectangles
   */
  public Rectangle[][] getPawns() {
    return pawns;
  }
}
