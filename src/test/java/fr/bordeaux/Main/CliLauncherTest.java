package fr.bordeaux.Main;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Unit tests for CliLauncher. */
public class CliLauncherTest {

  @Test
  public void testCliLauncherConstructor() {
    assertNotNull(new CliLauncher());
  }
}
