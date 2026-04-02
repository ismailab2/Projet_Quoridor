package fr.bordeaux.Main;

/**
 * Factory class responsible for creating the appropriate {@link AppLauncher} based on the selected
 * {@link AppMode}.
 *
 * <p>This class centralizes the instantiation logic of application launchers (CLI, GUI, Network),
 * allowing the main application to remain decoupled from specific implementations.
 *
 * <p>It follows the Factory design pattern.
 */
public class AppLauncherFactory {

  /** Default constructor. Constructs a new AppLauncherFactory instance. */
  public AppLauncherFactory() {}

  /**
   * Creates an {@link AppLauncher} corresponding to the given mode.
   *
   * @param mode the selected application mode
   * @return the appropriate launcher implementation:
   *     <ul>
   *       <li>{@link GuiLauncher} for GUI mode
   *       <li>{@link NetworkLauncher} for network mode
   *       <li>{@link CliLauncher} for CLI mode (default)
   *     </ul>
   */
  public static AppLauncher create(AppMode mode) {
    if (mode == AppMode.GUI) {
      return new GuiLauncher();
    }
    if (mode == AppMode.NETWORK) {
      return new NetworkLauncher();
    }
    return new CliLauncher();
  }
}
