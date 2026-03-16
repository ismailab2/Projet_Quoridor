package fr.bordeaux.cli.command;

import fr.bordeaux.ai.MCTSPlayer;
import fr.bordeaux.ai.MinimaxPlayer;
import fr.bordeaux.core.GameEngine;
import fr.bordeaux.core.Move;
import fr.bordeaux.core.Move.Type;
import fr.bordeaux.core.Orientation;
import fr.bordeaux.core.Position;
import fr.bordeaux.core.RuleChecker;

/**
 * Command that suggests the best possible move for the current player. The move is computed using a
 * Minimax AI based on the current game state.
 */
public class HintCommand implements Command {

  /** Creates a new HintCommand. */
  public HintCommand() {}

  /**
   * Executes the hint command. It creates a temporary AI player that evaluates the current game
   * state and suggests the best move for the current player.
   *
   * @param engine the current game engine
   * @return the unchanged game engine
   */
  @Override
  public GameEngine execute(GameEngine engine) {
/*     MinimaxPlayer ia =
        MinimaxPlayer.builder("hint", engine.getState().getCurrentPlayer().getPosition())
            .withHeuristic("advanced")
            .build(); */
    MCTSPlayer ia = new MCTSPlayer("hint", engine.getState().getCurrentPlayer().getPosition());
    Move hint = ia.getNextMove(engine.getState());
    System.out.println(RuleChecker.isMoveLegal(engine.getState(), hint));
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

  /**
   * Converts a board position into a human-readable string. The format follows the CLI coordinate
   * notation (e.g., a0, b3).
   *
   * @param p the position to convert
   * @return the string representation of the position
   */
  private String posToString(Position p) {
    char letter = (char) ('a' + p.getY());
    int number = p.getX() + 1;
    return "" + letter + number;
  }

  /** Displays help information about the hint command, including its usage and description. */
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
