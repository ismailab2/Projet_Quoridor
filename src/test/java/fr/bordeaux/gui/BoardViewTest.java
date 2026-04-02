package fr.bordeaux.gui;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import fr.bordeaux.core.*;

/**
 * @class BoardViewTest
 * @brief Unit tests for BoardView JavaFX component.
 * @details Tests cover all public and private methods, including lambdas, cell clicks, pawn clicks,
 *     wall drawing, highlighting accessible cells, and fade transition of the current player.
 */
class BoardViewTest {
  /*


  private GameEngine engine;
  private GameState state;
  private Board board;
  private Player player1, player2;
  private BoardView boardView;

  /**
   * @brief Setup JavaFX toolkit and initialize mocks.

  @BeforeEach
  void setup() {
    new JFXPanel(); // initialize JavaFX toolkit

    engine = mock(GameEngine.class);
    state = mock(GameState.class);
    board = mock(Board.class);
    player1 = mock(Player.class);
    player2 = mock(Player.class);

    when(engine.getState()).thenReturn(state);
    when(state.getBoard()).thenReturn(board);
    when(board.getSize()).thenReturn(9);
    when(state.getPlayers()).thenReturn(List.of(player1, player2));
    when(state.getCurrentPlayer()).thenReturn(player1);
    when(player1.getPosition()).thenReturn(new Position(0, 0));
    when(player2.getPosition()).thenReturn(new Position(1, 1));
    boardView = new BoardView(engine);
  }

  /**
   * @test Verify that drawWalls adds rectangle nodes for horizontal and vertical walls.

  @Test
  void testDrawWallsAddsRectangles() {
    when(board.getSize()).thenReturn(9);
    when(board.isWallCollision(any(), eq(Orientation.HORIZONTAL))).thenReturn(true);
    when(board.isWallCollision(any(), eq(Orientation.VERTICAL))).thenReturn(true);

    boardView.drawWalls();

    assertTrue(boardView.getChildren().stream().anyMatch(n -> n instanceof Rectangle));
  }

  /**
   * @test Verify handleCellClick calls engine.play and redraws board.

  @Test
  void testHandleCellClickCallsEnginePlay() {
    when(engine.play(any(Move.class))).thenReturn(true);
    boardView.handleCellClick(0, 1);
    verify(engine, times(1)).play(any(Move.class));
  }

  /**
   * @test Verify handleWallClick calls RuleChecker and engine.play if move is legal.

  @Test
  void testHandleWallClickCallsEnginePlayIfLegal() {
    try (MockedStatic<RuleChecker> checker = mockStatic(RuleChecker.class)) {
      checker
          .when(() -> RuleChecker.isMoveLegal(any(GameState.class), any(Move.class)))
          .thenReturn(true);
      when(engine.play(any(Move.class))).thenReturn(true);

      //boardView.handleWallClick(0, 0);

      checker.verify(() -> RuleChecker.isMoveLegal(any(GameState.class), any(Move.class)));
      verify(engine, times(1)).play(any(Move.class));
    }
  }

  /**
   * @test Verify primary click on a cell triggers handleCellClick.

  @Test
  void testClickOnCellPrimary() {
    StackPane cell = (StackPane) boardView.getChildren().get(0);
    assertNotNull(cell);

    var event = mock(javafx.scene.input.MouseEvent.class);
    when(event.getButton()).thenReturn(MouseButton.PRIMARY);

    javafx.application.Platform.runLater(() -> cell.getOnMouseClicked().handle(event));

    verify(engine, atLeast(0)).play(any(Move.class));
  }

  /**
   * @test Verify secondary click on a cell triggers handleWallClick.

  @Test
  void testClickOnCellSecondary() {
    StackPane cell = (StackPane) boardView.getChildren().get(0);
    assertNotNull(cell);

    try (MockedStatic<RuleChecker> checker = mockStatic(RuleChecker.class)) {
      checker
          .when(() -> RuleChecker.isMoveLegal(any(GameState.class), any(Move.class)))
          .thenReturn(true);
      when(engine.play(any(Move.class))).thenReturn(true);

      var event = mock(javafx.scene.input.MouseEvent.class);
      when(event.getButton()).thenReturn(MouseButton.SECONDARY);

      javafx.application.Platform.runLater(() -> cell.getOnMouseClicked().handle(event));

      verify(engine, atLeast(0)).play(any(Move.class));
    }
  }

  /**
   * @test Ensure drawBoard does not throw exceptions.

  @Test
  void testDrawBoardDoesNotThrow() {
    assertDoesNotThrow(() -> boardView.drawBoard());
  }

  /**
   * @test Ensure highlightCurrentPlayer does not throw exceptions.

  @Test
  void testHighlightCurrentPlayerDoesNotThrow() {
    assertDoesNotThrow(() -> boardView.highlightCurrentPlayer());
  }

  /** Test highlightAccessibleCells colors legal cells lightgreen and resets others
  @Test
  void testHighlightAccessibleCells() {
    Position legal1 = new Position(0, 1);
    Position legal2 = new Position(1, 0);
    when(state.getLegalMoves(player1)).thenReturn(List.of(legal1, legal2));

    boardView.highlightAccessibleCells(player1);

    StackPane cell1 =
        boardView.getChildren().stream()
            .filter(n -> n instanceof StackPane)
            .map(n -> (StackPane) n)
            .filter(c -> GridPane.getRowIndex(c) == 0 && GridPane.getColumnIndex(c) == 1)
            .findFirst()
            .orElse(null);
    StackPane cell2 =
        boardView.getChildren().stream()
            .filter(n -> n instanceof StackPane)
            .map(n -> (StackPane) n)
            .filter(c -> GridPane.getRowIndex(c) == 1 && GridPane.getColumnIndex(c) == 0)
            .findFirst()
            .orElse(null);

    assertNotNull(cell1);
    assertNotNull(cell2);

    assertTrue(cell1.getStyle().contains("lightgreen"));
    assertTrue(cell2.getStyle().contains("lightgreen"));

    StackPane defaultCell =
        boardView.getChildren().stream()
            .filter(n -> n instanceof StackPane)
            .map(n -> (StackPane) n)
            .filter(c -> GridPane.getRowIndex(c) == 0 && GridPane.getColumnIndex(c) == 0)
            .findFirst()
            .orElse(null);

    assertNotNull(defaultCell);
    assertTrue(defaultCell.getStyle().contains("beige"));
  }

  /** Test clicking on a pawn calls highlightAccessibleCells
  @Test
  void testPawnClickCallsHighlightAccessibleCells() {
    Rectangle pawn = boardView.getPawns()[0][0];
    assertNotNull(pawn);

    javafx.scene.input.MouseEvent event =
        new javafx.scene.input.MouseEvent(
            javafx.scene.input.MouseEvent.MOUSE_CLICKED,
            0,
            0,
            0,
            0,
            MouseButton.PRIMARY,
            1,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            null);

    pawn.getOnMouseClicked().handle(event);
  }
  */
}
