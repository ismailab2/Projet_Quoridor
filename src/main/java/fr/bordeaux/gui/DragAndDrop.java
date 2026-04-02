package fr.bordeaux.gui;

import fr.bordeaux.core.GameEngine;
import fr.bordeaux.core.Move;
import fr.bordeaux.core.Position;
import fr.bordeaux.core.RuleChecker;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

/** Utility class that handles pawn drag and drop on the game board. */
public final class DragAndDrop {

  /** Data format used to store the dragged pawn position. */
  private static final DataFormat PAWN_FORMAT = new DataFormat("quoridor/pawn");

  /** Base CSS style shared by board cells during drag and drop. */
  private static final String STYLE_BASE = "-fx-border-color: black; -fx-background-color: ";

  /** Default background color of a board cell. */
  private static final String COLOR_NORMAL = "beige;";

  /** Background color used for a legal target cell. */
  private static final String COLOR_LEGAL = "lightgreen;";

  /** Background color used for an illegal target cell. */
  private static final String COLOR_ILLEGAL = "lightgray;";

  /** Utility class constructor. */
  private DragAndDrop() {
    // Prevent instantiation.
  }

  /**
   * Installs drag and drop handlers and reattaches them after each move.
   *
   * @param engine game engine used to validate and play moves
   * @param boardView board view on which handlers are installed
   */
  public static void install(final GameEngine engine, final BoardView boardView) {
    Runnable attachHandlers = () -> attachHandlers(engine, boardView);
    Platform.runLater(attachHandlers);
    engine.getState().addListener(() -> Platform.runLater(attachHandlers));
  }

  /**
   * Attaches drag and drop handlers to all board cells and pawns.
   *
   * @param engine game engine used to validate moves
   * @param boardView board view containing the cells
   */
  private static void attachHandlers(final GameEngine engine, final BoardView boardView) {
    for (Node node : boardView.getChildren()) {
      if (!(node instanceof StackPane cell)) {
        continue;
      }
      int row = getIndex(cell, true);
      int col = getIndex(cell, false);
      installDropOnCell(engine, boardView, cell, row, col);

      for (Node child : cell.getChildren()) {
        if (child instanceof Rectangle pawn) {
          installDragOnPawn(engine, pawn, row, col);
        }
      }
    }
  }

  /**
   * Installs drag detection on a pawn.
   *
   * @param engine game engine used to read the current player
   * @param pawn pawn node that can be dragged
   * @param row row of the pawn
   * @param col column of the pawn
   */
  private static void installDragOnPawn(
      final GameEngine engine, final Rectangle pawn, final int row, final int col) {
    pawn.setOnDragDetected(
        event -> {
          Position currentPosition = engine.getState().getCurrentPlayer().getPosition();

          if (currentPosition.getX() != row || currentPosition.getY() != col) {
            event.consume();
            return;
          }

          Dragboard dragboard = pawn.startDragAndDrop(TransferMode.MOVE);
          ClipboardContent content = new ClipboardContent();
          content.put(PAWN_FORMAT, row + "," + col);
          dragboard.setContent(content);
          event.consume();
        });
  }

  /**
   * Installs drop handlers on a target cell.
   *
   * @param engine game engine used to validate and play moves
   * @param boardView board view to redraw after a successful move
   * @param cell target cell
   * @param toRow target row
   * @param toCol target column
   */
  private static void installDropOnCell(
      final GameEngine engine,
      final BoardView boardView,
      final StackPane cell,
      final int toRow,
      final int toCol) {
    cell.setOnDragEntered(
        event -> {
          if (event.getDragboard().hasContent(PAWN_FORMAT)) {
            boolean isLegal = isMoveLegal(engine, event.getDragboard(), toRow, toCol);
            cell.setStyle(STYLE_BASE + (isLegal ? COLOR_LEGAL : COLOR_ILLEGAL));
          }
          event.consume();
        });

    cell.setOnDragExited(
        event -> {
          cell.setStyle(STYLE_BASE + COLOR_NORMAL);
          event.consume();
        });

    cell.setOnDragOver(
        event -> {
          if (event.getDragboard().hasContent(PAWN_FORMAT)
              && isMoveLegal(engine, event.getDragboard(), toRow, toCol)) {
            event.acceptTransferModes(TransferMode.MOVE);
          }
          event.consume();
        });

    cell.setOnDragDropped(
        event -> {
          boolean success = false;
          if (event.getDragboard().hasContent(PAWN_FORMAT)
              && isMoveLegal(engine, event.getDragboard(), toRow, toCol)) {
            success = engine.play(buildMove(event.getDragboard(), toRow, toCol));
            if (success) {
              boardView.drawBoard();
            }
          }
          event.setDropCompleted(success);
          event.consume();
        });

    cell.setOnDragDone(
        event -> {
          cell.setStyle(STYLE_BASE + COLOR_NORMAL);
          event.consume();
        });
  }

  /**
   * Checks whether a move to the target cell is legal.
   *
   * @param engine game engine used to access the game state
   * @param dragboard dragboard containing the origin position
   * @param toRow target row
   * @param toCol target column
   * @return true if the move is legal, false otherwise
   */
  private static boolean isMoveLegal(
      final GameEngine engine, final Dragboard dragboard, final int toRow, final int toCol) {
    return RuleChecker.isMoveLegal(engine.getState(), buildMove(dragboard, toRow, toCol));
  }

  /**
   * Builds a move from the dragboard origin and the target coordinates.
   *
   * @param dragboard dragboard containing the origin position
   * @param toRow target row
   * @param toCol target column
   * @return the constructed pawn move
   */
  private static Move buildMove(final Dragboard dragboard, final int toRow, final int toCol) {
    int[] from = parsePosition((String) dragboard.getContent(PAWN_FORMAT));
    Position origin = new Position(from[0], from[1]);
    Position destination = new Position(toRow, toCol);
    return Move.pawn(origin, destination);
  }

  /**
   * Parses a position stored as "row,col".
   *
   * @param text text representation of the position
   * @return an array containing row and column
   */
  private static int[] parsePosition(final String text) {
    String[] parts = text.split(",");
    return new int[] {Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
  }

  /**
   * Returns the row or column index of a node in the grid.
   *
   * @param node node placed in the grid
   * @param isRow true to get the row index, false for the column index
   * @return the requested index, or 0 if it is null
   */
  private static int getIndex(final Node node, final boolean isRow) {
    Integer index = isRow ? GridPane.getRowIndex(node) : GridPane.getColumnIndex(node);
    return index == null ? 0 : index;
  }
}
