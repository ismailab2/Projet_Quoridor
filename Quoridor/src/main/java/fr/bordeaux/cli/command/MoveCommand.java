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

    public static Position parsePosition(String str) {
        if (str.length() < 2) throw new IllegalArgumentException("Invalid position format");

        int row = Integer.parseInt(str.substring(1))-1;
        int col = str.charAt(0) -'a';

        return new Position(row, col);
    }

    public GameEngine handleMove(GameEngine engine) {
        String[] moves = move.split("-");
        if (moves.length != 2)
            throw new IllegalArgumentException("Invalid move syntax");

        Position from = parsePosition(moves[0]);
        Position to = parsePosition(moves[1]);

        Player current = engine.getState().getCurrentPlayer();
        if (!current.getPosition().equals(from))
            throw new IllegalArgumentException("No pawn at " + moves[0]);

        engine.play(Move.pawn(from, to));
        return engine;
      }
    } catch (Exception e) {
      e.printStackTrace();
      return engine;
    }
  }

  @Override
  public void helpText() {
    System.out.println("To implement");
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
