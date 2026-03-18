package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;
import java.io.PrintStream;
import org.jline.reader.LineReader;

/**
 * Command that exits the CLI application. Before quitting, the user is prompted to optionally save
 * the current game.
 */
public class QuitCommand implements Command {
  /** Reader used for user interaction. */
  private final LineReader reader;

  /** Logger to display closing information. */
  private static final Logger logger = LoggerFactory.getLogger(QuitCommand.class);

  /** Stream where to display texts. */
  private final PrintStream writer = System.out;

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
      String response = reader.readLine("Save before quitting? [y/N] >> ");
      if (response.equalsIgnoreCase("y") || response.equalsIgnoreCase("yes")) {
        String path = reader.readLine("File path >> ");
        new SaveCommand(path).execute(engine);
      }
      logger.info("Exiting Quoridor...");
      return engine;
    } catch (Exception e) {
      logger.error("Error during quit process", e);
    }
    return engine;
  }

  /** Displays help information about the quit command, including its usage and behavior. */
  @Override
  public void helpText() {
    logger.info("=== QUIT Command ===\n");
    logger.info("Usage:");
    logger.info("  quit\n");
    logger.info("Description:");
    logger.info("  Exits the application.\n");
    logger.debug("Information:");
    logger.debug("  This command will ask you if you want to save before quitting.\n");
  }
}
