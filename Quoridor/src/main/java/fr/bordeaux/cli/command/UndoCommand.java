package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;

public class UndoCommand implements Command {

  private String[] tokens;

  public UndoCommand() {
    System.out.println("To implement");
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
    System.out.println("To implement");
  }
}
