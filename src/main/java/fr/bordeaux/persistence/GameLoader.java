package fr.bordeaux.persistence;

import fr.bordeaux.ai.MctsPlayer;
import fr.bordeaux.ai.MinimaxPlayer;
import fr.bordeaux.ai.RandomPlayer;
import fr.bordeaux.core.*;
import java.io.*;
import java.util.*;

/** Utility class used to load a game state from a save file. */
public final class GameLoader {
  // Board size : always 9x9
  private static int SIZE = 9;

  private static final int BOARD_START = 3;

  /** Private constructor to prevent instantiation. */
  private GameLoader() {}

  /**
   * Loads a saved game from a file and rebuilds the corresponding game state.
   *
   * @param engine the engine used to apply loaded configuration
   * @param filename path of the save file to load
   * @return the reconstructed game state
   * @throws IOException if an input/output error occurs while reading the file
   * @throws GameLoadException if the file content is invalid or inconsistent
   */
  public static GameState loadGame(GameEngine engine, String filename)
      throws IOException, GameLoadException {
    List<String> lines = GameLoaderComment.readCleanLines(filename);
    validateSettingsHeader(lines); // checks [settings] header
    AllPlayerAndLine allp_l = readAllPlayer(lines); // get players and the last line read
    List<Player> players = allp_l.players; // creates all players
    int currentLine = allp_l.nextLine; // Line after settings
    validateHeader(lines, currentLine); // checks [game] header
    SIZE = readBoardSize(lines, currentLine);
    Board board = new Board(SIZE); // creates empty 9x9 board
    readBoard(lines, board, players, currentLine); // places walls and players
    readRemainingWalls(lines, players, currentLine); // restores wall counts

    List<Move> history = new ArrayList<>();
    readHistory(lines, history);
    GameState gameState = new GameState(board, players, readCurrentPlayer(lines, currentLine));
    applyMode(engine, gameState, lines);
    gameState.loadHistoryWithoutApplying(history);
    return gameState;
  }

  // Checks that the first line is "[game]"
  // If not, throws GameLoadException with error type and line number
  private static void validateHeader(List<String> lines, int currentLine) throws GameLoadException {
    if (!lines.get(currentLine).equals("[game]"))
      throw new GameLoadException(
          "ERREUR_ENTETE", 1, "Expected [game] but found : " + lines.get(currentLine));
  }

  /**
   * Checks that the first line of the save file contains the expected settings header.
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
  private static int readCurrentPlayer(List<String> lines, int currentLine) {
    return Integer.parseInt(lines.get(currentLine + 1).trim().split("#")[0].trim()) - 1;
  }

  private static int readBoardSize(List<String> lines, int currentLine) {
    return Integer.parseInt(lines.get(currentLine + 2).trim().split("#")[0].trim());
  }

  // Reads the full board, repositions players and places all walls
  private static void readBoard(
      List<String> lines, Board board, List<Player> players, int currentLine) {
    for (int row = 0; row < SIZE; row++) {
      readCellLine(lines.get(BOARD_START + currentLine + row * 2), row, board, players);
      if (row < SIZE - 1)
        readHorizontalWalls(lines.get(BOARD_START + currentLine + row * 2 + 1), row, board);
    }
  }

  // Reads one cell line : repositions players and places vertical walls
  private static void readCellLine(String line, int row, Board board, List<Player> players) {
    for (int col = 0; col < SIZE; col++) {
      // Each cell character is at index col*2 in the line
      char cell = line.charAt(col * 2);
      if (cell == '1') players.get(0).setPosition(new Position(row, col));
      else if (cell == '2') players.get(1).setPosition(new Position(row, col));
      if (col < SIZE - 1 && line.charAt(col * 2 + 1) == '|')
        board.getGraph().removeEdge(board.id(row, col), board.id(row, col + 1));
    }
  }

  // Reads one horizontal wall line and places horizontal walls on the board
  private static void readHorizontalWalls(String line, int row, Board board) {
    for (int col = 0; col < SIZE; col++)
      if (line.charAt(col * 2) == '_')
        board.getGraph().removeEdge(board.id(row, col), board.id(row + 1, col));
  }

  // Reads "walls: 10 10" and restores the correct number of walls for each player
  private static void readRemainingWalls(
      List<String> lines, List<Player> players, int currentLine) {
    String[] parts =
        lines.get(BOARD_START + currentLine + SIZE * 2 - 1).replace("walls:", "").trim().split(" ");
    for (int i = 0; i < players.size(); i++) {
      int walls = Integer.parseInt(parts[i]);
      // Call useWall() until the player has the correct number of walls
      while (players.get(i).getRemainingWalls() > walls) players.get(i).useWall();
    }
  }

  /**
   * Internal record used to bundle a parsed player and the index of the next line to read.
   *
   * @param player The parsed player instance.
   * @param nextLine The index of the next line in the file.
   */
  private record PlayerAndLine(Player player, int nextLine) {}

