package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;

public class PauseCommand implements Command {

  public PauseCommand() {
    System.out.println("To implement");
  }

  @Override
  public GameEngine execute(GameEngine engine) {
    System.out.println("To implement");
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
