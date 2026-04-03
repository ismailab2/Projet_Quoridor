package fr.bordeaux.Main;

import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.gui.QuoridorApp;

/**
 * This launcher starts the Quoridor graphical interface using the QuoridorApp main class. It
 * delegates all GUI initialization and user interaction to the QuoridorApp class.
 */
public class GuiLauncher implements AppLauncher {

  /** GuiLaucher */
  public GuiLauncher() {}

  /**
   * Delegates execution to {@link fr.bordeaux.gui.QuoridorApp#main(String[])}. Any GUI
   * initialization, window creation, and event handling is managed by QuoridorApp.
   *
   * @param args command-line arguments passed to the GUI
   * @param config configuration manager instance for loading settings (currently unused)
   */
  @Override
  public void launch(String[] args, ConfigManager config) {
    QuoridorApp.main(args);
  }
}
