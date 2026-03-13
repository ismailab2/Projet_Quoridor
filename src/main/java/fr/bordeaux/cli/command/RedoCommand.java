package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;

/**
 * Command that replays moves that were previously undone. The user can redo a single move or
 * multiple moves.
 */
public class RedoCommand implements Command {

  private String[] tokens;

  /** Creates a RedoCommand that replays one move by default. */
  public RedoCommand() {
    this(new String[] {"redo", "1"});
  }

  /**
   * Creates a RedoCommand using the provided CLI tokens.
   *
   * @param t the tokens entered by the user
   */
  public RedoCommand(String[] t) {
    this.tokens = t;
  }

  /**
   * Executes the redo command. Replays the specified number of undone moves.
   *
   * @param engine the current game engine
   * @return the updated game engine after redoing moves
   */
  @Override
  public GameEngine execute(GameEngine engine) {
    if (tokens.length == 1) engine.getState().redo();
    else if (tokens.length >= 3) {
      System.out.println("Redo command should look like : redo [N] with N an integer");
    }
    for (int i = 0; i < (tokens.length > 1 ? Integer.parseInt(tokens[1]) : 1); i++)
      engine.getState().redo();
    return engine;
  }

  /** Displays help information about the redo command, including its usage and examples. */
  @Override
  public void helpText() {
    System.out.println("=== REDO Command ===\n");
    System.out.println("Usage:");
    System.out.println("  redo");
    System.out.println("  redo [N]\n");
    System.out.println("Description:");
    System.out.println("  Replays previously undone moves.\n");
    System.out.println("  - If no number is specified, one move is redone.");
    System.out.println("  - If N is specified, N moves are redone.\n");
    System.out.println("Examples:");
    System.out.println("  redo      -> redo one move");
    System.out.println("  redo 3    -> redo three moves\n");
  }
}
