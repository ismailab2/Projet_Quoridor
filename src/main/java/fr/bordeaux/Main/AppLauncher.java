package fr.bordeaux.Main;

import fr.bordeaux.config.ConfigManager;

/**
 * Defines the contract for launching an application mode.
 *
 * <p>This interface is implemented by different launchers (e.g., CLI, GUI, Network) to start the
 * application according to the selected execution mode.
 *
 * <p>Each implementation is responsible for initializing and running its specific environment.
 */
public interface AppLauncher {

  /**
   * Launches the application with the given arguments and configuration.
   *
   * @param args command-line arguments passed to the application
   * @param config global configuration manager instance
   */
  void launch(String[] args, ConfigManager config);
}
