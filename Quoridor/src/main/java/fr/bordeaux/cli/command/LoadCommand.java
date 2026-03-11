package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;

public class LoadCommand implements Command {

  public LoadCommand() {
    System.out.println("To implement");
  }

  @Override
  public GameEngine execute(GameEngine engine) {
    System.out.println("To implement");
    return engine;
  }

  @Override
  public void helpText() {
    System.out.println("=== LOAD Command ===\n");
    System.out.println("Usage:");
    System.out.println("  load FILE\n");
    System.out.println("Description:");
    System.out.println("  Loads a saved game from the specified FILE.\n");
    System.out.println("Arguments:");
    System.out.println("  FILE   : Path to the save file (required).\n");
    System.out.println("Example:");
    System.out.println("  load savegame.txt\n");
  }
}
