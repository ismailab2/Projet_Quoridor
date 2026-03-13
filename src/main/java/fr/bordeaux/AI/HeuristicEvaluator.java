package main.java.fr.bordeaux.AI;

import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Player;

/**
 * Interface for evaluating a game state. 
 * Allows the MinimaxPlayer's heuristic calculations to be isolated.
 */
@FunctionalInterface
public interface HeuristicEvaluator {
    /**
     * Evaluates the game board and returns a score.
     *
     * @param gameState The current state of the game.
     * @return The calculated score (positive = advantageous, negative = disadvantageous).
     */
    int evaluate(GameState gameState);
}
