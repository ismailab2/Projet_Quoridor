package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;
import fr.bordeaux.persistence.GameLoader;

public class LoadCommand implements Command {

  private String[] tokens;

  public LoadCommand(String p) {
    this(new String[] {"load", p});
  }

  public LoadCommand(String[] t) {
    this.tokens = t;
  }

  @Override
  public GameEngine execute(GameEngine engine) {
    try {
      return new GameEngine(GameLoader.loadGame(tokens[1]));
    } catch (Exception e) {
      System.err.println("Ca va pas du tout : " + e);
    }
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
