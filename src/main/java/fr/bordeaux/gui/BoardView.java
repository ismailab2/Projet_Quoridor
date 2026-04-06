package fr.bordeaux.gui;

import fr.bordeaux.core.*;
import fr.bordeaux.graph.Graph;
import java.util.List;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Graphical Quoridor board. Draws cells, pawns, walls and handles interactions. Board redraws
 * automatically on resize or game state change.
 */
public final class BoardView extends GridPane {

  /** Game engine used to retrieve the game state. */
  private final GameEngine engine;

  /** References to all cell containers on the board. */
  private final StackPane[][] cells;

  private static final double PAWN_SIZE_FACTOR = 0.6;
  private static final double WALL_ARC = 4.0;
  private static final double PAWN_ARC = 15.0;
  private static final double PAWN_STROKE_WIDTH = 5.0;
  private static final double SHADOW_RADIUS = 5.0;
  private static final double PULSE_DURATION = 0.5;
  private static final double PULSE_MIN_OPACITY = 0.2;
  private static final double PREVIEW_OPACITY = 0.4;
  private static final int DEFAULT_CELL_SIZE = 100;
  private static final int MIN_WALL_THICKNESS = 10;
  private static final int WALL_THICKNESS_RATIO = 10;

  private static final Color WALL_COLOR = Color.rgb(101, 67, 33);
  private static final Color PREVIEW_COLOR = Color.rgb(139, 69, 19, PREVIEW_OPACITY);

