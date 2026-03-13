package fr.bordeaux.ai;

import java.util.List;

import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;

/**
 * Intermediate heuristic that evaluates the relative advantage between players.
 * This strategy calculates the difference between the AI's progress and 
 * the opponent's progress to determine who is closer to winning.
 */
public class MediumHeuristic implements HeuristicEvaluator{

    /** The identifier of the AI player. */
    private final String aiName;

    /**
     * Constructs a MediumHeuristic.
     *
     * @param aiName The name of the AI player.
     */
    public MediumHeuristic(final String aiName) {
        this.aiName = aiName;
    }

    /**
     * Calculates a relative score by subtracting the opponent's progress from the AI's progress.
     * @param gameState The current game state.
     * @return A score representing the lead (or delay) relative to the opponent.
     * @throws IllegalStateException if a player is missing from the state.
     */
    @Override
    public int evaluate(final GameState gameState){
        final List<Player> listPlayer = gameState.getPlayers();
        Player enemie = null;
        Player player = null;   
        for (final Player otherPlayer : listPlayer) {
            if (otherPlayer.getName().equals(this.aiName)) {
                player = otherPlayer;
            } else {
                enemie = otherPlayer;
            }
        }
        final Position playerStartingPosition = player.getStartingPosition();
        final Position playerActualPosition = player.getPosition();
        if (enemie == null) {
            throw new IllegalStateException("No enemie found !");
        }
        final Position enemieStartingPosition = enemie.getStartingPosition();
        final Position enemieActualPosition = enemie.getPosition();
        final int evaluateHeuristic;

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
