package fr.bordeaux;

import static fr.bordeaux.core.Orientation.*;

import fr.bordeaux.cli.CliApp;
import fr.bordeaux.core.*;

import java.util.List;

public class Main {

  public static void main(String[] args) {

    int size = 9;

    Player p1 = new HumanPlayer("Human", new Position(0, 4));
    Player p2 = new HumanPlayer("Bob", new Position(8, 4));

    Board board = new Board(size);
    GameState state = new GameState(board, List.of(p1, p2));
    GameEngine engine = new GameEngine(state);
    engine.play(Move.wall(new Position(2, 4), HORIZONTAL));
    System.out.println(engine.getState().getPlayers());
    CliApp cli = new CliApp(engine);
    cli.run();

    System.out.println("--- Début de la partie de Quoridor ---");
    System.out.println("Plateau de taille : " + board.getSize());
    System.out.println(board);

    System.out.println("\nTour de : " + state.getCurrentPlayer().getName());

    Position pos = state.getCurrentPlayer().getPosition();

    Move move1 = Move.pawn(pos, new Position(1, 4));
    engine.play(move1);
    System.out.println("Human avance en " + p1.getPosition());
    System.out.println(board);

    // engine.state().undo();
    System.out.println("Bob annuler la pose un mur horizontal en (2, 4)");
    System.out.println("Murs restants pour Bob : " + p2.getRemainingWalls());
    System.out.println(board);
    // engine.state().undo();
    System.out.println("Human avancement annuler en " + p1.getPosition());
    System.out.println(board);

    // engine.state().redo();
    System.out.println("Human fait redo et est en " + p1.getPosition());
    System.out.println(board);

    System.out.println("\nTour de : " + state.getCurrentPlayer().getName());

    Move move3 = Move.pawn(pos, new Position(2, 4));

    try {
      engine.play(move3);
      System.out.println("Alice avance en " + p1.getPosition());
    } catch (IllegalArgumentException e) {
      System.out.println("Erreur : " + e.getMessage());
    }

    System.out.println("\nÉtat final des joueurs :");
    for (Player p : state.getPlayers()) {
      System.out.println(p);
    }
  }
}
