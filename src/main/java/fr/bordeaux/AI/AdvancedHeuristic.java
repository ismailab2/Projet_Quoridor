package main.java.fr.bordeaux.AI;

import java.util.List;

import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;

  /**
   * Heuristic 3 (Advanced): Uses Breadth-First Search (BFS) to find the actual shortest path to the
   * goal and incorporates remaining wall counts weighted by coefficients K1 and K2. We will use a
   * neuronal network to determine them.
   */
public class AdvancedHeuristic implements HeuristicEvaluator{

    private static final int MAX_SCORE = 10_000;
    private static final int MIN_SCORE = -10_000;
    private final String aiName;

    public AdvancedHeuristic(final String aiName) {
        this.aiName = aiName;
    }

    @Override
    public int evaluate(GameState gameState) {
        int evaluateHeuristic;
        float k1 = 1.2f;
        float k2 = 0.8f;

        List<Player> lp = gameState.getPlayers();
        Player enemie = null;
        Player me = null;
        for (Player p : lp) {
        if (p.getName().equals(this.aiName)) {
            me = p;
        } else {
            enemie = p;
        }
        }

        List<Integer> lMax =
            gameState.getBoard()
                .getGraph()
                .findPathBFS(
                    gameState.getBoard().id(me.getPosition().getX(), me.getPosition().getY()),
                    gameState.getWinCondition(me));

        List<Integer> lMin =
            gameState.getBoard()
                .getGraph()
                .findPathBFS(
                    gameState.getBoard().id(enemie.getPosition().getX(), enemie.getPosition().getY()),
                    gameState.getWinCondition(enemie));

        int nbWallPlayer = me.getRemainingWalls();
        int nbWallEnemie = enemie.getRemainingWalls();

        if (lMax.size() <= 1) {
            evaluateHeuristic =  MAX_SCORE;
        }
        else if (lMin.size() <= 1) {
            evaluateHeuristic =  MIN_SCORE;
        }
        else{
            int scoreBFS = (lMin.size() - lMax.size()) * 10;
            int scoreWall = (int) (nbWallPlayer * k1 * 10 - nbWallEnemie * k2 * 10);
            evaluateHeuristic = scoreBFS + scoreWall;
        }
        
        return evaluateHeuristic;
    }
}
