package fr.bordeaux.persistence;

import fr.bordeaux.ai.AiPlayer;
import fr.bordeaux.core.*;
import java.io.*;
import java.util.*;

/** Utility class used to save a game state to a file. */
public class GameSaver {
  // Board size : always 9x9
  private static int SIZE = 9;

  /** Private constructor to prevent instantiation. */
  private GameSaver() {}

  /**
   * Saves the current game state to a file.
   *
   * @param state game state to save
   * @param filename path of the destination file
   * @throws IOException if an input/output error occurs while writing the file
   */
  public static void saveGame(GameState state, String filename) throws IOException {
    try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
      SIZE = state.getBoard().getSize();
      saveParameters(writer, state); // write all parameters needed to play the game
      writeHeader(writer, state); // writes [game], current player, comment
      writeBoard(writer, state); // writes cells and walls
      writeWalls(writer, state); // writes "walls: 10 10"
      writeHistory(writer, state); // writes all moves since the beginning
    }
  }

  /**
   * Writes the game section header to the save file.
   *
   * @param writer writer used to save the data
   * @param state game state containing the current player information
   */
  private static void writeHeader(PrintWriter writer, GameState state) {
    writer.println("[game]");
    writer.println((state.getCurrentPlayer().getColor().getId()) + "  # Current player color");
    writer.println(state.getBoard().getSize() + "  # Board size");
    writer.println("# Board (" + state.getPlayers().size() + " players)");
  }

  /**
   * Writes the full board representation to the save file.
   *
   * @param writer writer used to save the data
   * @param state game state containing the board to write
   */
  private static void writeBoard(PrintWriter writer, GameState state) {
    Board board = state.getBoard();
    for (int row = 0; row < SIZE; row++) {
      writer.println(buildCellLine(row, board, state.getPlayers()));
      if (row < SIZE - 1) {
        writer.println(buildWallLine(row, board));
      }
    }
  }

  /**
   * Builds the textual representation of one board row.
   *
   * @param row row index to build
   * @param board board to inspect
   * @param players list of players placed on the board
   * @return string representing the row content
   */
  private static String buildCellLine(int row, Board board, List<Player> players) {
    StringBuilder line = new StringBuilder();
    for (int col = 0; col < SIZE; col++) {
      // Add '|' if there is a vertical wall between col-1 and col
      if (col > 0) line.append(hasVerticalWall(board, row, col) ? '|' : ' ');
      // Add player number '1'/'2' or '.' if cell is empty
      line.append(getCellChar(row, col, players));
    }
    return line.toString();
  }

  // Builds one horizontal wall line between two rows
  private static String buildWallLine(int row, Board board) {
    StringBuilder line = new StringBuilder();
    for (int col = 0; col < SIZE; col++) {
      if (col > 0) line.append(" ");
      // Add '_' if there is a horizontal wall between row and row+1
      line.append(hasHorizontalWall(board, row, col) ? '_' : ' ');
    }
    return line.toString();
  }

  /**
   * Returns the character used to represent the content of a board cell.
   *
   * @param row row of the cell
   * @param col column of the cell
   * @param players list of players to inspect
   * @return '1' for player 1, '2' for player 2, or '.' if the cell is empty
   */
  private static char getCellChar(int row, int col, List<Player> players) {
    for (int i = 0; i < players.size(); i++) {
      Position position = players.get(i).getPosition();
      if (position.getX() == row && position.getY() == col) {
        return (char) ('1' + i);
      }
    }
    return '.';
  }

  /**
   * Checks whether a vertical wall exists between two adjacent cells.
   *
   * @param board board to inspect
   * @param row row of the cell
   * @param col column of the right cell
   * @return true if a vertical wall is present, false otherwise
   */
  private static boolean hasVerticalWall(Board board, int row, int col) {
    return !board.getGraph().hasEdge(board.id(row, col - 1), board.id(row, col));
  }

  /**
   * Checks whether a horizontal wall exists between two adjacent rows.
   *
   * @param board board to inspect
   * @param row row of the upper cell
   * @param col column of the cell
   * @return true if a horizontal wall is present, false otherwise
   */
  private static boolean hasHorizontalWall(Board board, int row, int col) {
    return !board.getGraph().hasEdge(board.id(row, col), board.id(row + 1, col));
  }

  /**
   * Writes the remaining wall count for each player.
   *
   * @param writer writer used to save the data
   * @param state game state containing the players
   */
  private static void writeWalls(PrintWriter writer, GameState state) {
    StringBuilder line = new StringBuilder("walls:");
    for (Player player : state.getPlayers()) {
      line.append(" ").append(player.getRemainingWalls());
    }
    writer.println(line);
  }

  /**
   * Writes a single key-value pair to the output stream in the format "key=value".
   *
   * @param writer The {@link PrintWriter} used to write to the file.
   * @param key The setting key.
   * @param value The setting value.
   */
  private static void writeLine(PrintWriter writer, String key, Object value) {
    writer.println(key + "=" + value);
  }

  /**
   * Saves the specific settings of an AI player to the output file.
   *
   * @param w The {@link PrintWriter} used to write to the file.
   * @param g The current game state.
   * @param newP The AI player whose settings are being saved.
   * @param i The index of the player (used for formatting the key).
   */
  private static void saveAiSettings(PrintWriter w, GameState g, AiPlayer newP, int i) {
    writeLine(w, "ai-" + i + "-name", newP.getName());
    writeLine(w, "ai-" + i + "-color", newP.getColor());
    writeLine(w, "ai-" + i + "-starting-position", newP.getStartingPosition().toString());
    writeLine(w, "ai-" + i + "-remaining-time", newP.getRemainingTime());
    Map<String, Object> aiSettings = newP.getSettings();
    for (String s : aiSettings.keySet()) {
      writeLine(w, "ai-" + i + "-" + s, aiSettings.get(s));
    }
  }

  /**
   * Saves the global game parameters and all players' information to the output file. Writes the
   * "[settings]" header followed by game rules and player configurations.
   *
   * @param w The {@link PrintWriter} used to write to the file.
   * @param g The current game state containing the parameters to save.
   */
  private static void saveParameters(PrintWriter w, GameState g) {
    w.println("[settings]");

    writeLine(w, "verbose", g.isVerbose());
    writeLine(w, "blitz", g.isBlitz());
    writeLine(w, "constest", g.isContest());
    writeLine(w, "turns-limit", g.getTurnLimit());
    writeLine(w, "debug", g.isDebug());
    writeLine(w, "nb-players", g.getPlayers().size());

    int i = 1;
    for (Player p : g.getPlayers()) {
      if (p.isAI()) {
        AiPlayer newP = ((AiPlayer) p);
        saveAiSettings(w, g, newP, i);
      } else {

        writeLine(w, "player-" + i + "-name", p.getName());

        writeLine(w, "player-" + i + "-color", p.getColor());

        writeLine(w, "player-" + i + "-starting-position", p.getStartingPosition().toString());

        writeLine(w, "player-" + i + "-remaining-time", p.getRemainingTime());
      }
      i++;
    }
  }

  private static String posToCoord(Position p) {
    if (p == null) return "";
    char col = (char) ('a' + p.getY());
    int row = p.getX() + 1;
    return "" + col + row;
  }

  private static void writeHistory(PrintWriter w, GameState g) {
    w.println("[History]");

    Move[] moves = g.getMovesPlayed();
    int playersCount = g.getPlayers().size();
    int currentPlayer = 0;

    for (int i = moves.length - 1; i >= 0; i--) {
      String strMove = (currentPlayer + 1) + " ";
      Position to = moves[i].getTo();

      if (moves[i].isPawn()) {
        Position from = moves[i].getFrom();
        strMove += posToCoord(from) + '-' + posToCoord(to);
      } else {
        Orientation orientation = moves[i].getOrientation();
        strMove += posToCoord(to) + orientation.toString();
      }
      strMove += ';';
      if (currentPlayer == playersCount - 1) {
        // If it's the end of a turn, we make a new line
        w.println(strMove);
      } else {
        // Add a space only if it's not the last move to write on the line
        w.print(strMove + ' ');
      }
      currentPlayer = (currentPlayer + 1) % playersCount;
    }
  }
}
