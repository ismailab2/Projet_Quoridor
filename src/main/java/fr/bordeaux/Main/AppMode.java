package fr.bordeaux.Main;

/**
 * Represents the different execution modes of the application.
 *
 * <p>The application can be launched in multiple modes depending on user input or command-line
 * arguments:
 *
 * <ul>
 *   <li>{@link #GUI} - Launches the graphical user interface
 *   <li>{@link #CLI} - Launches the command-line interface (default mode)
 *   <li>{@link #NETWORK} - Launches the network mode (client/server commands)
 * </ul>
 */
public enum AppMode {

  /** Graphical User Interface mode */
  GUI,

  /** Command Line Interface mode */
  CLI,

  /** Network mode (server/client interactions) */
  NETWORK,

}
