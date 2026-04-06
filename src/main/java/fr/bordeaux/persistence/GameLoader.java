package fr.bordeaux.persistence;

import fr.bordeaux.ai.base.*;
import fr.bordeaux.ai.mcts.MctsPlayer;
import fr.bordeaux.ai.minimax.*;
import fr.bordeaux.ai.random.RandomPlayer;
import fr.bordeaux.core.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Utility class used to load a game state from a save file. */
public final class GameLoader {
  /** The size of the board (9x9). */
  private static int size = 9;

  /** The line index where the board section starts in the save file. */
  private static final int BOARD_START = 3;

  /** Default board size. */
  private static final int BOARD_SIZE_DEFAULT = 9;

  /** Default timeout in minutes. */
  private static final int TIMEOUT_DEFAULT = 30;

  /** Default MCTS reflexion time in milliseconds. */
  private static final long MCTS_REFLEXION_DEFAULT = 2000L;

  /** Private constructor to prevent instantiation. */
  private GameLoader() {}

  /**
   * Loads a saved game from a file.
   *
   * @param engine the engine for configuration
   * @param filename path of the save file
   * @return the reconstructed game state
   * @throws IOException on I/O error
   * @throws GameLoadException on invalid format
   */
  public static GameState loadGame(final GameEngine engine, final String filename)
      throws IOException, GameLoadException {
    final List<String> lines = GameLoaderComment.readCleanLines(filename);
    validateSettingsHeader(lines);
    final AllPlayerAndLine resultBundle = readAllPlayer(lines);
    final List<Player> players = resultBundle.players;
    final int currentLine = resultBundle.nextLine;
    validateHeader(lines, currentLine);
    size = readBoardSize(lines, currentLine);
    final Board board = new Board(size);
    readBoard(lines, board, players, currentLine);
    readRemainingWalls(lines, players, currentLine);

    final List<Move> history = new ArrayList<>();
    readHistory(lines, history);
    final int playerIndex = readCurrentPlayer(lines, currentLine);
    final GameState gameState = new GameState(board, players, playerIndex);
    applyMode(engine, gameState, lines);
    gameState.loadHistoryWithoutApplying(history);
    return gameState;
  }

  // Checks that the first line is "[game]".
  // Throws GameLoadException with error type if not.
  private static void validateHeader(final List<String> lines, final int currentLine)
      throws GameLoadException {
    if (!"[game]".equals(lines.get(currentLine))) {
      throw new GameLoadException(
          "ERREUR_ENTETE", 1, "Expected [game] but found : " + lines.get(currentLine));
    }
  }

  /**
   * Checks that the save file starts with the settings header.
   *
   * @param lines The list of strings representing the lines of the save file.
   * @throws GameLoadException if the header is missing or incorrect.
   */
  private static void validateSettingsHeader(final List<String> lines) throws GameLoadException {
    if (!"[settings]".equals(lines.get(0))) {
      throw new GameLoadException(
          "ERREUR_ENTETE", 1, "Expected [settings] but found : " + lines.get(0));
    }
  }

  // Reads the current player index from line 2
  // Example : "2  # Current player color" , returns 1
  private static int readCurrentPlayer(final List<String> lines, final int currentLine) {
    return Integer.parseInt(lines.get(currentLine + 1).trim().split("#")[0].trim()) - 1;
  }

  private static int readBoardSize(final List<String> lines, final int currentLine) {
    return Integer.parseInt(lines.get(currentLine + 2).trim().split("#")[0].trim());
  }

  // Reads board, positions players and places walls.
  private static void readBoard(
      final List<String> lines,
      final Board board,
      final List<Player> players,
      final int currentLine) {
    for (int row = 0; row < size; row++) {
      readCellLine(lines.get(BOARD_START + currentLine + row * 2), row, board, players);
      if (row < size - 1) {
        readHorizontalWalls(lines.get(BOARD_START + currentLine + row * 2 + 1), row, board);
      }
    }
  }

