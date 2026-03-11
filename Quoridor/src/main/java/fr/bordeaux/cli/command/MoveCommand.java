package fr.bordeaux.cli.command;

import fr.bordeaux.core.GameEngine;
import fr.bordeaux.core.Move;
import fr.bordeaux.core.Orientation;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;

public class MoveCommand implements Command {

  private String move;

  public MoveCommand(String m) {
    move = m;
  }

  public MoveCommand() {
    this(null);
  }

  @Override public GameEngine execute(GameEngine engine) {
    try {
      if (move.contains("-") && (move.length()==5 || move.length()==7)) return handleMove(engine);
      else if ((move.contains("v") || move.contains("h")) && move.length() == 3) return handleWall(engine);
      else {
        System.out.println("Invalid move format");
        return engine;
      }
    } catch (Exception e) {
      e.printStackTrace();
      return engine; 
    }
  
  }
  @Override public void helpText() {
    System.out.println("=== Move Commands ===\n");
    System.out.println("Usage:");
    System.out.println("To move a pawn : e2-e3 (if it's legal).");
    System.out.println("        Remember that you can't neither move diagonally nor jump over a wall nor exit the board.");
    System.out.println("To place a wall : e2h (if it's legal).");
    System.out.println("        It place the wall horizontally under the case e2 and under the case f2.");
    System.out.println("        You can't place a wall horizontally if it's the right edge of the board (colonne E for a 5x5 board).");
    System.out.println("                : e2v (if it's legal).");
    System.out.println("        It place the wall vertically next to the case e2 and next to the case e1");
    System.out.println("        You can't place a wall vertically if it's the top edge of the board. (row 1 on any board).\n");
  }

  public Position parsePosition(String str) {
    if (str.length() < 2) throw new IllegalArgumentException("Invalid position format");

    int row = Integer.parseInt(str.substring(1)) - 1;
    int col = str.charAt(0) - 'a';

    return new Position(row, col);
  }

  public GameEngine handleMove(GameEngine engine) {
    String[] moves = move.split("-");
    if (moves.length != 2) throw new IllegalArgumentException("Invalid move syntax");

    Position from = parsePosition(moves[0]);
    Position to = parsePosition(moves[1]);

    Player current = engine.getState().getCurrentPlayer();
    if (!current.getPosition().equals(from))
      throw new IllegalArgumentException("No pawn at " + moves[0]);

    engine.play(Move.pawn(from, to));
    return engine;
  }

  public GameEngine handleWall(GameEngine engine) {
    Position pos = parsePosition(move.substring(0, move.length() - 1));
    Orientation orientation =
        (move.charAt(move.length() - 1) == 'v') ? Orientation.VERTICAL : Orientation.HORIZONTAL;
    engine.play(Move.wall(pos, orientation));
    return engine;
  }
}
