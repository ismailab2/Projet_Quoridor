package fr.bordeaux.ai;

import fr.bordeaux.core.GameState;

/**
 * Functional interface for evaluating a game state in the Quoridor game.
 * This interface follows the Strategy Pattern, allowing different heuristic 
 * algorithms to be swapped at runtime within the Minimax search.
 */
@FunctionalInterface
public interface HeuristicEvaluator {
    /**
     * Evaluates the current game board and returns a numerical score.
     *
     * @param gameState The current state of the game to be evaluated.
     * @return A score representing the advantage 
     * (positive for the AI, negative for the opponent).
     */
    int evaluate(GameState gameState);
}
