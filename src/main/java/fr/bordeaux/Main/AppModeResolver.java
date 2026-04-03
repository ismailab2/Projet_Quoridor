package fr.bordeaux.Main;

/**
 * Utility class used to resolve the application execution mode from command-line arguments.
 *
 * <p>This class parses the provided arguments and determines which {@link AppMode} should be used
 * to launch the application.
 *
 * <p>Supported options:
 *
 * <ul>
 *   <li><code>-g</code>, <code>--gui</code> → {@link AppMode#GUI}
 *   <li><code>-n</code>, <code>--net</code> → {@link AppMode#NETWORK}
 *   <li><code>-c</code>, <code>--cli</code> → {@link AppMode#CLI}
 * </ul>
 *
 * <p>If no mode is specified, the default mode is {@link AppMode#CLI}.
 *
 * <p>This class cannot be instantiated.
 */
public final class AppModeResolver {

  /** Private constructor to prevent instantiation. */
  private AppModeResolver() {}

  /**
   * Resolves the application mode from command-line arguments.
   *
   * <p>The method scans the arguments in order and returns the first matching mode flag found.
   *
   * @param args command-line arguments passed to the application
   * @return the resolved {@link AppMode}, or {@link AppMode#CLI} if none is specified
   */
  public static AppMode resolve(String[] args) {
    for (String arg : args) {
      switch (arg) {
        case "--gui":
        case "-g":
          return AppMode.GUI;

        case "--net":
        case "-n":
          return AppMode.NETWORK;

        case "--server":
        case "-s":
          return AppMode.NETWORK;

        case "--cli":
        case "-c":
          return AppMode.CLI;
      }
    }

    return AppMode.CLI;
  }
}
