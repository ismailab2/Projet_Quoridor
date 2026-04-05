package fr.bordeaux.core;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manager for the Blitz mode in Quoridor. This class uses a background thread to decrement the
 * reflection time of the current player at regular intervals.
 */
public class BlitzManager {

  private static final long THRESHOLD_MS = 60_000;
  private static final int TICK_SLOW_MS = 1000;
  private static final int TICK_FAST_MS = 10;

  private GameEngine gameEngine;
  private ScheduledExecutorService scheduler;
  private boolean isRunning = false;

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
    if (isRunning) {
      return;
    }
    isRunning = true;
    long remaining = gameEngine.getState().getCurrentPlayer().getRemainingTime();
    int tick = remaining > THRESHOLD_MS ? TICK_SLOW_MS : TICK_FAST_MS;
    startWithTick(tick);
  }

  /**
   * Creates a new scheduler and starts it with the given tick interval.
   *
   * @param tickMs the interval in milliseconds between each timer tick
   */
  private void startWithTick(int tickMs) {
    scheduler = Executors.newSingleThreadScheduledExecutor();
    scheduler.scheduleAtFixedRate(() -> task(tickMs), tickMs, tickMs, TimeUnit.MILLISECONDS);
  }

  /**
   * Temporarily stops the countdown (Pause). Shuts down the current thread to stop all
   * decrementation.
   */
  public void pause() {
    if (!isRunning) {
      return;
    }
    isRunning = false;
    if (scheduler != null && !scheduler.isShutdown()) {
      scheduler.shutdownNow();
    }
  }

  /**
   * Internal task executed at each timer tick (every second). Identifies the active player,
   * decrements their time, and checks for loss conditions.
   */
  private void task(int tickMs) {
    try {
      GameState state = gameEngine.getState();
      if (state.isPaused() || state.isGameOver()) return;

      state.decrementCurrentPlayerTime(tickMs);
      if (state.isGameOver()) {
        pause();
        return;
      }

      if (tickMs == TICK_SLOW_MS && state.getCurrentPlayer().getRemainingTime() <= THRESHOLD_MS) {
        ScheduledExecutorService old = scheduler;
        new Thread(
                () -> {
                  old.shutdownNow();
                  startWithTick(TICK_FAST_MS);
                })
            .start();
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Indicates whether the time manager is currently active.
   *
   * @return true if the timer is running, false otherwise.
   */
  public boolean isRunning() {
    return isRunning;
  }
}
