package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;

public class UndoCommand implements Command {

  private String[] tokens;

  public UndoCommand() {
    this(new String[] {"undo", "1"});
  }

  public UndoCommand(String[] t) {
    tokens = t;
  }

  @Override
  public GameEngine execute(GameEngine engine) {
    if (tokens.length == 1) engine.getState().undo();
    else if (tokens.length >= 3) {
      System.out.println("Undo command should look like : undo [N] with N an integer");
    }
    for (int i = 0; i < Integer.parseInt(tokens[1]); i++) engine.getState().undo();
    return engine;
  }

  @Override
  public void helpText() {
    System.out.println("=== UNDO Command ===\n");
    System.out.println("Usage:");
    System.out.println("  undo");
    System.out.println("  undo <N>\n");
    System.out.println("Description:");
    System.out.println("  Cancels previously played moves.\n");
    System.out.println("  - If no number is specified, one move is undone.");
    System.out.println("  - If N is specified, N moves are undone.\n");
    System.out.println("Examples:");
    System.out.println("  undo      -> undo last move");
    System.out.println("  undo 2    -> undo last two moves\n");
  }
}
