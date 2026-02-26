package fr.bordeaux.AI;

import java.util.List;
import java.util.Random;

import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Move;
import fr.bordeaux.core.Player;

public class RandomPlayer extends Player{
    

    @JsonCreator
    public RandomPlayer(@JsonProperty("name") String name, @JsonProperty("positionStart") Position positionStart){
        super(name, positionStart);
    }

    @Override
    public Move getNextMove(GameState state) {
        List<Move> l = state.generate_legal_moves();
        Random random = new Random();
        return l.get( random.nextInt(l.size()));
    }

}
