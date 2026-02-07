package fr.bordeaux.AI;

import fr.bordeaux.core.Board;
import fr.bordeaux.core.Move;

public class AiPlayer {


    private boolean canSearch = true; // Became false when the timer has finished

    public AiPlayer(int d){
    }

    public Move IterativeDeepening(Board b){
        int d = 1;
        Move save = null;

        while (canSearch) {
            Move = MiniMax(b, d);
            d++;
        }

        return save;
    }


    public Move MiniMax(Board b, int d){
        best_move = null;
        best_score = Integer.MIN_VALUE;
        for (Move s : b.generate_legal_moves()){
            b.push(s);
            alpha = max(best_score, MinMax(b, d, Integer.MIN_VALUE ,Integer.MAX_VALUE));
            if(alpha > best_score){
                best_score = alpha;
                best_move = s;
            }
            b.pop();
        }

        return best_move;
    }



    private int MaxMin(Board b,int d, int alpha, int beta){

        if(b.is_game_over() || d==0){
            return heuristic(b);
        }

        for (Move s : b.generate_legal_moves()){
            b.push(s);
            alpha = max(alpha, MinMax(b, d-1, alpha ,beta));
            b.pop();
            if(alpha>=beta){
                return beta;
            }
        }
        return alpha;
    }



    private int MinMax(Board b,int d, int alpha, int beta){

        if(b.is_game_over() || d==0){
            return heuristic(b);
        }

        for (Move s : b.generate_legal_moves()){
            b.push(s);
            beta = min(beta, MaxMin(b, d-1, alpha ,beta));
            b.pop();
            if(alpha>=beta){
                return alpha;
            }
        }
        return beta;
    }

}
