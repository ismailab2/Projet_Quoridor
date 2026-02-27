package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;

public class ShowCommand implements Command {

  public ShowCommand() {
    System.out.println("To implement");
  }

  @Override
  public GameEngine execute(GameEngine engine) {
    System.out.println("To implement");
    return engine;
  }

  @Override
  public void helpText() {
    System.out.println("To implement");
  }
}
