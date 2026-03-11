package fr.bordeaux.gui;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import fr.bordeaux.core.*;
import javafx.embed.swing.JFXPanel;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class BoardViewTest {

  @BeforeAll
  static void initJavaFX() {
    new JFXPanel(); // Initialize JavaFX toolkit
  }

  @Test
  void testBoardView9x9() {
    GameEngine engine = mock(GameEngine.class);
    GameState state = mock(GameState.class);
    Board board = mock(Board.class);

    when(engine.getState()).thenReturn(state);
    when(state.getBoard()).thenReturn(board);
    when(board.getSize()).thenReturn(9);

    Player player = mock(Player.class);
    when(player.getPosition()).thenReturn(new Position(0, 0));
    when(state.getPlayers()).thenReturn(java.util.List.of(player));
    when(state.getCurrentPlayer()).thenReturn(player);

    when(engine.play(any(Move.class))).thenReturn(true);

    try (MockedStatic<RuleChecker> ruleChecker = mockStatic(RuleChecker.class)) {
      ruleChecker.when(() -> RuleChecker.isMoveLegal(any(), any())).thenReturn(true);

      BoardView view = new BoardView(engine);
      assertEquals(81, view.getChildren().size());

      // Fire primary click on first cell
      StackPane firstCell = (StackPane) view.getChildren().get(0);
      firstCell
          .getOnMouseClicked()
          .handle(
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
                  null));

      // Fire secondary click on first cell (adds wall)
      firstCell
          .getOnMouseClicked()
          .handle(
              new javafx.scene.input.MouseEvent(
                  javafx.scene.input.MouseEvent.MOUSE_CLICKED,
                  0,
                  0,
                  0,
                  0,
                  MouseButton.SECONDARY,
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
                  null));

      // Verify engine.play() called at least twice (pawn + wall)
      verify(engine, atLeast(2)).play(any(Move.class));

      // DrawBoard should place the player's rectangle in first cell
      view.drawBoard();
      StackPane cell0 = (StackPane) view.getChildren().get(0);
      assertTrue(cell0.getChildren().stream().anyMatch(node -> node instanceof Rectangle));
    }
  }
}
