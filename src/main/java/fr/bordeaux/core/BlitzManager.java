package fr.bordeaux.core;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manager for the Blitz mode in Quoridor. This class uses a background thread to decrement the
 * reflection time of the current player at regular intervals.
 */
public class BlitzManager {

  private final GameEngine gameEngine;
  private ScheduledExecutorService scheduler;
  private boolean running = false;

  /**
   * Constructs a BlitzManager associated with a game engine.
   *
   * @param gameEngine The game engine to monitor.
   */
  public BlitzManager(GameEngine gameEngine) {
    this.gameEngine = gameEngine;
  }

  /** Starts or resumes the countdown. Creates a new scheduled thread that executes every second. */
  public void start() {
    if (running) {
      return;
    }
    running = true;
    scheduler = Executors.newSingleThreadScheduledExecutor();
    scheduler.scheduleAtFixedRate(this::task, 1, 1, TimeUnit.SECONDS);
  }

  /**
   * Temporarily stops the countdown (Pause). Shuts down the current thread to stop all
   * decrementation.
   */
  public void pause() {
    if (!running) {
      return;
    }
    running = false;
    if (scheduler != null && !scheduler.isShutdown()) {
      scheduler.shutdownNow();
    }
  }

  /**
   * Internal task executed at each timer tick (every second). Identifies the active player,
   * decrements their time, and checks for loss conditions.
   */
  private void task() {

    GameState state = gameEngine.getState();
    if (state.isPaused() || state.isGameOver()) {
      return;
    }

    state.decrementCurrentPlayerTime(1000);

    if (state.isGameOver()) {
      pause();
    }
  }

  /**
   * Indicates whether the time manager is currently active.
   *
   * @return true if the timer is running, false otherwise.
   */
  public boolean isRunning() {
    return running;
  }
}
