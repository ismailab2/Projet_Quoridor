package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;

public class HintCommand implements Command {

  public HintCommand() {
    System.out.println("To implement");
  }

  @Override
  public GameEngine execute(GameEngine engine) {
    System.out.println("To implement");
    return engine;
  }

  @Override
  public void helpText() {
    System.out.println("=== HINT Command ===\n");
    System.out.println("Usage:");
    System.out.println("  hint\n");
    System.out.println("Description:");
    System.out.println("  Provides a suggestion for the next move.\n");
    System.out.println("  The suggested move is computed based on the current game state.\n");
  }
}
