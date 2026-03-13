package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;
import fr.bordeaux.persistence.GameSaver;

/** Command that saves the current game state to a file. */
public class SaveCommand implements Command {

  private String[] tokens;

  /**
   * Creates a SaveCommand using the specified file path.
   *
   * @param p the path of the file where the game will be saved
   */
  public SaveCommand(String p) {
    this(new String[] {"save", p});
  }

  /**
   * Creates a SaveCommand using the provided CLI tokens.
   *
   * @param t the tokens entered by the user
   */
  public SaveCommand(String[] t) {
    this.tokens = t;
  }

  /**
   * Executes the save command. Writes the current game state to the specified file.
   *
   * @param engine the current game engine
   * @return the unchanged game engine
   */
  @Override
  public GameEngine execute(GameEngine engine) {
    try {
      GameSaver.saveGame(engine.getState(), tokens[1]);
    } catch (Exception e) {
      System.err.println("Ca va pas du tout : " + e);
    }
    return engine;
  }

  /** Displays help information about the save command, including its usage and example. */
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
