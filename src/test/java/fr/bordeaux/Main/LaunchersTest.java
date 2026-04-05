package fr.bordeaux.Main;

import static org.junit.jupiter.api.Assertions.*;

import fr.bordeaux.config.ConfigManager;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import fr.bordeaux.core.*;

public class LaunchersTest {

  private ConfigManager config;

  @BeforeEach
  public void setUp() {
    config = ConfigManager.getInstance();
  }

  @Test
  public void testGuiLauncherConstructor() {
    assertNotNull(new GuiLauncher());
  }

  @Test
  public void testGuiLauncherBuildEngine() {
    
    GuiLauncher launcher = new GuiLauncher();
    assertDoesNotThrow(() -> {
      var engine = launcher.buildEngine();
      assertNotNull(engine);
    });
  }

  @Test
  public void testCliLauncherConstructor() {
    assertNotNull(new CliLauncher());
  }

  @Test
  public void testContestLauncherConstructor() throws Exception {
    Constructor<ContestLauncher> c = ContestLauncher.class.getDeclaredConstructor();
    c.setAccessible(true);
    assertNotNull(c.newInstance());
  }

  @Test
  public void testContestLaunchInvalidFileContent(@TempDir Path tempDir) throws IOException {
      Path badFile = tempDir.resolve("bad_save.txt");
      Files.writeString(badFile, "this is not a valid save file\n");
      config.enableContest(badFile.toString());
      ContestLauncher launcher = newContestLauncher();
      assertDoesNotThrow(() -> launcher.launch(new String[]{}, config));
  }

  @Test
  public void testContestLaunchInvalidFile(@TempDir Path tempDir) throws IOException {
    Path badFile = tempDir.resolve("bad_save.txt");
    Files.writeString(badFile, "this is not a valid save file\n");
    config.enableContest(badFile.toString());
    ContestLauncher launcher = newContestLauncher();
    assertDoesNotThrow(() -> launcher.launch(new String[]{}, config));
  }

  @Test
  public void testMoveToStringPawn() throws Exception {
    
    Method m = ContestLauncher.class.getDeclaredMethod("moveToString", 
        Move.class);
    m.setAccessible(true);

    Position from = new Position(0, 4);
    Position to   = new Position(1, 4);
    Move pawnMove = Move.pawn(from, to);

    String result = (String) m.invoke(null, pawnMove);
    assertEquals("a4-b4", result);
  }

  @Test
  public void testMoveToStringWallHorizontal() throws Exception {
    Method m = ContestLauncher.class.getDeclaredMethod("moveToString",
        Move.class);
    m.setAccessible(true);

    Position pos  = new Position(3, 2);
    Move wallMove = Move.wall(pos,
        Orientation.HORIZONTAL);

    String result = (String) m.invoke(null, wallMove);
    assertEquals("d2h", result);
  }

  @Test
  public void testMoveToStringWallVertical() throws Exception {
    Method m = ContestLauncher.class.getDeclaredMethod("moveToString",
        Move.class);
    m.setAccessible(true);

    Position pos  = new Position(5, 1);
    Move wallMove = Move.wall(pos,
        Orientation.VERTICAL);

    String result = (String) m.invoke(null, wallMove);
    assertEquals("f1v", result);
  }

  @Test
  public void testPosToString() throws Exception {
    Method m = ContestLauncher.class.getDeclaredMethod("posToString",
        Position.class);
    m.setAccessible(true);

    assertEquals("a0", m.invoke(null, new Position(0, 0)));
    assertEquals("e4", m.invoke(null, new Position(4, 4)));
    assertEquals("i8", m.invoke(null, new Position(8, 8)));
  }

  @Test
  public void testCreateDummyEngine() throws Exception {
    Method m = ContestLauncher.class.getDeclaredMethod("createDummyEngine",
        ConfigManager.class);
    m.setAccessible(true);

    Object engine = m.invoke(null, config);
    assertNotNull(engine);
    assertInstanceOf(GameEngine.class, engine);
  }


  @Test
  public void testNetworkLauncherConstructor() {
    assertNotNull(new NetworkLauncher());
  }


  @Test
  public void testNetworkLauncherQuitImmediately() throws InterruptedException {
      NetworkLauncher launcher = new NetworkLauncher();
      java.io.InputStream original = System.in;
      System.setIn(new java.io.ByteArrayInputStream("quit\n".getBytes()));

      Thread t = new Thread(() -> launcher.launch(new String[]{}, config));
      t.start();
      t.join(3000);

      System.setIn(original);
      assertFalse(t.isAlive(), "Le thread devrait s'être terminé après 'quit'");
  }


  private ContestLauncher newContestLauncher() {
    try {
      Constructor<ContestLauncher> c = ContestLauncher.class.getDeclaredConstructor();
      c.setAccessible(true);
      return c.newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}