  // Reads a cell line, positions players and vertical walls.
  private static void readCellLine(
      final String line, final int row, final Board board, final List<Player> players) {
    for (int col = 0; col < size; col++) {
      // Each cell character is at index col*2 in the line
      final char cell = line.charAt(col * 2);
      if (cell == '1') {
        players.get(0).setPosition(new Position(row, col));
      } else if (cell == '2') {
        players.get(1).setPosition(new Position(row, col));
      }
      if (col < size - 1 && line.charAt(col * 2 + 1) == '|') {
        board.getGraph().removeEdge(board.getCellId(row, col), board.getCellId(row, col + 1));
      }
    }
  }

  // Reads and places horizontal walls.
  private static void readHorizontalWalls(final String line, final int row, final Board board) {
    for (int col = 0; col < size; col++) {
      if (line.charAt(col * 2) == '_') {
        board.getGraph().removeEdge(board.getCellId(row, col), board.getCellId(row + 1, col));
      }
    }
  }

  // Restores the correct number of walls for each player.
  private static void readRemainingWalls(
      final List<String> lines, final List<Player> players, final int currentLine) {
    final String wallLine = lines.get(BOARD_START + currentLine + size * 2 - 1);
    final String[] parts = wallLine.replace("walls:", "").trim().split(" ");
    for (int index = 0; index < players.size(); index++) {
      final int walls = Integer.parseInt(parts[index]);
      final Player player = players.get(index);
      while (player.getRemainingWalls() > walls) {
        player.useWall();
      }
    }
  }

  /**
   * Record bundling a parsed player and next line index.
   *
   * @param player The parsed player instance.
   * @param nextLine The index of the next line in the file.
   */
  private record PlayerAndLine(Player player, int nextLine) {}

  /**
   * Record bundling all parsed players and next line index.
   *
   * @param players The list of all parsed players.
   * @param nextLine The index of the line where player and settings parsing stopped.
   */
  private record AllPlayerAndLine(List<Player> players, int nextLine) {}

  /**
   * Reads all players (Human and AI) from the save file.
   *
   * @param lines The list of strings representing the lines of the save file.
   * @return AllPlayerAndLine with players and next line index.
   */
  private static AllPlayerAndLine readAllPlayer(final List<String> lines) {
    final List<Player> lPlayers = new ArrayList<>();
    // Find the start of the [game] section first to know where players end
    int gameSectionIndex = -1;
    for (int i = 0; i < lines.size(); i++) {
      if ("[game]".equals(lines.get(i))) {
        gameSectionIndex = i;
        break;
      }
    }

    // Find the first player line after [settings]
    int currentLine = 1;
    while (currentLine < lines.size()
        && currentLine < gameSectionIndex
        && !lines.get(currentLine).startsWith("player-")
        && !lines.get(currentLine).startsWith("ai-")) {
      currentLine++;
    }

    while (currentLine < lines.size() && currentLine < gameSectionIndex) {
      final PlayerAndLine res = readPlayer(lines, currentLine);
      lPlayers.add(res.player);
      currentLine = res.nextLine;
    }
    return new AllPlayerAndLine(lPlayers, gameSectionIndex);
  }

  /**
   * Reads player info and delegates AI configuration.
   *
   * @param lines The list of strings representing the lines of the save file.
   * @param currentLine The index of the line where the player's information begins.
   * @return PlayerAndLine with player and updated line index.
   */
  private static PlayerAndLine readPlayer(final List<String> lines, final int currentLine) {
    final String playerLine = lines.get(currentLine);
    final String typePart = playerLine.split("=")[0].split("-")[0];
    final String playerName = playerLine.split("=")[1];
    int myCurrentLine = currentLine + 1;

    final String colorLine = lines.get(myCurrentLine);
    final String colorValue = colorLine.split("=")[1].trim().toUpperCase(Locale.ENGLISH);
    final Color playerColor = Color.valueOf(colorValue);
    myCurrentLine++;

    final String posLine = lines.get(myCurrentLine);
    final Position playerPosition = readPlayerStartPosition(posLine);
    myCurrentLine++;

    final String timeLine = lines.get(myCurrentLine);
    final long remainingTime = Long.parseLong(timeLine.split("=")[1]);

    final PlayerAndLine result;
    if ("ai".equals(typePart)) {
      result =
          readAiPlayer(
              lines, myCurrentLine, playerName, playerPosition, playerColor, remainingTime);
    } else {
      final Player human = new HumanPlayer(playerName, playerPosition, playerColor);
      human.setRemainingTime(remainingTime);
      result = new PlayerAndLine(human, myCurrentLine + 1);
    }
    return result;
  }

