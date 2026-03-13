package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;

/**
 * Represents a command that can be executed in the CLI for the game.
 * Each command performs an action on the game engine and can provide
 * help information about its usage.
 */
public interface Command {
    /**
   * Executes the command, potentially modifying the game engine state.
   *
   * @param engine the current game engine
   * @return the updated game engine after executing the command
   */
  GameEngine execute(GameEngine engine);

    /**
   * Prints help information for the command to the console.
   * This should describe usage, arguments, and examples if applicable.
   */
  void helpText();
}
