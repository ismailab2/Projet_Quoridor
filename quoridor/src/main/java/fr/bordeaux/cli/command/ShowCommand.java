package fr.bordeaux.cli.command;

import fr.bordeaux.core.Board;
import fr.bordeaux.core.GameEngine;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;

public class ShowCommand implements Command {

  public ShowCommand() {
    System.out.println("To implement");
  }

  @Override
  public GameEngine execute(GameEngine engine) {
    printBoard(engine);
    return engine;
  }

  @Override
  public void helpText() {
    System.out.println("=== SHOW Command ===\n");
    System.out.println("Usage:");
    System.out.println("  show board");
    System.out.println("  show history");
    System.out.println("  show time\n");
    System.out.println("Description:");
    System.out.println("  Displays information about the current game.\n");
    System.out.println("Options:");
    System.out.println("  board         : Displays the current board state.");
    System.out.println("  history       : Displays the list of moves played so far.");
    System.out.println("  configuration : Displays all the configuations set for the game.");
    System.out.println("  time          : Displays the remaining time for each player.\n");
    System.out.println("If you want the help with an option, you can enter \"help show [option]\".\n");
  }

  public void helpBoard() {
    System.out.println("=== SHOW BOARD ===\n");
    System.out.println("Usage:");
    System.out.println("  show board\n");
    System.out.println("Description:");
    System.out.println("  Displays the current state of the board.\n");
  }
  public void helpHistory() {
    System.out.println("=== SHOW HISTORY ===\n");
    System.out.println("Usage:");
    System.out.println("  show history\n");
    System.out.println("Description:");
    System.out.println("  Displays the chronological list of all moves played.\n");
  }
  public void helpTime() {
    System.out.println("=== SHOW TIME ===\n");
    System.out.println("Usage:");
    System.out.println("  show time\n");
    System.out.println("Description:");
    System.out.println("  Displays the remaining time for each player.\n");
    System.out.println("  Only relevant if a timed mode is active.\n");
  }
  public void helpConfig() {
    System.out.println("=== SHOW CONFIGURATION ===\n");
    System.out.println("Usage:");
    System.out.println("  show configuration\n");
    System.out.println("Description:");
    System.out.println("  Displays the current game configuration.\n");
    System.out.println("  This includes all active settings such as:");
    System.out.println("    - Board size");
    System.out.println("    - Number of players");
    System.out.println("    - Game mode (e.g., Blitz)");
    System.out.println("    - Debug mode");
    System.out.println("    - Any other configurable parameters\n");
    System.out.println("This command does not modify the game state.\n");
  }

  private void printBoard(GameEngine engine) {
    int boardSize = engine.getState().getBoard().getSize();
    Board b = engine.getState().getBoard();
    for (int i = 0; i < boardSize; i++) System.out.print(((char) ('A' + i)) + " ");
    System.out.println("");
    for (int i = 0; i < boardSize; i++) {
      boolean empty = true;
      for (int j = 0; j < boardSize; j++) {
        int id = 1;
        for (Player player : engine.getState().getPlayers()) {
          if ((player.getPosition()).equals(new Position(i, j))) {
            System.out.print(id + printWall(b, i, j, false));
            empty = false;
          } id++;
        }
        if (empty) System.out.print("_" + ((j != boardSize - 1) ? printWall(b, i, j, false) : " "));
        empty = true;
        if (j == boardSize - 1) System.out.print((i + 1));
      }
      if (i == boardSize - 1) break;
      System.out.println("");
      for (int j = 0; j < boardSize; j++) {
        System.out.print(printWall(b, i, j, true));
      }
      System.out.println("");
    }
    System.out.println("");
  }

  private String printWall(Board b, int i, int j, boolean under) {
    if (under) {
      if (i >= b.getSize() - 1) return "X ";
      if (b.getGraph().hasEdge(b.id(i, j), b.id(i + 1, j))) return ". ";
      else return "X ";
    } else {
      if (j >= b.getSize() - 1) return "X";
      if (b.getGraph().hasEdge(b.id(i, j), b.id(i, j + 1))) return " ";
      else return "X";
    }
  }
}
