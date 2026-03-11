package fr.bordeaux.AI;

import java.util.Map;

import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Move;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;

public abstract class AiPlayer extends Player {

    public AiPlayer(String name, Position positionStart) {
        super(name, positionStart);
    }

    protected AiPlayer(String name, Position current, Position start, int walls, int score) {
        super(name, current, start, walls, score);
    }

    @Override
    public Boolean isAI() {
        return true;
    }

    @Override
    public abstract Player copy();

    @Override
    public abstract Move getNextMove(GameState state);

    public abstract Map<String, Object> getSettings(); // return all settings of minimax
    
}
