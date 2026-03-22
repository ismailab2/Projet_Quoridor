package fr.bordeaux.core;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import fr.bordeaux.core.GameEngine;
import fr.bordeaux.core.Player;

/**
 * Manager for the Blitz mode in Quoridor.
 * This class uses a background thread to decrement the reflection time
 * of the current player at regular intervals.
 */
public class BlitzManager {
    
    private GameEngine gameEngine;
    private ScheduledExecutorService scheduler;
    private boolean isRunning = false;

    /**
     * Constructs a BlitzManager associated with a game engine.
     * * @param gameEngine The game engine to monitor.
     */
    public BlitzManager(GameEngine gameEngine){
        this.gameEngine = gameEngine;
    }

    /**
     * Starts or resumes the countdown.
     * Creates a new scheduled thread that executes every second.
     */
    public void start(){
        if(isRunning){
            return;
        }
        isRunning = true;
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::task, 1, 1, TimeUnit.SECONDS);  
    }

    /**
     * Temporarily stops the countdown (Pause).
     * Shuts down the current thread to stop all decrementation.
     */
    public void pause(){
        if(!isRunning){
            return;
        }
        isRunning = false;
        if(scheduler!= null && !scheduler.isShutdown()){
            scheduler.shutdownNow();
        }

    }

    /**
     * Internal task executed at each timer tick (every second).
     * Identifies the active player, decrements their time, and checks for loss conditions.
     */
    private void task(){
        
        GameState gameState = gameEngine.getState();
        if(gameState.isPaused() || gameState.isGameOver()){
            return;
        }
        Player target = gameState.getCurrentPlayer();
        Long playerTime = target.getRemainingTime() -1000;
        target.setRemainingTime( playerTime);

        if (playerTime <= 0){
            gameState.setGameOver(true);
            System.out.println("TIMEOUT ! " + target.getName() + " a perdu.");
            pause();
        }
        
    }

    /**
     * Indicates whether the time manager is currently active.
     * * @return true if the timer is running, false otherwise.
     */
    public boolean isRunning() {
        return isRunning;
    }

}