package fr.bordeaux.core;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import fr.bordeaux.core.GameEngine;
import fr.bordeaux.core.Player;
public class BlitzManager {
    private GameEngine gameEngine;
    private ScheduledExecutorService scheduler;
    private Map<Player, Long> remainingsTimes = new HashMap<>();
    private boolean isRunning = false;

    public BlitzManager(GameEngine gameEngine, int maxTime){
        this.gameEngine = gameEngine;
        for(Player player : this.gameEngine.getState().getPlayers()){
            remainingsTimes.put(player, maxTime * 60 * 1000L);
        }
    }

    public void start(){
        if(isRunning){
            return;
        }
        isRunning = true;
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::task, 1, 1, TimeUnit.SECONDS);
    }

    public void pause(){
        if(!isRunning){
            return;
        }
        if(scheduler!= null && scheduler.isShutdown()){
            scheduler.shutdown();
        }

    }

    private void task(){
        
        if(gameEngine.getState().isPaused() || gameEngine.getState().isGameOver()){
            return;
        }
        Player target = this.gameEngine.getState().getCurrentPlayer();
        Long playerTime = remainingsTimes.get(target) -1000;
        remainingsTimes.put(target, playerTime);
    }

    public long getTimeLeft(Player player) {
        return remainingTimeMap.getOrDefault(player, 0L);
    }


}