  /**
   * Parses starting position coordinates from string.
   *
   * @param p The string containing the position data (e.g., "key=Position: 4, 0").
   * @return A {@link Position} object representing the parsed coordinates.
   */
  private static Position readPlayerStartPosition(final String positionString) {
    final String posValue = positionString.split("=")[1];
    final String coordPart = posValue.split(":")[1];
    final String[] coords = coordPart.split(",");
    final int posX = Integer.parseInt(coords[0].trim());
    final int posY = Integer.parseInt(coords[1].trim());
    return new Position(posX, posY);
  }

  /**
   * Reads an AI player and its settings.
   *
   * @return PlayerAndLine with configured AI.
   */
  private static PlayerAndLine readAiPlayer(
      final List<String> lines,
      final int currentLine,
      final String name,
      final Position pos,
      final Color color,
      final long time) {
    final Map<String, String> settings = parseAiSettings(lines, currentLine);
    final int nextLine = currentLine + settings.size() + 1;

    final String mode = settings.get("mode");
    final Player aiPlayer;
    switch (mode) {
      case "minimax" -> aiPlayer = createMinimax(name, pos, color, settings, false);
      case "minimax-iterative-deepening" ->
          aiPlayer = createMinimax(name, pos, color, settings, true);
      case "mcts" -> aiPlayer = createMcts(name, pos, color, settings);
      case "random" -> aiPlayer = new RandomPlayer(name, pos, color);
      default -> throw new IllegalArgumentException("Unknown AI Type : " + mode);
    }
    aiPlayer.setRemainingTime(time);
    return new PlayerAndLine(aiPlayer, nextLine);
  }

  private static Map<String, String> parseAiSettings(
      final List<String> lines, final int startLine) {
    final Map<String, String> settings = new ConcurrentHashMap<>();
    int lineRef = startLine + 1;
    final int targetIndex = Integer.parseInt(lines.get(lineRef).split("-")[1]);
    while (checkSameIndex(lines, lineRef, targetIndex)) {
      final String[] split = lines.get(lineRef).split("=");
      final String[] param = split[0].split("-");
      settings.put(param[param.length - 1], split[1]);
      lineRef++;
    }
    return settings;
  }

  private static Player createMinimax(
      final String name,
      final Position pos,
      final Color color,
      final Map<String, String> settings,
      final boolean isIterative) {
    final String heuristic = settings.get("heuristic");
    final int reflexion = Integer.parseInt(settings.get("reflexion"));
    final MinimaxBuilder builder =
        MinimaxPlayer.builder(name, pos, color).withHeuristic(heuristic).withReflexion(reflexion);

    if (isIterative) {
      builder.withIterativeDeepening(true);
    } else {
      builder.withDepth(Integer.parseInt(settings.get("depth")));
    }
    return builder.build();
  }

  private static Player createMcts(
      final String name,
      final Position pos,
      final Color color,
      final Map<String, String> settings) {
    final long reflexion =
        settings.containsKey("reflexion")
            ? Long.parseLong(settings.get("reflexion"))
            : MCTS_REFLEXION_DEFAULT;
    final boolean useNN = "ML".equals(settings.get("selection"));
    try {
      return new MctsPlayer(name, pos, reflexion, useNN, color);
    } catch (final IOException exception) {
      throw new IllegalArgumentException("Impossible de charger le modèle pour MCTS", exception);
    }
  }

