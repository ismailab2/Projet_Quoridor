package fr.bordeaux.main;

/** Factory for creating the appropriate {@link AppLauncher} based on the {@link AppMode}. */
public final class AppLauncherFactory {

  /** Default constructor. Constructs a new AppLauncherFactory instance. */
  private AppLauncherFactory() {}

  /**
   * Creates an AppLauncher corresponding to the given mode.
   *
   * @param mode the selected application mode
   * @return the appropriate launcher implementation
   */
  public static AppLauncher create(final AppMode mode) {
    if (mode == AppMode.GUI) {
      return new GuiLauncher();
    }
    if (mode == AppMode.NETWORK) {
      return new NetworkLauncher();
    }
    return new CliLauncher();
  }
}
