package fr.bordeaux.Main;

import static org.junit.jupiter.api.Assertions.*;

import fr.bordeaux.config.ConfigManager;
import java.lang.reflect.Constructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OptionsParserTest {

  private ConfigManager config;

  @BeforeEach
  public void setUp() {
    config = ConfigManager.getInstance();
  }

  @Test
  public void testPrivateConstructor() throws Exception {
    Constructor<OptionsParser> constructor = OptionsParser.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    assertNotNull(constructor.newInstance());
  }


  @Test
  public void testHelpShort() {
    assertDoesNotThrow(() -> {
        var method = OptionsParser.class.getDeclaredMethod("printHelp");
        assertNotNull(method);
    });
  }

  @Test
  public void testHelpLong() {
    assertDoesNotThrow(() -> {
        var method = OptionsParser.class.getDeclaredMethod("printHelp");
        assertNotNull(method);
    });
  }

  @Test
  public void testVersionShort() {
    assertDoesNotThrow(() -> OptionsParser.parse(new String[]{"-V"}, config));
  }

  @Test
  public void testVersionLong() {
    assertDoesNotThrow(() -> OptionsParser.parse(new String[]{"--version"}, config));
  }


  @Test
  public void testContestWithFile() {
    assertDoesNotThrow(() ->
        OptionsParser.parse(new String[]{"--contest", "game.txt"}, config));
  }

  @Test
  public void testContestShortWithFile() {
    assertDoesNotThrow(() ->
        OptionsParser.parse(new String[]{"-c", "game.txt"}, config));
  }

  @Test
  public void testContestMissingFile() {
    assertThrows(IllegalArgumentException.class, () ->
        OptionsParser.parse(new String[]{"--contest"}, config));
  }


  @Test
  public void testVerbose() {
    OptionsParser.parse(new String[]{"-v"}, config);
    assertEquals("true", config.getOption("verbose", "false"));
  }

  @Test
  public void testVerboseLong() {
    OptionsParser.parse(new String[]{"--verbose"}, config);
    assertEquals("true", config.getOption("verbose", "false"));
  }


  @Test
  public void testDebug() {
    OptionsParser.parse(new String[]{"-d"}, config);
    assertEquals("true", config.getOption("debug", "false"));
  }

  @Test
  public void testDebugLong() {
    OptionsParser.parse(new String[]{"--debug"}, config);
    assertEquals("true", config.getOption("debug", "false"));
  }


  @Test
  public void testDaemon() {
    OptionsParser.parse(new String[]{"--daemon"}, config);
    assertEquals("true", config.getOption("daemon", "false"));
  }

  @Test
  public void testBlitz() {
    OptionsParser.parse(new String[]{"-b"}, config);
    assertEquals("true", config.getOption("blitz", "false"));
  }

  @Test
  public void testBlitzLong() {
    OptionsParser.parse(new String[]{"--blitz"}, config);
    assertEquals("true", config.getOption("blitz", "false"));
  }


  @Test
  public void testTimeWithBlitzEnabled() {
    OptionsParser.parse(new String[]{"-b", "-t", "10"}, config);
    assertEquals("10", config.getOption("timeout", null));
  }

  @Test
  public void testTimeWithoutBlitz() {
    assertDoesNotThrow(() ->
        OptionsParser.parse(new String[]{"-t", "10"}, config));
  }

  @Test
  public void testTimeMissingValue() {
    assertThrows(IllegalArgumentException.class, () ->
        OptionsParser.parse(new String[]{"-t"}, config));
  }

  @Test
  public void testAiBothPlayers() {
    OptionsParser.parse(new String[]{"-a", "a"}, config);
    assertEquals("true", config.getOption("ai1", "false"));
    assertEquals("true", config.getOption("ai2", "false"));
  }

  @Test
  public void testAiPlayer1ByName() {
    OptionsParser.parse(new String[]{"-a", "blanc"}, config);
    assertEquals("true", config.getOption("ai1", "false"));
  }

  @Test
  public void testAiPlayer1ByIndex() {
    OptionsParser.parse(new String[]{"-a", "1"}, config);
    assertEquals("true", config.getOption("ai1", "false"));
  }

  @Test
  public void testAiDefaultPlayer2() {
    OptionsParser.parse(new String[]{"-a", "other"}, config);
    assertEquals("true", config.getOption("ai2", "false"));
  }

  @Test
  public void testAiNoNextArg() {
    OptionsParser.parse(new String[]{"-a"}, config);
    assertEquals("true", config.getOption("ai2", "false"));
  }

  @Test
  public void testAiNextArgIsFlag() {
    OptionsParser.parse(new String[]{"-a", "-v"}, config);
    assertEquals("true", config.getOption("ai2", "false"));
  }


  @Test
  public void testAiModeAppliedToAi2() {
    OptionsParser.parse(new String[]{"-a", "other", "--ai-mode", "minimax"}, config);
    assertEquals("minimax", config.getOption("ai2.mode", null));
  }

  @Test
  public void testAiModeAppliedToBothWithComma() {
    OptionsParser.parse(new String[]{"-a", "a", "--ai-mode", "minimax,mcts"}, config);
    assertEquals("minimax", config.getOption("ai1.mode", null));
    assertEquals("mcts",    config.getOption("ai2.mode", null));
  }

  @Test
  public void testAiMinimaxDepth() {
    OptionsParser.parse(new String[]{"-a", "a", "--ai-minimax-depth", "4"}, config);
    assertEquals("4", config.getOption("ai1.depth", null));
    assertEquals("4", config.getOption("ai2.depth", null));
  }


  @Test
  public void testAiTime() {
    OptionsParser.parse(new String[]{"-a", "a", "--ai-time", "5"}, config);
    assertEquals("5", config.getOption("ai1.time", null));
    assertEquals("5", config.getOption("ai2.time", null));
  }


  @Test
  public void testAiMinimaxScoring() {
    OptionsParser.parse(new String[]{"-a", "a", "--ai-minimax-scoring", "advanced"}, config);
    assertEquals("advanced", config.getOption("ai1.scoring", null));
    assertEquals("advanced", config.getOption("ai2.scoring", null));
  }


  @Test
  public void testAiMctsSelectionUCT() {
    OptionsParser.parse(new String[]{"-a", "a", "--ai-mcts-selection", "UCT"}, config);
    assertEquals("UCT", config.getOption("ai1.selection", null));
    assertEquals("UCT", config.getOption("ai2.selection", null));
  }

  @Test
  public void testAiMctsSelectionML() {
    OptionsParser.parse(new String[]{"-a", "a", "--ai-mcts-selection", "ML"}, config);
    assertEquals("ML", config.getOption("ai1.selection", null));
  }

  @Test
  public void testAiMctsSelectionWithComma() {
    OptionsParser.parse(new String[]{"-a", "a", "--ai-mcts-selection", "UCT,ML"}, config);
    assertEquals("UCT", config.getOption("ai1.selection", null));
    assertEquals("ML",  config.getOption("ai2.selection", null));
  }

  @Test
  public void testAiMctsSelectionInvalid() {
    assertThrows(IllegalArgumentException.class, () ->
        OptionsParser.parse(new String[]{"-a", "a", "--ai-mcts-selection", "INVALID"}, config));
  }


  @Test
  public void testSBoardSize() {
    OptionsParser.parse(new String[]{"-s", "9"}, config);
    assertEquals("9", config.getOption("board-size", null));
  }

  @Test
  public void testSServerPort() {
    OptionsParser.parse(new String[]{"-s", "1234"}, config);
    assertEquals("true", config.getOption("server", "false"));
    assertEquals("1234", config.getOption("server-port", null));
  }

  @Test
  public void testSMissingArg() {
    assertThrows(IllegalArgumentException.class, () ->
        OptionsParser.parse(new String[]{"-s"}, config));
  }

  @Test
  public void testServerWithPort() {
    OptionsParser.parse(new String[]{"--server", "8080"}, config);
    assertEquals("true", config.getOption("server", "false"));
    assertEquals("8080", config.getOption("server-port", null));
  }

  @Test
  public void testServerMissingPort() {
    assertThrows(IllegalArgumentException.class, () ->
        OptionsParser.parse(new String[]{"--server"}, config));
  }

  @Test
  public void testPlayers() {
    OptionsParser.parse(new String[]{"-p", "4"}, config);
    assertEquals("4", config.getOption("nb-players", null));
  }

  @Test
  public void testPlayersLong() {
    OptionsParser.parse(new String[]{"--players", "2"}, config);
    assertEquals("2", config.getOption("nb-players", null));
  }

  @Test
  public void testPlayersMissingValue() {
    assertThrows(IllegalArgumentException.class, () ->
        OptionsParser.parse(new String[]{"--players"}, config));
  }

  @Test
  public void testWallsPositive() {
    OptionsParser.parse(new String[]{"-w", "10"}, config);
    assertEquals("10", config.getOption("nb-walls", null));
  }

  @Test
  public void testWallsNegative() {
    assertThrows(IllegalArgumentException.class, () ->
        OptionsParser.parse(new String[]{"-w", "-1"}, config));
  }

  @Test
  public void testWallsLong() {
    OptionsParser.parse(new String[]{"--walls", "5"}, config);
    assertEquals("5", config.getOption("nb-walls", null));
  }

  @Test
  public void testWallsMissingValue() {
    assertThrows(IllegalArgumentException.class, () ->
        OptionsParser.parse(new String[]{"--walls"}, config));
  }


  @Test
  public void testSize() {
    OptionsParser.parse(new String[]{"--size", "11"}, config);
    assertEquals("11", config.getOption("board-size", null));
  }

  @Test
  public void testSizeMissingValue() {
    assertThrows(IllegalArgumentException.class, () ->
        OptionsParser.parse(new String[]{"--size"}, config));
  }

  @Test
  public void testGuiFlag() {
    assertDoesNotThrow(() -> OptionsParser.parse(new String[]{"-g"}, config));
  }

  @Test
  public void testGuiLongFlag() {
    assertDoesNotThrow(() -> OptionsParser.parse(new String[]{"--gui"}, config));
  }

  @Test
  public void testCliFlag() {
    assertDoesNotThrow(() -> OptionsParser.parse(new String[]{"--cli"}, config));
  }

  @Test
  public void testNetShortFlag() {
    assertDoesNotThrow(() -> OptionsParser.parse(new String[]{"-n"}, config));
  }

  @Test
  public void testNetLongFlag() {
    assertDoesNotThrow(() -> OptionsParser.parse(new String[]{"--net"}, config));
  }


  @Test
  public void testInvalidArgument() {
    assertThrows(IllegalArgumentException.class, () ->
        OptionsParser.parse(new String[]{"--unknown"}, config));
  }
}