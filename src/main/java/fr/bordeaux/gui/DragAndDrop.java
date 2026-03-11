package fr.bordeaux.gui;

import fr.bordeaux.core.*;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.input.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

// Handles drag and drop of pawns on the game board
public class DragAndDrop {
  // Identifies the data type carried during the drag (pawn position)
  private static final DataFormat PAWN_FORMAT = new DataFormat("quoridor/pawn");

  private static final String STYLE_BASE = "-fx-border-color: black; -fx-background-color: ";
  private static final String COLOR_NORMAL = "beige;";
  private static final String COLOR_LEGAL = "lightgreen;";
  private static final String COLOR_ILLEGAL = "lightgray;";

  // Enables drag and drop and re-attaches handlers after each move
  public static void install(GameEngine engine, BoardView boardView) {
    Runnable attachHandlers = () -> attachHandlers(engine, boardView);
    Platform.runLater(attachHandlers);
    engine.getState().addListener(() -> Platform.runLater(attachHandlers));
  }

  // Loops through all cells and attaches drag and drop handlers
  private static void attachHandlers(GameEngine engine, BoardView boardView) {
    for (Node node : boardView.getChildren()) {
      if (!(node instanceof StackPane cell)) continue;
      int row = getIndex(cell, true);
      int col = getIndex(cell, false);
      installDropOnCell(engine, boardView, cell, row, col);
      for (Node child : cell.getChildren()) {
        if (child instanceof Rectangle pawn) installDragOnPawn(engine, pawn, row, col);
      }
    }
  }

  // Allows dragging only the current player's pawn
  private static void installDragOnPawn(GameEngine engine, Rectangle pawn, int row, int col) {
    pawn.setOnDragDetected(
        event -> {
          Position currentPosition = engine.getState().getCurrentPlayer().getPosition();

          // Only the current player's pawn can be dragged
          if (currentPosition.getX() != row || currentPosition.getY() != col) {
            event.consume();
            return;
          }

          // Store the pawn position in the Dragboard
          Dragboard dragboard = pawn.startDragAndDrop(TransferMode.MOVE);
          ClipboardContent content = new ClipboardContent();
          content.put(PAWN_FORMAT, row + "," + col);
          dragboard.setContent(content);
          event.consume();
        });
  }

  // Handles all drop events on a target cell
  private static void installDropOnCell(
      GameEngine engine, BoardView boardView, StackPane cell, int toRow, int toCol) {

    // Highlight the cell in green (legal) or gray (illegal)
    cell.setOnDragEntered(
        event -> {
          if (event.getDragboard().hasContent(PAWN_FORMAT)) {
            boolean isLegal = isMoveLegal(engine, event.getDragboard(), toRow, toCol);
            cell.setStyle(STYLE_BASE + (isLegal ? COLOR_LEGAL : COLOR_ILLEGAL));
          }
          event.consume();
        });

    // Restore normal color when the pawn leaves the cell
    cell.setOnDragExited(
        event -> {
          cell.setStyle(STYLE_BASE + COLOR_NORMAL);
          event.consume();
        });

    // Allow the drop only if the move is legal
    cell.setOnDragOver(
        event -> {
          if (event.getDragboard().hasContent(PAWN_FORMAT)
              && isMoveLegal(engine, event.getDragboard(), toRow, toCol))
            event.acceptTransferModes(TransferMode.MOVE);
          event.consume();
        });

    // Play the move when the pawn is dropped
    cell.setOnDragDropped(
        event -> {
          boolean success = false;
          if (event.getDragboard().hasContent(PAWN_FORMAT)
              && isMoveLegal(engine, event.getDragboard(), toRow, toCol)) {
            success = engine.play(buildMove(event.getDragboard(), toRow, toCol));
            if (success) boardView.drawBoard();
          }
          event.setDropCompleted(success);
          event.consume();
        });

    // Restore normal color at the end of the drag
    cell.setOnDragDone(
        event -> {
          cell.setStyle(STYLE_BASE + COLOR_NORMAL);
          event.consume();
        });
  }

  // Checks if the move to the target cell is allowed by the rules
  private static boolean isMoveLegal(GameEngine engine, Dragboard dragboard, int toRow, int toCol) {
    return RuleChecker.isMoveLegal(engine.getState(), buildMove(dragboard, toRow, toCol));
  }

  // Builds a Move from the Dragboard position and the target cell
  private static Move buildMove(Dragboard dragboard, int toRow, int toCol) {
    int[] from = parsePosition((String) dragboard.getContent(PAWN_FORMAT));
    Position origin = new Position(from[0], from[1]);
    Position destination = new Position(toRow, toCol);
    return Move.pawn(origin, destination);
  }

  // Converts "row,col" into an integer array Example: "3,4" in [3, 4]
  private static int[] parsePosition(String text) {
    String[] parts = text.split(",");
    return new int[] {Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
  }

  // Returns the row or column index of a cell in the GridPane
  private static int getIndex(Node node, boolean isRow) {
    Integer index = isRow ? GridPane.getRowIndex(node) : GridPane.getColumnIndex(node);
    return index == null ? 0 : index;
  }
}
