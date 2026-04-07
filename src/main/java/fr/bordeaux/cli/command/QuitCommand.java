package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;
import fr.bordeaux.util.Logger;
import org.jline.reader.LineReader;

/** CLI command that exits the application after a save prompt. */
public class QuitCommand implements Command {
  /** Reader used for user interaction. */
  private final LineReader reader;

  /** Logger to display closing information. */
  private static final Logger LOGGER = Logger.getInstance();

  /**
   * Creates a QuitCommand using the provided line reader to interact with the user.
   *
   * @param reader the LineReader used to read user input
   */
  public QuitCommand(final LineReader reader) {
    this.reader = reader;
  }

  /** Creates a QuitCommand without a predefined reader. */
  public QuitCommand() {
    this(null);
  }

  /**
   * Executes quit. Asks to save before exiting.
   *
   * @param engine the current game engine
   * @return the current game engine
   */
  @Override
  public GameEngine execute(final GameEngine engine) {
    try {
      final String response = reader.readLine("Save before quitting? [y/N] >> ");
      if ("y".equalsIgnoreCase(response) || "yes".equalsIgnoreCase(response)) {
        final String path = reader.readLine("File path >> ");
        new SaveCommand(path).execute(engine);
      }
      LOGGER.info("Exiting Quoridor...");
      return engine;
    } catch (final Exception e) {
      LOGGER.error("Error during quit process", e);
    }
    return engine;
  }

  /** Displays help information for the quit command. */
  @Override
  public void helpText() {
    LOGGER.info("=== QUIT Command ===\n");
    LOGGER.info("Usage:");
    LOGGER.info("  quit\n");
    LOGGER.info("Description:");
    LOGGER.info("  Exits the application.\n");
    LOGGER.verbose("Information:");
    LOGGER.verbose("  This command will ask you if you want to save before quitting.\n");
  }
}
