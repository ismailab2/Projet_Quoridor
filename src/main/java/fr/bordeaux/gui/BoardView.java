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
 * Graphical representation of the Quoridor board. Responsible for drawing cells, pawns, walls and
 * handling board interactions. Extends {@link GridPane} to organize cells in a grid layout. The
 * board redraws itself automatically when the window is resized or the game state changes.
 */
public class BoardView extends GridPane {

  private final GameEngine engine;
  private StackPane[][] cells;
  private Rectangle[][] pawns;

  /**
   * Creates a board view linked to the given game engine. Registers listeners on width, height and
   * game state to trigger automatic redraws.
   *
   * @param engine the game engine used to read and update the game state
   */
  public BoardView(GameEngine engine) {
    this.engine = engine;
    int size = engine.getState().getBoard().getSize();
    cells = new StackPane[size][size];
    pawns = new Rectangle[engine.getState().getPlayers().size()][size];
    this.widthProperty().addListener((obs, oldVal, newVal) -> drawBoard());
    this.heightProperty().addListener((obs, oldVal, newVal) -> drawBoard());
    engine.getState().addListener(this::drawBoard);
    drawBoard();
  }

  /**
   * Redraws the complete board from scratch. Clears all children and constraints, then rebuilds
   * cells, wall slots, placed walls, player pawns and the current player highlight in order.
   */
  public void drawBoard() {
    getChildren().clear();
    getColumnConstraints().clear();
    getRowConstraints().clear();
    Board board = engine.getState().getBoard();
    int size = board.getSize();
    int cellSize = getCellSize();
    for (int i = 0; i < size; i++) {
      getColumnConstraints().add(new ColumnConstraints(cellSize));
      getRowConstraints().add(new RowConstraints(cellSize));
    }
    for (int row = 0; row < size; row++) {
      for (int col = 0; col < size; col++) {
        StackPane cell = new StackPane();
        cell.setPrefSize(cellSize, cellSize);
        cell.setStyle("-fx-border-color: black; -fx-background-color: beige;");
        cells[row][col] = cell;
        int r = row, c = col;
        cell.setOnMouseClicked(
            e -> {
              if (e.getButton() == MouseButton.PRIMARY) handleCellClick(r, c);
              else handleWallClick(r, c, e.getButton());
            });
        add(cell, col, row);
      }
    }
    drawWallSlots();
    drawWalls();
    drawPlayers();
    highlightCurrentPlayer();
  }

  /**
   * Draws all walls currently placed on the board by inspecting missing edges in the graph. A
   * horizontal wall is detected when edges u1-v1 and u2-v2 are both absent. A vertical wall is
   * detected when edges u1-u2 and v1-v2 are both absent. Walls are rendered as brown rounded
   * rectangles and are mouse-transparent.
   */
  public void drawWalls() {
    Board board = engine.getState().getBoard();
    Graph graph = board.getGraph();
    int cellSize = getCellSize();
    int wallThickness = getWallThickness();
    int size = board.getSize();
    for (int row = 0; row < size - 1; row++) {
      for (int col = 0; col < size - 1; col++) {
        int u1 = board.id(row, col);
        int u2 = board.id(row, col + 1);
        int v1 = board.id(row + 1, col);
        int v2 = board.id(row + 1, col + 1);

        if (!graph.hasEdge(u1, v1) && !graph.hasEdge(u2, v2)) {
          Rectangle wall = new Rectangle(cellSize * 2, wallThickness);
          wall.setFill(Color.rgb(101, 67, 33));
          wall.setArcWidth(4);
          wall.setArcHeight(4);
          wall.setMouseTransparent(true);
          add(wall, col, row, 2, 1);
          GridPane.setValignment(wall, VPos.BOTTOM);
          wall.setTranslateY(wallThickness / 2.0);
        }
        if (!graph.hasEdge(u1, u2) && !graph.hasEdge(v1, v2)) {
          Rectangle wall = new Rectangle(wallThickness, cellSize * 2);
          wall.setFill(Color.rgb(101, 67, 33));
          wall.setArcWidth(4);
          wall.setArcHeight(4);
          wall.setMouseTransparent(true);
          add(wall, col, row, 1, 2);
          GridPane.setHalignment(wall, HPos.RIGHT);
          wall.setTranslateX(wallThickness / 2.0);
        }
      }
    }
  }

  /**
   * Draws all player pawns at their current positions on the board. Player 0 is drawn in red, all
   * other players in black. Each pawn listens for a left click to highlight accessible cells.
   */
  public void drawPlayers() {
    int cellSize = getCellSize();
    List<Player> players = engine.getState().getPlayers();
    for (int i = 0; i < players.size(); i++) {
      Player player = players.get(i);
      Position pos = player.getPosition();
      Rectangle pawn = new Rectangle(cellSize * 0.6, cellSize * 0.6);
      pawn.setArcWidth(15);
      pawn.setArcHeight(15);
      pawn.setFill(i == 0 ? Color.RED : Color.BLACK);
      pawns[i][0] = pawn;
      cells[pos.getX()][pos.getY()].getChildren().add(pawn);
      pawn.setOnMouseClicked(
          e -> {
            if (e.getButton() == MouseButton.PRIMARY) highlightAccessibleCells(player);
          });
    }
  }

  /**
   * Applies a pulsing {@link FadeTransition} animation to the pawn of the current player to
   * visually indicate whose turn it is.
   */
  public void highlightCurrentPlayer() {
    if (engine.getState().isGameOver()) return;
    Player current = engine.getState().getCurrentPlayer();
    Position pos = current.getPosition();
    StackPane cell = cells[pos.getX()][pos.getY()];
    Rectangle pawn =
        cell.getChildren().stream()
            .filter(n -> n instanceof Rectangle)
            .map(n -> (Rectangle) n)
            .findFirst()
            .orElse(null);
    if (pawn == null) return;
    FadeTransition ft = new FadeTransition(Duration.seconds(0.5), pawn);
    ft.setFromValue(1.0);
    ft.setToValue(0.2);
    ft.setCycleCount(FadeTransition.INDEFINITE);
    ft.setAutoReverse(true);
    ft.play();
  }

