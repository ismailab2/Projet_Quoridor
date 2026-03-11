package fr.bordeaux.persistence;

import fr.bordeaux.AI.MCTSPlayer;
import fr.bordeaux.AI.MinimaxPlayer;
import fr.bordeaux.AI.NeuralNetPlayer;
import fr.bordeaux.AI.RandomPlayer;
import fr.bordeaux.core.*;
import java.io.*;
import java.util.*;

public class GameLoader {
  // Board size : always 9x9
  private static final int SIZE = 9;

  private static final int BOARD_START = 3;

  // Loads a file and reconstructs the GameState
  public static GameState loadGame(String filename) throws IOException, GameLoadException {
    List<String> lines = readAllLines(filename); // reads all lines from file
    validateSettingsHeader(lines); // checks [settings] header
    AllPlayerAndLine allp_l = readAllPlayer(lines); // get players and the last line read
    List<Player> players = allp_l.players; // creates all players
    int currentLine = allp_l.nextLine; // Line after settings
    validateHeader(lines, currentLine); // checks [game] header
    Board board = new Board(SIZE); // creates empty 9x9 board
    readBoard(lines, board, players, currentLine); // places walls and players
    readRemainingWalls(lines, players, currentLine); // restores wall counts

    List<Move> history = new ArrayList<>();
    readHistory(lines, history);
    GameState gameState = new GameState(board, players, readCurrentPlayer(lines, currentLine));
    for (Move m : history) {
      gameState.applyMove(m);
    }
    return gameState;
  }

  // Opens the file and reads all lines into a list
  private static List<String> readAllLines(String filename) throws IOException {
    try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
      List<String> lines = new ArrayList<>();
      String line;
      while ((line = reader.readLine()) != null) lines.add(line);
      return lines;
    }
  }

  // Checks that the first line is "[game]"
  // If not, throws GameLoadException with error type and line number
  private static void validateHeader(List<String> lines, int currentLine) throws GameLoadException {
    if (!lines.get(currentLine).equals("[game]"))
      throw new GameLoadException(
          "ERREUR_ENTETE", 1, "Expected [game] but found : " + lines.get(currentLine));
  }

  // Checks that the first line is "[settings]"
  // If not, throws GameLoadException with error type and line number
  private static void validateSettingsHeader(List<String> lines) throws GameLoadException {
    if (!lines.get(0).equals("[settings] # Paramètres du programme"))
      throw new GameLoadException(
          "ERREUR_ENTETE", 1, "Expected [settings] but found : " + lines.get(0));
  }

  // Reads the current player index from line 2
  // Example : "2  # Current player color" , returns 1
  private static int readCurrentPlayer(List<String> lines, int currentLine) {
    return Integer.parseInt(lines.get(currentLine + 1).trim().split("#")[0].trim()) - 1;
  }

  // Creates the 2 players with their default starting positions
  private static List<Player> createDefaultPlayers() {
    return new ArrayList<>(
        Arrays.asList(
            new HumanPlayer("Joueur1", new Position(0, 4)),
            new HumanPlayer("Joueur2", new Position(8, 4))));
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

  private record PlayerAndLine(
      Player player, int nextLine) {} // structure to return the player and the last line read

  private record AllPlayerAndLine(
      List<Player> players,
      int nextLine) {} // structure to return the player and the last line read

  private static AllPlayerAndLine readAllPlayer(List<String> lines) {
    List<Player> lPlayers = new ArrayList<>();
    int currentLine = 8; // 8 because its at the 9 lines that starts the informations about players
    while (currentLine < lines.size() && !lines.get(currentLine).equals("[game]")) {
      PlayerAndLine res = readPlayer(lines, currentLine);
      lPlayers.add(res.player);
      currentLine = res.nextLine + 1;
    }
    return new AllPlayerAndLine(lPlayers, currentLine);
  }

  private static PlayerAndLine readPlayer(List<String> lines, int currentLine) {

    String p = lines.get(currentLine);
    String p_type = p.split("=")[0].split("-")[0];
    String p_name = p.split("=")[1];
    p = lines.get(++currentLine);
    Position p_postion = readPlayerStartPosition(p);

    if (p_type.equals("ai")) {
      return readAiPlayer(lines, currentLine, p_name, p_postion);
    } else {
      return new PlayerAndLine(new HumanPlayer(p_name, p_postion), currentLine);
    }
  }

  private static Position readPlayerStartPosition(String p) {
    String p_start_pos = p.split("=")[1];
    String p_start_pos_coord = p_start_pos.split(":")[1];
    int x =
        Integer.parseInt(
            p_start_pos_coord.split(",")[0].replaceAll(
                " ", "")); // Spaces must be removed because ParseInt does not handle them.
    int y = Integer.parseInt(p_start_pos_coord.split(",")[1].replaceAll(" ", ""));
    return new Position(x, y);
  }

  private static PlayerAndLine readAiPlayer(
      List<String> lines, int currentLine, String p_name, Position p_postion) {
    Map<String, String> aiSettings = new HashMap<>();
    while (checkStartByAi(lines, ++currentLine)) {
      String[] ls = lines.get(currentLine).split("=");
      String[] aiParameter = ls[0].split("-");
      aiSettings.put(aiParameter[aiParameter.length - 1], ls[1]);
    }

    switch (aiSettings.get("mode")) {
      case "minimax":
        {
          String heuristicName = aiSettings.get("heuristic");
          int depth = Integer.parseInt(aiSettings.get("depth"));
          int reflexion_time = Integer.parseInt(aiSettings.get("reflexion"));
          return new PlayerAndLine(
              MinimaxPlayer.builder(p_name, p_postion)
                  .withHeuristic(heuristicName)
                  .withDepth(depth)
                  .withReflexion(reflexion_time)
                  .build(),
              currentLine);
        }
      case "mcts":
        return new PlayerAndLine(new MCTSPlayer(p_name, p_postion), currentLine);

      case "neural-network":
        return new PlayerAndLine(new NeuralNetPlayer(p_name, p_postion), currentLine);

      case "random":
        return new PlayerAndLine(new RandomPlayer(p_name, p_postion), currentLine);

      case "minimax-iterative-deepening":
        {
          String heuristicName = aiSettings.get("heuristic");
          int reflexion_time = Integer.parseInt(aiSettings.get("reflexion"));
          return new PlayerAndLine(
              MinimaxPlayer.builder(p_name, p_postion)
                  .withHeuristic(heuristicName)
                  .withReflexion(reflexion_time)
                  .isIterativeDeepening(true)
                  .build(),
              currentLine);
        }
      default:
        throw new IllegalArgumentException("Unknow Ai Type : " + aiSettings.get("mode"));
    }
  }

  private static boolean checkStartByAi(List<String> lines, int currentLine) {
    return lines.get(currentLine).startsWith("ai");
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
