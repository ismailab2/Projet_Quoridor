package fr.bordeaux.main;

import fr.bordeaux.config.ConfigManager;

/**
 * This class is the main entry point of the application. It performs initial configuration loading,
 * parses command-line options, determines the launch mode (CLI, GUI, NETWORK), and delegates
 * execution to the appropriate {@link AppLauncher}.
 *
 * <p>Usage example:
 *
 * <pre>
 * java -jar quoridor.jar --cli
 * java -jar quoridor.jar --gui
 * java -jar quoridor.jar --net
 * </pre>
 */
@SuppressWarnings("PMD.SystemPrintln")
public final class MainApp {

  /** Default constructor. Constructs a new MainApp instance. */
  private MainApp() {}

  /**
   * This method performs the following steps:
   *
   * <ol>
   *   <li>Loads the application configuration via {@link ConfigManager}.
   *   <li>Parses command-line arguments using {@link OptionsParser}.
   *   <li>Resolves the desired launch mode via {@link AppModeResolver}.
   *   <li>Creates the corresponding {@link AppLauncher} via {@link AppLauncherFactory}.
   *   <li>Launches the application in the selected mode.
   * </ol>
   *
   * @param args Command-line arguments passed to the application
   */
  public static void main(final String[] args) {
    final ConfigManager config = ConfigManager.getInstance();
    config.load();

    try {
      OptionsParser.parse(args, config);

      if (config.isContestMode()) {
        new ContestLauncher().launch(args, config);
      }
      final AppMode mode = AppModeResolver.resolve(args);

      final AppLauncher launcher = AppLauncherFactory.create(mode);
      launcher.launch(args, config);

    } catch (IllegalArgumentException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
  }
}