  /**
   * Highlights in green all cells reachable by the given player on their current turn. Does nothing
   * if the given player is not the current player. Resets all cells to their default beige color
   * before applying highlights.
   *
   * @param player the player whose legal moves should be highlighted
   */
  public void highlightAccessibleCells(Player player) {
    if (engine.getState().getCurrentPlayer() != player || player.isAI()) return;
    for (StackPane[] row : cells)
      for (StackPane cell : row)
        cell.setStyle("-fx-border-color: black; -fx-background-color: beige;");
    for (Position pos : engine.getState().getLegalMoves(player))
      cells[pos.getX()][pos.getY()].setStyle(
          "-fx-border-color: black; -fx-background-color: lightgreen;");
  }

  /**
   * Handles a left click on a board cell by attempting to move the current player's pawn to the
   * clicked position. Redraws the board if the move is accepted by the engine.
   *
   * @param row the row index of the clicked cell
   * @param col the column index of the clicked cell
   */
  public void handleCellClick(int row, int col) {
    Player current = engine.getState().getCurrentPlayer();
    if (current.isAI()) return;
    Move move = Move.pawn(current.getPosition(), new Position(row, col));
    if (engine.play(move)) drawBoard();
  }

  /**
   * Handles a non-primary click on a board cell by attempting to place a wall. A right click places
   * a horizontal wall; any other button places a vertical wall. The wall is only placed if it
   * passes the rule checker validation.
   *
   * @param row the row index of the clicked cell
   * @param col the column index of the clicked cell
   * @param button the mouse button that was clicked
   */
  public void handleWallClick(int row, int col, MouseButton button) {
    if (engine.getState().getCurrentPlayer().isAI()) return;
    Orientation orientation =
        (button == MouseButton.SECONDARY) ? Orientation.HORIZONTAL : Orientation.VERTICAL;
    Move wallMove = Move.wall(new Position(row, col), orientation);
    if (RuleChecker.isMoveLegal(engine.getState(), wallMove)) {
      engine.play(wallMove);
      drawBoard();
    }
  }

  /**
   * Creates and places invisible interactive rectangles (slots) between cells. Each slot detects
   * mouse hover to show a wall preview and right clicks to place walls. Slots are placed on all
   * inner borders of the board (size - 1 rows and columns).
   */
  private void drawWallSlots() {
    int cellSize = getCellSize();
    int wallThickness = getWallThickness();
    int size = engine.getState().getBoard().getSize();
    for (int row = 0; row < size - 1; row++) {
      for (int col = 0; col < size - 1; col++) {
        Rectangle hSlot = new Rectangle(cellSize * 2, wallThickness);
        hSlot.setFill(Color.TRANSPARENT);
        setupWallInteractions(hSlot, row, col, Orientation.HORIZONTAL);
        add(hSlot, col, row, 2, 1);
        GridPane.setValignment(hSlot, VPos.BOTTOM);
        hSlot.setTranslateY(wallThickness / 2.0);
        Rectangle vSlot = new Rectangle(wallThickness, cellSize * 2);
        vSlot.setFill(Color.TRANSPARENT);
        setupWallInteractions(vSlot, row, col, Orientation.VERTICAL);
        add(vSlot, col, row, 1, 2);
        GridPane.setHalignment(vSlot, HPos.RIGHT);
        vSlot.setTranslateX(wallThickness / 2.0);
      }
    }
  }

  /**
   * Attaches mouse event handlers to a wall slot rectangle. On hover, the slot becomes
   * semi-transparent brown as a placement preview. On right click, a wall move is submitted to the
   * engine and the board is redrawn if accepted.
   *
   * @param slot the rectangle acting as the interactive wall slot
   * @param row the row index of the top-left cell of the potential wall
   * @param col the column index of the top-left cell of the potential wall
   * @param orientation the orientation of the wall (HORIZONTAL or VERTICAL)
   */
  private void setupWallInteractions(Rectangle slot, int row, int col, Orientation orientation) {
    slot.setOnMouseEntered(
        e -> {
          if (!engine.getState().getCurrentPlayer().isAI()) {
            slot.setFill(Color.rgb(139, 69, 19, 0.4));
          }
        });
    slot.setOnMouseExited(e -> slot.setFill(Color.TRANSPARENT));
    slot.setOnMouseClicked(
        e -> {
          if (engine.getState().getCurrentPlayer().isAI()) return;
          if (e.getButton() == MouseButton.SECONDARY) {
            Move wallMove = Move.wall(new Position(row, col), orientation);
            if (engine.play(wallMove)) drawBoard();
          }
        });
  }

  /**
   * Calculates the pixel size of a single cell based on the current component dimensions. Uses the
   * smaller of width and height to keep the board square. Returns 100 as a default value before the
   * first render when dimensions are not yet known.
   *
   * @return the cell size in pixels
   */
  private int getCellSize() {
    int size = engine.getState().getBoard().getSize();
    double available = Math.min(getWidth(), getHeight());
    if (available <= 0) return 100;
    return (int) (available / size);
  }

  /**
   * Calculates the wall thickness in pixels proportional to the cell size. Enforces a minimum
   * thickness of 4 pixels to remain visible at small sizes.
   *
   * @return the wall thickness in pixels
   */
  private int getWallThickness() {
    return Math.max(10, getCellSize() / 10);
  }
}
