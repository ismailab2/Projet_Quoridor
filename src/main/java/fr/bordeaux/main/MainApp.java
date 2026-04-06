package fr.bordeaux.main;

import fr.bordeaux.config.ConfigManager;

/** Main entry point. Loads config, parses options, and resolves launch mode. */
@SuppressWarnings("PMD.SystemPrintln")
public final class MainApp {

  /** Default constructor. Constructs a new MainApp instance. */
  private MainApp() {}

  /**
   * Main application entry.
   *
   * @param args command-line args
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
