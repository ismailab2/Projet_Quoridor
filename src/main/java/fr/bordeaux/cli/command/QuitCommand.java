package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.UserInterruptException;

public class QuitCommand implements Command {

  private LineReader reader;

  public QuitCommand(LineReader r) {
    reader = r;
  }

  public QuitCommand() {
    this(null);
  }

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