  /**
   * Internal record used to bundle the list of all parsed players and the index of the next line.
   *
   * @param players The list of all parsed players.
   * @param nextLine The index of the line where player and settings parsing stopped.
   */
  private record AllPlayerAndLine(List<Player> players, int nextLine) {}

  /**
   * Reads and parses all players (Human and AI) from the save file. Stops reading when it
   * encounters the "[game]" section marker.
   *
   * @param lines The list of strings representing the lines of the save file.
   * @return An {@link AllPlayerAndLine} object containing the list of players and the next line
   *     index.
   */
  private static AllPlayerAndLine readAllPlayer(final List<String> lines) {
    List<Player> lPlayers = new ArrayList<>();
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
   * Reads a single player's basic information(type, name, starting position) and delegates to
   * specific methods for AI configuration if necessary.
   *
   * @param lines The list of strings representing the lines of the save file.
   * @param currentLine The index of the line where the player's information begins.
   * @return A {@link PlayerAndLine} record containing the instantiated player and the updated line
   *     index.
   */
  private static PlayerAndLine readPlayer(List<String> lines, int currentLine) {

    String player = lines.get(currentLine);
    final String pType = player.split("=")[0].split("-")[0];
    final String pName = player.split("=")[1];
    int myCurrentLine = currentLine + 1;

    String line = lines.get(myCurrentLine);
    final Color pColor = Color.valueOf(line.split("=")[1].trim().toUpperCase());
    myCurrentLine++;

    player = lines.get(myCurrentLine);
    final Position pPostion = readPlayerStartPosition(player);
    myCurrentLine += 1;
    final long remainingTime = Long.parseLong(lines.get(myCurrentLine).split("=")[1]);
    PlayerAndLine result = null;
    if (pType.equals("ai")) {
      result = readAiPlayer(lines, myCurrentLine, pName, pPostion, pColor);

    } else {
      result = new PlayerAndLine(new HumanPlayer(pName, pPostion, pColor), myCurrentLine + 1);
    }
    result.player.setRemainingTime(remainingTime);
    return result;
  }

  /**
   * Parses the starting position coordinates from a formatted string.
   *
   * @param p The string containing the position data (e.g., "key=Position: 4, 0").
   * @return A {@link Position} object representing the parsed coordinates.
   */
  private static Position readPlayerStartPosition(final String p) {
    final String pStartPos = p.split("=")[1];
    final String pStartPosCoord = pStartPos.split(":")[1];
    final int x =
        Integer.parseInt(
            pStartPosCoord.split(",")[0].replaceAll(
                " ", "")); // Spaces must be removed because ParseInt does not handle them.
    final int y = Integer.parseInt(pStartPosCoord.split(",")[1].replaceAll(" ", ""));
    return new Position(x, y);
  }

  /**
   * @param lines The list of strings representing the lines of the save file.
   * @param currentLine The current line index after reading basic player info.
   * @param pName The parsed name of the AI player.
   * @param pPostion The parsed starting position of the AI.
   * @return A {@link PlayerAndLine} record containing the fully configured AI player
   * @throws IllegalArgumentException if the AI mode specified in the file is unknown.
   */
  private static PlayerAndLine readAiPlayer(
      final List<String> lines,
      int currentLine,
      final String pName,
      final Position pPostion,
      final Color pColor) {
    Map<String, String> aiSettings = new HashMap<>();
    int myCurrentLine = currentLine + 1;
    int index = Integer.parseInt(lines.get(myCurrentLine).split("-")[1]);
    while (checkSameIndex(lines, myCurrentLine, index)) {
      final String[] ls = lines.get(myCurrentLine).split("=");
      final String[] aiParameter = ls[0].split("-");
      aiSettings.put(aiParameter[aiParameter.length - 1], ls[1]);
      myCurrentLine++;
    }

    switch (aiSettings.get("mode")) {
      case "minimax":
        {
          final String heuristicName = aiSettings.get("heuristic");
          final int depth = Integer.parseInt(aiSettings.get("depth"));
          final int reflexionTime = Integer.parseInt(aiSettings.get("reflexion"));
          return new PlayerAndLine(
              MinimaxPlayer.builder(pName, pPostion, pColor)
                  .withHeuristic(heuristicName)
                  .withDepth(depth)
                  .withReflexion(reflexionTime)
                  .build(),
              myCurrentLine);
        }
      case "mcts":
        long reflexionTimeMcts = 2000;
        if (aiSettings.containsKey("reflexion")) {
          reflexionTimeMcts = Long.parseLong(aiSettings.get("reflexion"));
        }
        boolean useNN = "ML".equals(aiSettings.get("selection"));
        try {
          return new PlayerAndLine(
              new MctsPlayer(pName, pPostion, reflexionTimeMcts, useNN, pColor), myCurrentLine);
        } catch (java.io.IOException e) {
          throw new IllegalArgumentException("Impossible de charger le modèle pour MCTS", e);
        }

      case "random":
        return new PlayerAndLine(new RandomPlayer(pName, pPostion, pColor), myCurrentLine);

      case "minimax-iterative-deepening":
        {
          final String heuristicName = aiSettings.get("heuristic");
          final int reflexionTime = Integer.parseInt(aiSettings.get("reflexion"));
          return new PlayerAndLine(
              MinimaxPlayer.builder(pName, pPostion, pColor)
                  .withHeuristic(heuristicName)
                  .withReflexion(reflexionTime)
                  .withIterativeDeepening(true)
                  .build(),
              myCurrentLine);
        }
      default:
        throw new IllegalArgumentException("Unknow Ai Type : " + aiSettings.get("mode"));
    }
  }

  /**
   * Checks if the current line index player is the same of the index parameter.
   *
   * @param lines The list of strings representing the lines of the save file.
   * @param currentLine The index of the line to check.
   * @param index The index of a player
   * @return {@code true} if the line has the same index player, {@code false} otherwise.
   */
  private static boolean checkSameIndex(
      final List<String> lines, final int currentLine, final int index) {
    if (!lines.get(currentLine).contains("-")) {
      return false;
    }
    return Integer.parseInt(lines.get(currentLine).split("-")[1]) == index;
  }

  /**
   * Applies the game configuration modes (verbose, blitz, etc.) from the save file to the provided
   * engine's {@link fr.bordeaux.config.ConfigManager}.
   *
   * <p>This method reads settings at specific line indices following the [settings] header, using a
   * "key=value" format, and writes them via {@link GameEngine#getConfig()}.
   *
   * @param engine the engine whose configuration will be updated
   * @param gameState the game state to configure
   * @param lines the list of strings representing the save file content
   * @throws IllegalArgumentException if the number of players defined in settings mismatches the
   *     actual players list size
   * @throws IndexOutOfBoundsException if the file is shorter than expected
   * @throws NumberFormatException if a numeric value (nbPlayers) is improperly formatted
   */
  private static void applyMode(GameEngine engine, GameState gameState, final List<String> lines) {
    int nbPlayers = 0;
    for (String line : lines) {
      if (line.equals("[game]")) break;
      if (!line.contains("=")) continue;
      String[] parts = line.split("=");
      String key = parts[0].trim();
      String value = parts[1].trim();

      switch (key) {
        case "verbose":
          engine.setVerbose(Boolean.parseBoolean(value));
          break;
        case "blitz":
          engine.setBlitz(Boolean.parseBoolean(value));
          break;
        case "constest":
          engine.setContest(Boolean.parseBoolean(value));
          break;
        case "debug":
          engine.setDebug(Boolean.parseBoolean(value));
          break;
        case "nb-players":
          nbPlayers = Integer.parseInt(value);
          break;
      }
    }

    if (nbPlayers > 0 && gameState.getPlayers().size() != nbPlayers) {
      throw new IllegalArgumentException(
          "The number of players read from the file ("
              + nbPlayers
              + ") and the actual players in the game ("
              + gameState.getPlayers().size()
              + ") are different");
    }
  }

  private static Position coordToPos(String coord) {
    int y = coord.charAt(0) - 'a';
    int x = Integer.parseInt(coord.substring(1)) - 1;
    return new Position(x, y);
  }

  private static void readHistory(List<String> lines, List<Move> history) {
    boolean inHistory = false;

    for (String line : lines) {
      String trimmed = line.trim();

      // Detect section start
      if (trimmed.equals("[History]")) {
        inHistory = true;
        continue;
      }

      // Skip until we reach the section
      if (!inHistory) continue;

      // Stop if a new section starts
      if (trimmed.startsWith("[")) break;

      // Skip empty lines
      if (trimmed.isEmpty()) continue;

      // Split line by ';' to get individual moves: "1 e2-e3"
      String[] parts = trimmed.split(";");
      for (String part : parts) {
        String moveStr = part.trim();
        if (moveStr.isEmpty()) continue;

        // Split player number and move: ["1", "e2-e3"]
        String[] tokens = moveStr.split(" ");
        if (tokens.length < 2) continue;

        String desc = tokens[1]; // e.g., "e2-e3" or "e3v"
        Move move;

        if (desc.contains("-")) {
          // Handle Pawn Move: "e2-e3"
          String[] coords = desc.split("-");
          move = Move.pawn(coordToPos(coords[0]), coordToPos(coords[1]));
        } else {
          // Handle Wall Placement: "e3v"
          String posStr = desc.substring(0, desc.length() - 1); // "e3"
          char orientChar = desc.charAt(desc.length() - 1); // 'v' or 'h'

          Orientation orient = (orientChar == 'h') ? Orientation.HORIZONTAL : Orientation.VERTICAL;
          move = Move.wall(coordToPos(posStr), orient);
        }

        // Add to list in chronological order
        history.add(move);
      }
    }
  }
}
