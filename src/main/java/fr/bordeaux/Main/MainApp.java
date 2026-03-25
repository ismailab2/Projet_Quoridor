package fr.bordeaux.Main;

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
public final class MainApp {

    /**
     * Default constructor.
     * Constructs a new MainApp instance.
     */
    public MainApp() {
    }


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
  public static void main(String[] args) {
    ConfigManager config = ConfigManager.getInstance();
    config.load();

    try {
      OptionsParser.parse(args, config);
      AppMode mode = AppModeResolver.resolve(args);

      AppLauncher launcher = AppLauncherFactory.create(mode);
      launcher.launch(args, config);

    } catch (IllegalArgumentException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
  }
}
