package fr.bordeaux.AI;

import java.util.List;
import java.util.Random;

import fr.bordeaux.core.Board;
import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Move;

public class AiPlayer { // MiniMax and IterativeDeepening are limited because it works with only 2 players. They will not work if the number of players is larger.


    private boolean canSearch = true; // Became false when the timer has finished

    public AiPlayer(int d){
    }

    public Move IterativeDeepening(GameState g){
        int d = 1;
        Move save = null;

        while (canSearch) {
            Move = MiniMax(g, d);
            d++;
        }

        return save;
    }


    public Move MiniMax(GameState g, int d){
        best_move = null;
        best_score = Integer.MIN_VALUE;
        for (Move s : g.generate_legal_moves()){
            g.push(s);
            alpha = max(best_score, MinMax(g, d, Integer.MIN_VALUE ,Integer.MAX_VALUE));
            if(alpha > best_score){
                best_score = alpha;
                best_move = s;
            }
            g.pop();
        }

        return best_move;
    }



    private int MaxMin(GameState g,int d, int alpha, int beta){

        if(g.is_game_over() || d==0){
            return heuristic(g);
        }

        for (Move s : g.generate_legal_moves()){
            g.push(s);
            alpha = max(alpha, MinMax(g, d-1, alpha ,beta));
            g.pop();
            if(alpha>=beta){
                return beta;
            }
        }
        return alpha;
    }



    private int MinMax(GameState g,int d, int alpha, int beta){

        if(g.is_game_over() || d==0){
            return heuristic(g);
        }

        for (Move s : g.generate_legal_moves()){
            g.push(s);
            beta = min(beta, MaxMin(g, d-1, alpha ,beta));
            g.pop();
            if(alpha>=beta){
                return alpha;
            }
        }
        return beta;
    }


    public Move randomMove(GameState g){ // It is not planned to be able to launch it in the game, it will just be used for testing
        List<Move> l = g.generate_legal_moves();
        Random random = new Random();
        return random.nextInt(l.size());
    }

}
