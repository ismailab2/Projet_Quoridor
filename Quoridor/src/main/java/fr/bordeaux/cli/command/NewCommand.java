package fr.bordeaux.cli.command;

import fr.bordeaux.core.*;
import java.util.Arrays;
import java.util.List;
import org.jline.reader.LineReader;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;

public class NewCommand implements Command {

  private String[] tokens;
  private Terminal terminal;
  private LineReader reader;

  public NewCommand(String[] t, Terminal term, LineReader r) {
    this.tokens = t;
    this.terminal = term;
    this.reader = r;
  }

  public NewCommand(Terminal t, LineReader r) {
    this(new String[] {"9", "2"}, t, r);
  }

  public NewCommand() {
    this(null, null, null);
  }

  @Override
  public GameEngine execute(GameEngine engine) {
    if (tokens.length > 5) {
      System.out.println("wrong amount of arguments...");
    }
    int size = tokens.length >= 2 ? Integer.parseInt(tokens[1]) : 9;
    Position[] positions = {
      new Position(0, (size - 1) / 2), new Position(size - 1, (size - 1) / 2),
      new Position((size - 1) / 2, 0), new Position((size - 1) / 2, size - 1)
    };
    if (tokens.length == 1)
      return new GameEngine(
          new GameState(
              new Board(size),
              List.of(
                  new HumanPlayer("alice", positions[0]), new HumanPlayer("bob", positions[1]))));
    else {
      int nbPlayer = tokens.length <= 2 ? 2 : Integer.parseInt(tokens[2]);
      Board board = new Board(size);
      Player[] players = new Player[nbPlayer];

      for (int i = 0; i < nbPlayer; i++) {
        try {
          String inputName = reader.readLine("Player's name >> ");
          players[i] = new HumanPlayer(inputName, positions[i]);
        } catch (UserInterruptException e) {
          System.err.println("Tout va bien, je m'arrête !\n");
        }
      }
      engine = new GameEngine(new GameState(board, Arrays.stream(players).toList()));
    }
    return engine;
  }

  @Override
  public void helpText() {
    System.out.println("To implement");
  }
}
