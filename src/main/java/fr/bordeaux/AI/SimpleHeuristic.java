package main.java.fr.bordeaux.AI;

import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;

/**
   * Heuristic 1 (Simple): Calculates the direct distance covered by the AI. Compares rows or
   * columns based on the player's starting side.
*/
public class SimpleHeuristic implements HeuristicEvaluator {


    private final String aiName;

    public SimpleHeuristic(final String aiName) {
        this.aiName = aiName;
    }

    @Override
    public int evaluate(GameState gameState) {

        Player me = null;
        for (Player p : gameState.getPlayers()) {
            if (p.getName().equals(this.aiName)) {
                me = p;
                break;
            }
        }
        if (me == null) throw new IllegalStateException("AI player not found");

        Position startPos = me.getStartingPosition();
        Position actualPos = me.getPosition();
        int evaluateHeuristic;
        if (startPos.getX() == 0) {
            evaluateHeuristic =  actualPos.getX() - startPos.getX();
        } else if (startPos.getX() == gameState.getBoard().getSize() - 1) {
            evaluateHeuristic =  startPos.getX() - actualPos.getX();
        } else if (startPos.getY() == 0) {
            evaluateHeuristic =  actualPos.getY() - startPos.getY();
        } else {
            evaluateHeuristic =  startPos.getY() - actualPos.getY();
        }
        return evaluateHeuristic;
    }
}
