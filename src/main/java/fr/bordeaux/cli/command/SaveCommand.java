package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;
import fr.bordeaux.persistence.GameSerializer;

public class SaveCommand implements Command {

  private String[] tokens;

  public SaveCommand(String p) {
    this(new String[] {"save", p});
  }

  public SaveCommand(String[] t) {
    this.tokens = t;
  }

  @Override
  public GameEngine execute(GameEngine engine) {
    try {
      GameSerializer.saveGame(engine.getState(), tokens[1]);
    } catch (Exception e) {
      System.err.println("Ca va pas du tout : " + e);
    }
    return engine;
  }

  @Override
  public void helpText() {
    System.out.println("=== SAVE Command ===\n");
    System.out.println("Usage:");
    System.out.println("  save FILE\n");
    System.out.println("Description:");
    System.out.println("  Saves the current game state into the specified file FILE.\n");
    System.out.println("Arguments:");
    System.out.println("  FILE   : Destination file path (required).\n");
    System.out.println("Example:");
    System.out.println("  save mygame.txt\n");
  }
}
