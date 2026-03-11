package fr.bordeaux.persistence;

import fr.bordeaux.AI.AiPlayer;
import fr.bordeaux.core.*;
import java.io.*;
import java.util.*;

public class GameSaver {
  // Board size : always 9x9
  private static final int SIZE = 9;

  // Saves the current game state to an ASCII file
  public static void saveGame(GameState state, String filename) throws IOException {
    try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
      saveParameters(writer, state); // write all parameters needed to play the game
      writeHeader(writer, state); // writes [game], current player, comment
      writeBoard(writer, state); // writes cells and walls
      writeWalls(writer, state); // writes "walls: 10 10"
      writeHistory(writer, state); // writes all moves since the beginning
    }
  }

  // Writes the 3 header lines : [game], current player, comment
  private static void writeHeader(PrintWriter writer, GameState state) {
    writer.println("[game]");
    writer.println((state.getCurrentIndexPlayer() + 1) + "  # Current player color");
    writer.println("# Board (" + state.getPlayers().size() + " players)");
  }

  // Writes all rows of the board : cell lines + wall lines
  private static void writeBoard(PrintWriter writer, GameState state) {
    Board board = state.getBoard();
    for (int row = 0; row < SIZE; row++) {
      writer.println(buildCellLine(row, board, state.getPlayers()));
      // Write horizontal wall line between rows (not after last row)
      if (row < SIZE - 1) writer.println(buildWallLine(row, board));
    }
  }

  // Builds one cell line for a given row
  // Example : ". . . . 1 . . . ." or ". . .|. . . . . ."
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

  // Returns the character to display for a given cell
  // '1' for player 1, '2' for player 2, '.' for empty cell
  private static char getCellChar(int row, int col, List<Player> players) {
    for (int i = 0; i < players.size(); i++) {
      Position position = players.get(i).getPosition();
      if (position.getX() == row && position.getY() == col) {
        return (char) ('1' + i);
      }
    }
    return '.';
  }

  // Returns true if there is a vertical wall between col-1 and col
  private static boolean hasVerticalWall(Board board, int row, int col) {
    return !board.getGraph().hasEdge(board.id(row, col - 1), board.id(row, col));
  }

  // Returns true if there is a horizontal wall between row and row+1
  private static boolean hasHorizontalWall(Board board, int row, int col) {
    return !board.getGraph().hasEdge(board.id(row, col), board.id(row + 1, col));
  }

  // Writes the remaining walls for each player
  // Example : "walls: 10 10"
  private static void writeWalls(PrintWriter writer, GameState state) {
    StringBuilder line = new StringBuilder("walls:");
    for (Player player : state.getPlayers()) {
      line.append(" ").append(player.getRemainingWalls());
    }
    writer.println(line);
  }

  private static void writeLine(PrintWriter writer, String key, Object value) {
    writer.println(key + "=" + value);
  }

  private static void saveAiSettings(PrintWriter w, GameState g, AiPlayer new_p, int i) {
    writeLine(w, "ai-player-" + i + "-name", new_p.getName());
    writeLine(w, "ai-player-" + i + "-starting-position", new_p.getStartingPosition().toString());
    Map<String, Object> aiSettings = new_p.getSettings();
    for (String s : aiSettings.keySet()) {
      writeLine(w, s, aiSettings.get(s));
    }
  }

  private static void saveParameters(PrintWriter w, GameState g) {
    w.println("[settings] # Paramètres du programme");

    writeLine(w, "verbose", g.isVerbose());
    writeLine(w, "blitz", g.isBlitz());
    writeLine(w, "constest", g.isContest());
    writeLine(w, "ai", g.isAi());
    writeLine(w, "turns-limit", g.getTurnLimit());
    writeLine(w, "debug", g.isDebug());
    writeLine(w, "nb-players", g.getPlayers().size());

    int i = 1;
    for (Player p : g.getPlayers()) {
      if (p.isAI()) {
        AiPlayer new_p = ((AiPlayer) p);
        saveAiSettings(w, g, new_p, i);
      } else {

        // writeLine(w, "player-"+i+"-remaining-time" ,  p.getRemainingTime());

        writeLine(w, "player-" + i + "-name", p.getName());

        writeLine(w, "player-" + i + "-starting-position", p.getStartingPosition().toString());
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