  /**
   * Creates board view.
   *
   * @param engine game engine
   */
  public BoardView(final GameEngine engine) {
    super();
    this.engine = engine;
    final GameState state = engine.getState();
    final int size = state.getBoard().getSize();
    cells = new StackPane[size][size];
    this.widthProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              drawBoard();
            });
    this.heightProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              drawBoard();
            });
    state.addListener(
        () -> {
          Platform.runLater(this::drawBoard);
        });
    drawBoard();
  }

  /** Redraws board. Clears children/constraints and rebuilds all components. */
  public void drawBoard() {
    getChildren().clear();
    getColumnConstraints().clear();
    getRowConstraints().clear();
    final GameState state = engine.getState();
    final Board board = state.getBoard();
    final int size = board.getSize();
    final int cellSize = getCellSize();
    for (int index = 0; index < size; index++) {
      getColumnConstraints().add(new ColumnConstraints(cellSize));
      getRowConstraints().add(new RowConstraints(cellSize));
    }
    for (int row = 0; row < size; row++) {
      for (int col = 0; col < size; col++) {
        final StackPane cell = new StackPane();
        cell.setPrefSize(cellSize, cellSize);
        cell.setStyle("-fx-border-color: black; -fx-background-color: beige;");
        cells[row][col] = cell;
        final int targetRow = row;
        final int targetCol = col;
        cell.setOnMouseClicked(
            e -> {
              if (e.getButton() == MouseButton.PRIMARY) {
                handleCellClick(targetRow, targetCol);
              } else {
                handleWallClick(targetRow, targetCol, e.getButton());
              }
            });
        add(cell, col, row);
      }
    }
    drawWallSlots();
    drawWalls();
    drawPlayers();
    highlightCurrentPlayer();
  }

  /** Draws placed walls by checking missing graph edges. Rendered as brown rectangles. */
  public void drawWalls() {
    final GameState state = engine.getState();
    final Board board = state.getBoard();
    final Graph graph = board.getGraph();
    final int cellSize = getCellSize();
    final int wallThickness = getWallThickness();
    final int size = board.getSize();
    for (int row = 0; row < size - 1; row++) {
      for (int col = 0; col < size - 1; col++) {
        final int cellU1 = board.getCellId(row, col);
        final int cellU2 = board.getCellId(row, col + 1);
        final int cellV1 = board.getCellId(row + 1, col);
        final int cellV2 = board.getCellId(row + 1, col + 1);

        if (!graph.hasEdge(cellU1, cellV1) && !graph.hasEdge(cellU2, cellV2)) {
          final Rectangle wall = new Rectangle(cellSize * 2, wallThickness);
          wall.setFill(WALL_COLOR);
          wall.setArcWidth(WALL_ARC);
          wall.setArcHeight(WALL_ARC);
          wall.setMouseTransparent(true);
          add(wall, col, row, 2, 1);
          GridPane.setValignment(wall, VPos.BOTTOM);
          wall.setTranslateY(wallThickness / 2.0);
        }
        if (!graph.hasEdge(cellU1, cellU2) && !graph.hasEdge(cellV1, cellV2)) {
          final Rectangle wall = new Rectangle(wallThickness, cellSize * 2);
          wall.setFill(WALL_COLOR);
          wall.setArcWidth(WALL_ARC);
          wall.setArcHeight(WALL_ARC);
          wall.setMouseTransparent(true);
          add(wall, col, row, 1, 2);
          GridPane.setHalignment(wall, HPos.RIGHT);
          wall.setTranslateX(wallThickness / 2.0);
        }
      }
    }
  }

  /** Draws player pawns. Pawns listen for left click to show highlights. */
  public void drawPlayers() {
    final int cellSize = getCellSize();
    final GameState state = engine.getState();
    final List<Player> players = state.getPlayers();
    for (final Player player : players) {
      final Position pos = player.getPosition();
      final Rectangle pawn =
          new Rectangle(cellSize * PAWN_SIZE_FACTOR, cellSize * PAWN_SIZE_FACTOR);
      pawn.setArcWidth(PAWN_ARC);
      pawn.setArcHeight(PAWN_ARC);
      pawn.setStroke(Color.LIGHTGRAY);
      pawn.setStrokeWidth(PAWN_STROKE_WIDTH);
      pawn.setEffect(
          new javafx.scene.effect.DropShadow(SHADOW_RADIUS, javafx.scene.paint.Color.GRAY));

      final fr.bordeaux.core.Color playerColor = player.getColor();
      final Color fxColor =
          switch (playerColor) {
            case WHITE -> Color.WHITE;
            case BLACK -> Color.BLACK;
            case BLUE -> Color.BLUE;
            case RED -> Color.RED;
          };
      pawn.setFill(fxColor);
      final int posX = pos.getX();
      final int posY = pos.getY();
      cells[posX][posY].getChildren().add(pawn);
      pawn.setOnMouseClicked(
          event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
              highlightAccessibleCells(player);
            }
          });
    }
  }

  /** Poulse animation for the current player's pawn. */
  public void highlightCurrentPlayer() {
    final GameState state = engine.getState();
    if (state.isGameOver()) {
      return;
    }
    final Player current = state.getCurrentPlayer();
    final Position pos = current.getPosition();
    final int posX = pos.getX();
    final int posY = pos.getY();
    final StackPane cell = cells[posX][posY];
    final Rectangle pawn =
        cell.getChildren().stream()
            .filter(n -> n instanceof Rectangle)
            .map(n -> (Rectangle) n)
            .findFirst()
            .orElse(null);
    if (pawn == null) {
      return;
    }
    final FadeTransition fadeTransition =
        new FadeTransition(Duration.seconds(PULSE_DURATION), pawn);
    fadeTransition.setFromValue(1.0);
    fadeTransition.setToValue(PULSE_MIN_OPACITY);
    fadeTransition.setCycleCount(FadeTransition.INDEFINITE);
    fadeTransition.setAutoReverse(true);
    fadeTransition.play();
  }

  /**
   * Highlights cells reachable by player.
   *
   * @param player player to check
   */
  public void highlightAccessibleCells(final Player player) {
    final GameState state = engine.getState();
    final Player currentPlayer = state.getCurrentPlayer();
    if (currentPlayer != player || player.isAI()) {
      return;
    }
    for (final StackPane[] row : cells) {
      for (final StackPane cell : row) {
        cell.setStyle("-fx-border-color: black; -fx-background-color: beige;");
      }
    }
    for (final Position movePos : state.getLegalMoves(player)) {
      final int posX = movePos.getX();
      final int posY = movePos.getY();
      cells[posX][posY].setStyle("-fx-border-color: black; -fx-background-color: lightgreen;");
    }
  }

  /**
   * Moves pawn on left click.
   *
   * @param row row index
   * @param col column index
   */
  public void handleCellClick(final int row, final int col) {
    final GameState state = engine.getState();
    final Player current = state.getCurrentPlayer();
    if (current.isAI()) {
      return;
    }
    final Position from = current.getPosition();
    final Position to = new Position(row, col);
    final Move move = Move.pawn(from, to);
    if (engine.play(move)) {
      drawBoard();
    }
  }

  /**
   * Places wall on right/other click.
   *
   * @param row row index
   * @param col column index
   * @param button clicked button
   */
  public void handleWallClick(final int row, final int col, final MouseButton button) {
    final GameState state = engine.getState();
    if (state.getCurrentPlayer().isAI()) {
      return;
    }
    final Orientation orientation =
        (button == MouseButton.SECONDARY) ? Orientation.HORIZONTAL : Orientation.VERTICAL;
    final Move wallMove = Move.wall(new Position(row, col), orientation);
    if (RuleChecker.isMoveLegal(state, wallMove)) {
      engine.play(wallMove);
      drawBoard();
    }
  }

  /** Creates interactive wall slots between cells. */
  private void drawWallSlots() {
    final int cellSize = getCellSize();
    final int wallThickness = getWallThickness();
    final int size = engine.getState().getBoard().getSize();
    for (int row = 0; row < size - 1; row++) {
      for (int col = 0; col < size - 1; col++) {
        final Rectangle hSlot = new Rectangle(cellSize * 2, wallThickness);
        hSlot.setFill(Color.TRANSPARENT);
        setupWallInteractions(hSlot, row, col, Orientation.HORIZONTAL);
        add(hSlot, col, row, 2, 1);
        GridPane.setValignment(hSlot, VPos.BOTTOM);
        hSlot.setTranslateY(wallThickness / 2.0);
        final Rectangle vSlot = new Rectangle(wallThickness, cellSize * 2);
        vSlot.setFill(Color.TRANSPARENT);
        setupWallInteractions(vSlot, row, col, Orientation.VERTICAL);
        add(vSlot, col, row, 1, 2);
        GridPane.setHalignment(vSlot, HPos.RIGHT);
        vSlot.setTranslateX(wallThickness / 2.0);
      }
    }
  }

  /**
   * Sets wall slot handlers (hover, click).
   *
   * @param slot interactive slot
   * @param row row index
   * @param col column index
   * @param orientation wall orientation
   */
  private void setupWallInteractions(
      final Rectangle slot, final int row, final int col, final Orientation orientation) {
    slot.setOnMouseEntered(
        event -> {
          final GameState state = engine.getState();
          final Player current = state.getCurrentPlayer();
          if (!current.isAI()) {
            slot.setFill(PREVIEW_COLOR);
          }
        });
    slot.setOnMouseExited(
        event -> {
          slot.setFill(Color.TRANSPARENT);
        });
    slot.setOnMouseClicked(
        event -> {
          final GameState state = engine.getState();
          final Player current = state.getCurrentPlayer();
          if (current.isAI()) {
            return;
          }
          if (event.getButton() == MouseButton.SECONDARY) {
            final Move wallMove = Move.wall(new Position(row, col), orientation);
            if (engine.play(wallMove)) {
              drawBoard();
            }
          }
        });
  }

  /**
   * @return cell size in pixels.
   */
  private int getCellSize() {
    final GameState state = engine.getState();
    final Board board = state.getBoard();
    final int size = board.getSize();
    final double available = Math.min(getWidth(), getHeight());
    if (available <= 0.0) {
      return DEFAULT_CELL_SIZE;
    }
    return (int) (available / size);
  }

  /**
   * @return wall thickness in pixels.
   */
  private int getWallThickness() {
    return Math.max(MIN_WALL_THICKNESS, getCellSize() / WALL_THICKNESS_RATIO);
  }
}
