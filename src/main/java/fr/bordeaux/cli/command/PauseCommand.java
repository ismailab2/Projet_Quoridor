package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;

public class PauseCommand implements Command {

  public PauseCommand() {}

  @Override
  public GameEngine execute(GameEngine engine) {
    if (engine.getState().isBlitz()) engine.getState().setBlitz(false);
    else System.out.println("Not in blitz mode, pause is doign nothing.");
    return engine;
  }

  @Override
  public void helpText() {
    System.out.println("=== PAUSE Command ===\n");
    System.out.println("Usage:");
    System.out.println("  pause\n");
    System.out.println("Description:");
    System.out.println("  Pauses the game timer if the game mode is Blitz.\n");
    System.out.println("  If the game is not in Blitz mode, this command has no effect.\n");
  }
}
