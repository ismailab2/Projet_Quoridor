package fr.bordeaux.cli.command;

import fr.bordeaux.ai.MinimaxPlayer;
import fr.bordeaux.core.GameEngine;
import fr.bordeaux.core.Move;
import fr.bordeaux.core.Move.Type;
import fr.bordeaux.core.Orientation;
import fr.bordeaux.core.Position;

public class HintCommand implements Command {

  public HintCommand() {}

  @Override
  public GameEngine execute(GameEngine engine) {
    MinimaxPlayer ia =
        MinimaxPlayer.builder("hint", engine.getState().getCurrentPlayer().getPosition()).build();
    Move hint = ia.getNextMove(engine.getState());
    String bestMove = "";
    if (hint.getType() == Type.PAWN) {
      bestMove = posToString(hint.getFrom()) + "-" + posToString(hint.getTo());
    } else {
      char orientationChar = hint.getOrientation() == Orientation.HORIZONTAL ? 'h' : 'v';
      bestMove = posToString(hint.getTo()) + orientationChar;
    }
    System.out.println("Best move : " + bestMove);
    return engine;
  }

  private String posToString(Position p) {
    char letter = (char) ('a' + p.getX());
    int number = p.getY();
    return "" + letter + number;
  }

  @Override
  public void helpText() {
    System.out.println("=== HINT Command ===\n");
    System.out.println("Usage:");
    System.out.println("  hint\n");
    System.out.println("Description:");
    System.out.println("  Provides a suggestion for the next move.\n");
    System.out.println("  The suggested move is computed based on the current game state.\n");
  }
}
