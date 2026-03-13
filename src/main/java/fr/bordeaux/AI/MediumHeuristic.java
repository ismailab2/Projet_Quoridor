package fr.bordeaux.AI;

import java.util.List;

import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;

  /**
   * Heuristic 2 (Intermediate): Considers both the AI's and the opponent's positions. Calculates
   * the difference between the AI's progress and the opponent's progress.
   *
   */
public class MediumHeuristic implements HeuristicEvaluator{


    private final String aiName;

    public MediumHeuristic(final String aiName) {
        this.aiName = aiName;
    }

    @Override
    public int evaluate(GameState gameState){
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
        Position playerStartingPosition = me.getStartingPosition();
        Position playerActualPosition = me.getPosition();
        if (enemie == null) {
            throw new IllegalStateException("No enemie found !");
        }
        Position enemieStartingPosition = enemie.getStartingPosition();
        Position enemieActualPosition = enemie.getPosition();
        int evaluateHeuristic;

        if (playerStartingPosition.getX() == 0) {
            evaluateHeuristic =  playerActualPosition.getX() - playerStartingPosition.getX()- enemieStartingPosition.getX() - enemieActualPosition.getX();
        } else if (playerStartingPosition.getX() == gameState.getBoard().getSize() - 1) {
            evaluateHeuristic = playerStartingPosition.getX() - playerActualPosition.getX()
            - enemieActualPosition.getX() - enemieStartingPosition.getX();
        } else if (playerStartingPosition.getY() == 0) {
            evaluateHeuristic = playerActualPosition.getY() - playerStartingPosition.getY()
            - enemieStartingPosition.getY() - enemieActualPosition.getY();
        } else {
            evaluateHeuristic = playerStartingPosition.getY() - playerActualPosition.getY()
            - enemieActualPosition.getY() - enemieStartingPosition.getY();
        }
        return evaluateHeuristic;
    }
}
