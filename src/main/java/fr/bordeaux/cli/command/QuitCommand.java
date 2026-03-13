package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.UserInterruptException;

/**
 * Command that exits the CLI application. Before quitting, the user is prompted to optionally save
 * the current game.
 */
public class QuitCommand implements Command {

  private LineReader reader;

  /**
   * Creates a QuitCommand using the provided line reader to interact with the user.
   *
   * @param r the LineReader used to read user input
   */
  public QuitCommand(LineReader r) {
    reader = r;
  }

  /** Creates a QuitCommand without a predefined reader. */
  public QuitCommand() {
    this(null);
  }

  /**
   * Executes the quit command. Asks the user whether they want to save the game before exiting.
   *
   * @param engine the current game engine
   * @return the current game engine
   */
  @Override
  public GameEngine execute(GameEngine engine) {
    try {
      String line = reader.readLine("Save the game before quitting? [y/N] >> ");
      if (line.toLowerCase().equals("y") || line.toLowerCase().equals("yes")) {
        String save = reader.readLine("Path where you want to save it >> ");
        SaveCommand cmd = new SaveCommand(save);
        return cmd.execute(engine);
      } else {
        if (line.toLowerCase().equals("n") || line.toLowerCase().equals("no")) {
          System.out.println("You choose to not save...");
        } else {
          System.out.println("Unrecognizable answer... Game not saved.");
        }
        return engine;
      }
    } catch (UserInterruptException e) {
      System.err.println("Tout va bien, je m'arrête !\n");
      return engine;
    } catch (EndOfFileException e) {
      return engine;
    }
  }

  /** Displays help information about the quit command, including its usage and behavior. */
  @Override
  public void helpText() {
    System.out.println("=== QUIT Command ===\n");
    System.out.println("Usage:");
    System.out.println("  quit\n");
    System.out.println("Description:");
    System.out.println("  Exits the application.\n");
    System.out.println("Information:");
    System.out.println("  This command will ask you if you want to save before quitting.\n");
  }
}
