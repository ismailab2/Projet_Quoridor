package fr.bordeaux.gui;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import fr.bordeaux.core.*;
import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DragAndDropTest {

  private GameEngine engine;
  private GameState state;
  private BoardView boardView;

  @BeforeAll
  static void initJavaFx() {
    new JFXPanel();
  }

  // Create a fresh game before each test
  @BeforeEach
  void setUp() throws Exception {
    Board board = new Board(9);
    state = new GameState(board, Player.createDefaultPlayers());
    engine = new GameEngine(state, false, false, false, false, 10);
    runAndWait(() -> boardView = new BoardView(engine));
    runAndWait(() -> DragAndDrop.install(engine, boardView));
    waitForFxEvents();
  }

  // dragExited handler
  // When the mouse leaves a cell, it must go back to beige
  @Test
  void dragExited_resetsStyleToBeige() throws Exception {
    StackPane targetCell = findCell(1, 4);
    DragEvent event = mock(DragEvent.class);

    runAndWait(
        () -> {
          targetCell.setStyle("-fx-border-color: black; -fx-background-color: lightgreen;");
          targetCell.getOnDragExited().handle(event);
        });

    runAndWait(() -> assertTrue(targetCell.getStyle().contains("beige")));
    verify(event).consume();
  }

  // dragDropped handler - legal move
  // When the pawn is dropped on a legal cell, the position must be updated
  @Test
  void dragDropped_legalMove_updatesGameState() throws Exception {
    StackPane targetCell = findCell(1, 4);

    Dragboard dragboard = mock(Dragboard.class);
    DragEvent event = mock(DragEvent.class);

    when(dragboard.hasContent(getPawnFormat())).thenReturn(true);
    when(dragboard.getContent(getPawnFormat())).thenReturn("0,4");
    when(event.getDragboard()).thenReturn(dragboard);

    runAndWait(() -> targetCell.getOnDragDropped().handle(event));
    waitForFxEvents();

    assertEquals(new Position(1, 4), state.getPlayers().get(0).getPosition());
    assertEquals("Bob", state.getCurrentPlayer().getName());
    verify(event).setDropCompleted(true);
    verify(event).consume();
  }

  // dragDropped handler - illegal move
  // When the pawn is dropped on an illegal cell, nothing must happen
  @Test
  void dragDropped_illegalMove_isRejected() throws Exception {
    StackPane targetCell = findCell(5, 5);

    Dragboard dragboard = mock(Dragboard.class);
    DragEvent event = mock(DragEvent.class);

    when(dragboard.hasContent(getPawnFormat())).thenReturn(true);
    when(dragboard.getContent(getPawnFormat())).thenReturn("0,4");
    when(event.getDragboard()).thenReturn(dragboard);

    runAndWait(() -> targetCell.getOnDragDropped().handle(event));
    waitForFxEvents();

    assertEquals(new Position(0, 4), state.getPlayers().get(0).getPosition());

    verify(event).setDropCompleted(false);
    verify(event).consume();
  }

  @Test
  void install_setsHandlersOnPawnAndCells() throws Exception {
    StackPane startCell = findCell(0, 4);
    StackPane targetCell = findCell(1, 4);

    runAndWait(
        () -> {
          Rectangle pawn = (Rectangle) startCell.getChildren().get(0);

          assertNotNull(pawn.getOnDragDetected());
          assertNotNull(targetCell.getOnDragEntered());
          assertNotNull(targetCell.getOnDragExited());
          assertNotNull(targetCell.getOnDragOver());
          assertNotNull(targetCell.getOnDragDropped());
          assertNotNull(targetCell.getOnDragDone());
        });
  }

  @Test
  void dragEntered_legalMove_changesStyleToGreen() throws Exception {
    StackPane targetCell = findCell(1, 4);

    Dragboard dragboard = mock(Dragboard.class);
    DragEvent event = mock(DragEvent.class);

    when(dragboard.hasContent(getPawnFormat())).thenReturn(true);
    when(dragboard.getContent(getPawnFormat())).thenReturn("0,4");
    when(event.getDragboard()).thenReturn(dragboard);

    runAndWait(() -> targetCell.getOnDragEntered().handle(event));

    runAndWait(() -> assertTrue(targetCell.getStyle().contains("lightgreen")));
    verify(event).consume();
  }

  @Test
  void dragEntered_illegalMove_changesStyleToGray() throws Exception {
    StackPane targetCell = findCell(5, 5);

    Dragboard dragboard = mock(Dragboard.class);
    DragEvent event = mock(DragEvent.class);

    when(dragboard.hasContent(getPawnFormat())).thenReturn(true);
    when(dragboard.getContent(getPawnFormat())).thenReturn("0,4");
    when(event.getDragboard()).thenReturn(dragboard);

    runAndWait(() -> targetCell.getOnDragEntered().handle(event));

    runAndWait(() -> assertTrue(targetCell.getStyle().contains("lightgray")));
    verify(event).consume();
  }

  @Test
  void dragOver_legalMove_acceptsMoveTransferMode() throws Exception {
    StackPane targetCell = findCell(1, 4);

    Dragboard dragboard = mock(Dragboard.class);
    DragEvent event = mock(DragEvent.class);

    when(dragboard.hasContent(getPawnFormat())).thenReturn(true);
    when(dragboard.getContent(getPawnFormat())).thenReturn("0,4");
    when(event.getDragboard()).thenReturn(dragboard);

    runAndWait(() -> targetCell.getOnDragOver().handle(event));

    verify(event).acceptTransferModes(TransferMode.MOVE);
    verify(event).consume();
  }

  @Test
  void dragDone_resetsStyleToBeige() throws Exception {
    StackPane targetCell = findCell(1, 4);
    DragEvent event = mock(DragEvent.class);

    runAndWait(
        () -> {
          targetCell.setStyle("-fx-border-color: black; -fx-background-color: lightgreen;");
          targetCell.getOnDragDone().handle(event);
        });

    runAndWait(() -> assertTrue(targetCell.getStyle().contains("beige")));
    verify(event).consume();
  }

  @Test
  void dragDetected_onOpponentPawn_consumesEvent() throws Exception {
    StackPane opponentCell = findCell(8, 4);
    MouseEvent event = mock(MouseEvent.class);

    runAndWait(
        () -> {
          Rectangle pawn = (Rectangle) opponentCell.getChildren().get(0);
          pawn.getOnDragDetected().handle(event);
        });

    verify(event).consume();
  }

  @Test
  void dragOver_illegalMove_doesNotAcceptTransferMode() throws Exception {
    StackPane targetCell = findCell(5, 5);

    Dragboard dragboard = mock(Dragboard.class);
    DragEvent event = mock(DragEvent.class);

    when(dragboard.hasContent(getPawnFormat())).thenReturn(true);
    when(dragboard.getContent(getPawnFormat())).thenReturn("0,4");
    when(event.getDragboard()).thenReturn(dragboard);

    runAndWait(() -> targetCell.getOnDragOver().handle(event));

    verify(event, never()).acceptTransferModes(TransferMode.MOVE);
    verify(event).consume();
  }

  @Test
  void getIndex_returnsZeroWhenMissing() {
    StackPane cell = new StackPane();

    assertEquals(0, getIndexFromDragAndDrop(cell, true));
    assertEquals(0, getIndexFromDragAndDrop(cell, false));
  }

  @Test
  void getIndex_returnsAssignedValues() {
    StackPane cell = new StackPane();
    GridPane.setRowIndex(cell, 3);
    GridPane.setColumnIndex(cell, 5);

    assertEquals(3, getIndexFromDragAndDrop(cell, true));
    assertEquals(5, getIndexFromDragAndDrop(cell, false));
  }

  // Find a cell by its row and column
  private StackPane findCell(int row, int col) throws Exception {
    final StackPane[] result = new StackPane[1];
    runAndWait(
        () -> {
          result[0] =
              boardView.getChildren().stream()
                  .filter(StackPane.class::isInstance)
                  .map(StackPane.class::cast)
                  .filter(cell -> getIndex(cell, true) == row && getIndex(cell, false) == col)
                  .findFirst()
                  .orElseThrow();
        });
    return result[0];
  }

  private static int getIndexFromDragAndDrop(StackPane cell, boolean isRow) {
    try {
      var method =
          DragAndDrop.class.getDeclaredMethod("getIndex", javafx.scene.Node.class, boolean.class);
      method.setAccessible(true);
      return (int) method.invoke(null, cell, isRow);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  // Get row or column index of a cell in the GridPane
  private static int getIndex(StackPane cell, boolean isRow) {
    Integer index = isRow ? GridPane.getRowIndex(cell) : GridPane.getColumnIndex(cell);
    return index == null ? 0 : index;
  }

  // Access the private PAWN_FORMAT field using reflection
  private static DataFormat getPawnFormat() {
    try {
      Field field = DragAndDrop.class.getDeclaredField("PAWN_FORMAT");
      field.setAccessible(true);
      return (DataFormat) field.get(null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  // Run an action on the JavaFX thread and wait for it to finish
  private static void runAndWait(Runnable action) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    final Throwable[] error = new Throwable[1];
    Platform.runLater(
        () -> {
          try {
            action.run();
          } catch (Throwable t) {
            error[0] = t;
          } finally {
            latch.countDown();
          }
        });
    assertTrue(latch.await(5, TimeUnit.SECONDS), "Timeout on JavaFX thread");
    if (error[0] != null) throw new RuntimeException(error[0]);
  }

  // Wait for all pending JavaFX events to be processed
  private static void waitForFxEvents() throws Exception {
    runAndWait(() -> {});
  }

  // install()
  // Engine and game state must be correctly initialized
  @Test
  void install_isReady() {
    assertNotNull(engine);
    assertNotNull(state);
  }

  // The game must have at least 2 players
  @Test
  void install_minTwoPlayers() {
    assertTrue(state.getPlayers().size() >= 2);
  }

  // installDragOnPawn()
  // Current player must be at his start position
  @Test
  void drag_currentPlayerAtStart() {
    Position currentPos = state.getCurrentPlayer().getPosition();
    assertEquals(new Position(0, 4), currentPos);
  }

  // isMoveLegal()
  // A move one step forward must be accepted
  @Test
  void move_legal_isAccepted() {
    Move move = Move.pawn(new Position(0, 4), new Position(1, 4));
    assertTrue(RuleChecker.isMoveLegal(state, move));
    assertTrue(engine.play(move));
  }

  // A move far away must be rejected
  @Test
  void move_illegal_isRejected() {
    Move move = Move.pawn(new Position(0, 4), new Position(5, 5));
    assertFalse(RuleChecker.isMoveLegal(state, move));
    assertFalse(engine.play(move));
  }

  // installDropOnCell()
  // After a move, it must be the other player's turn
  @Test
  void drop_playerChanges() {
    assertEquals("Human", state.getCurrentPlayer().getName());
    engine.play(Move.pawn(new Position(0, 4), new Position(1, 4)));
    assertEquals("Bob", state.getCurrentPlayer().getName());
  }

  // After a move, the pawn position must be updated
  @Test
  void drop_positionUpdated() {
    engine.play(Move.pawn(new Position(0, 4), new Position(1, 4)));
    engine.play(Move.pawn(new Position(8, 4), new Position(7, 4)));
    // Human position must be (1,4)
    Player human = state.getPlayers().get(0);
    assertEquals(new Position(1, 4), human.getPosition());
  }

  // No move allowed when game is paused
  @Test
  void drop_whenPaused_isRejected() {
    engine.pause();
    Move move = Move.pawn(new Position(0, 4), new Position(1, 4));
    assertFalse(engine.play(move));
  }
}
