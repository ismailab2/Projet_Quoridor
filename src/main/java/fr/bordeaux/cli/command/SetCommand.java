package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;

public class SetCommand implements Command {

  public SetCommand() {
    System.out.println("To implement");
  }

  @Override
  public GameEngine execute(GameEngine engine) {
    System.out.println("To implement");
    return engine;
  }

  @Override
  public void helpText() {
    System.out.println("=== SET Command ===\n");
    System.out.println("Usage:");
    System.out.println("  set PARAM=VALUE\n");
    System.out.println("Description:");
    System.out.println("  Updates a configuration parameter of the game.\n");
    System.out.println("Arguments:");
    System.out.println("  PARAM   : Name of the configuration option.");
    System.out.println("  VALUE   : New value to assign.\n");
    System.out.println("Example:");
    System.out.println("  set debug=true\n");
  }
}
