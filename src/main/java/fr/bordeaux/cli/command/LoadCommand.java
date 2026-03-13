package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;
import fr.bordeaux.persistence.GameLoader;

/**
 * Command that loads a saved game from a file. The loaded game replaces the current game engine
 * state.
 */
public class LoadCommand implements Command {

  private String[] tokens;

  /**
   * Creates a LoadCommand using a file path.
   *
   * @param p the path of the save file to load
   */
  public LoadCommand(String p) {
    this(new String[] {"load", p});
  }

  /**
   * Creates a LoadCommand with the provided command tokens.
   *
   * @param t the tokens entered by the user in the CLI
   */
  public LoadCommand(String[] t) {
    this.tokens = t;
  }

  /**
   * Executes the load command. Attempts to load a saved game from the file specified in the tokens.
   * If loading succeeds, a new GameEngine is created with the loaded state.
   *
   * @param engine the current game engine
   * @return a new GameEngine containing the loaded game state, or the current engine if loading
   *     fails
   */
  @Override
  public GameEngine execute(GameEngine engine) {
    if (tokens.length < 2) {
      System.out.println("Usage: load FILE");
      return engine;
    }
    try {
      return new GameEngine(GameLoader.loadGame(tokens[1]));
    } catch (Exception e) {
      System.err.println("Ca va pas du tout : " + e);
    }
    return engine;
  }

  /**
   * Displays help information about the load command, including its usage, arguments, and an
   * example.
   */
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