  /**
   * Checks if current line player matches target index.
   *
   * @param lines The list of strings representing the lines of the save file.
   * @param currentLine The index of the line to check.
   * @param index The index of a player
   * @return {@code true} if the line has the same index player, {@code false} otherwise.
   */
  private static boolean checkSameIndex(
      final List<String> lines, final int myLineRef, final int targetIndex) {
    boolean result = false;
    if (lines.get(myLineRef).contains("-")) {
      result = Integer.parseInt(lines.get(myLineRef).split("-")[1]) == targetIndex;
    }
    return result;
  }

  /**
   * Applies game configuration (verbose, blitz, etc.).
   *
   * @throws IllegalArgumentException if player count mismatch.
   */
  private static void applyMode(
      final GameEngine engine, final GameState gameState, final List<String> lines) {
    int nbPlayers = 0;
    for (final String line : lines) {
      if ("[game]".equals(line)) {
        break;
      }
      if (line.contains("=")) {
        final String[] parts = line.split("=");
        final String key = parts[0].trim();
        final String value = parts[1].trim();
        nbPlayers = handleModeSetting(engine, key, value, nbPlayers);
      }
    }

    validatePlayerCount(nbPlayers, gameState.getPlayers().size());
  }

  private static int handleModeSetting(
      final GameEngine engine,
      final String key,
      final String value,
      final int currentPlayersCount) {
    int playersCount = currentPlayersCount;
    switch (key) {
      case "verbose" -> engine.setVerbose(Boolean.parseBoolean(value));
      case "blitz" -> engine.setBlitz(Boolean.parseBoolean(value));
      case "constest" -> engine.setContest(Boolean.parseBoolean(value));
      case "debug" -> engine.setDebug(Boolean.parseBoolean(value));
      case "nb-players" -> playersCount = Integer.parseInt(value);
      default -> {}
    }
    return playersCount;
  }

  private static void validatePlayerCount(final int expected, final int actual) {
    if (expected > 0 && actual != expected) {
      throw new IllegalArgumentException(
          "The number of players read from the file ("
              + expected
              + ") and the actual players in the game ("
              + actual
              + ") are different");
    }
  }

  private static Position coordToPos(final String coord) {
    final int posY = coord.charAt(0) - 'a';
    final int posX = Integer.parseInt(coord.substring(1)) - 1;
    return new Position(posX, posY);
  }

  private static void readHistory(final List<String> lines, final List<Move> history) {
    boolean inHistory = false;
    for (final String line : lines) {
      final String trimmed = line.trim();
      if ("[History]".equals(trimmed)) {
        inHistory = true;
        continue;
      }
      if (!inHistory) {
        continue;
      }
      if (trimmed.startsWith("[")) {
        break;
      }
      if (!trimmed.isEmpty()) {
        parseHistoryLine(trimmed, history);
      }
    }
  }

  private static void parseHistoryLine(final String line, final List<Move> history) {
    final String[] parts = line.split(";");
    for (final String part : parts) {
      final String moveStr = part.trim();
      if (!moveStr.isEmpty()) {
        final String[] tokens = moveStr.split(" ");
        if (tokens.length >= 2) {
          history.add(parseMove(tokens[1]));
        }
      }
    }
  }

  private static Move parseMove(final String desc) {
    final Move result;
    if (desc.contains("-")) {
      final String[] coords = desc.split("-");
      result = Move.pawn(coordToPos(coords[0]), coordToPos(coords[1]));
    } else {
      final String posStr = desc.substring(0, desc.length() - 1);
      final char orientChar = desc.charAt(desc.length() - 1);
      final Orientation orient =
          (orientChar == 'h') ? Orientation.HORIZONTAL : Orientation.VERTICAL;
      result = Move.wall(coordToPos(posStr), orient);
    }
    return result;
  }
}
