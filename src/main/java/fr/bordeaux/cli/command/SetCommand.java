package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;

/**
 * Command that modifies a configuration parameter of the game. The command expects an assignment in
 * the form PARAM=VALUE.
 */
public class SetCommand implements Command {

  /** Creates a SetCommand. Currently not implemented. */
  public SetCommand() {
    System.out.println("To implement");
  }

  /**
   * Executes the set command. Intended to update a configuration option in the game engine.
   *
   * @param engine the current game engine
   * @return the unchanged game engine
   */
  @Override
  public GameEngine execute(GameEngine engine) {
    System.out.println("To implement");
    return engine;
  }

  /** Displays help information about the set command, including its usage and an example. */
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
