package fr.bordeaux.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import fr.bordeaux.ai.MinimaxPlayer;
import fr.bordeaux.core.BlitzManager;
import fr.bordeaux.core.Board;
import fr.bordeaux.core.GameEngine;
import fr.bordeaux.core.GameState;
import fr.bordeaux.core.HumanPlayer;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;



public class BlitzManagerTest {
    
    @Test
    public void testDecreaseTime(){
        Board board = new Board(9);
        List<Player> players = new ArrayList<>();
        players.add( MinimaxPlayer.builder("IA", new Position(8, 4)).withIterativeDeepening(true).withReflexion(2).build());
        players.add(new HumanPlayer( "Human", new Position(0, 4)));
        GameState state = new GameState(board, players);
        state.setBlitz(true);
        state.setLimitTimePlayer(10000); // 10 seconds
        GameEngine engine = new GameEngine(state);

        BlitzManager blitz = new BlitzManager(engine);
        try {
            Player player = state.getCurrentPlayer();
            long initialTime = player.getRemainingTime();

            blitz.start();

            Thread.sleep(1100); 

            long afterSleep = player.getRemainingTime();

            assertTrue(afterSleep < initialTime, "Time should be decreased");
            assertTrue(afterSleep <= 9000, "Remaining time should be less or equal to 9000 ms");
            
        } 
        catch(InterruptedException e){
            assertTrue(false);
        }
        finally {
            blitz.pause();
        }
    }

    @Test
    public void testLooseByTime(){
        Board board = new Board(9);
        List<Player> players = new ArrayList<>();
        players.add( new HumanPlayer( "Human", new Position(8, 4)));
        players.add(new HumanPlayer( "Human", new Position(0, 4)));
        GameState state = new GameState(board, players);
        state.setBlitz(true);
        state.setLimitTimePlayer(1000); // 10 seconds
        GameEngine engine = new GameEngine(state);

        BlitzManager blitz = new BlitzManager(engine);
        try {
            Player player = state.getCurrentPlayer();
            long initialTime = player.getRemainingTime();

            blitz.start();
            blitz.start();
            assertTrue(blitz.isRunning());

            Thread.sleep(2000); 

            long afterSleep = player.getRemainingTime();

            assertTrue(afterSleep == 0, "RemainingTime should be equal to 0");
            assertTrue(state.isGameOver());
            
        } 
        catch(InterruptedException e){
            assertTrue(false);
        }
        finally {
            blitz.pause();
        }
    }
}
