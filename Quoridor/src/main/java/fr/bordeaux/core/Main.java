package fr.bordeaux.core;

import java.util.List;

import fr.bordeaux.cli.CliApp;
import static fr.bordeaux.core.Orientation.*;

public class Main {

  public static void main(String[] args) {
    
    CliApp cli = new CliApp();
    cli.run();

    int size = 9;

    Player p1 = new Player("Alice", new Position(0, 4));
    Player p2 = new Player("Bob", new Position(8, 4));

    Board board = new Board(size);
    GameState state = new GameState(board, List.of(p1, p2));
    GameEngine engine = new GameEngine(state);

    System.out.println("--- Début de la partie de Quoridor ---");
    System.out.println("Plateau de taille : " + board.getSize());
    System.out.println(board);

    System.out.println("\nTour de : " + state.currentPlayer().getName());
    Move move1 = Move.pawn(new Position(1, 4));
    engine.play(move1);
    System.out.println("Alice avance en " + p1.getPosition());
    System.out.println(board);

    System.out.println("\nTour de : " + state.currentPlayer().getName());
    Move move2 = Move.wall(new Position(2, 4), HORIZONTAL);
    engine.play(move2);
    System.out.println("Bob pose un mur horizontal en (2, 4)");
    System.out.println("Murs restants pour Bob : " + p2.getRemainingWalls());
    System.out.println(board);

    System.out.println("\nTour de : " + state.currentPlayer().getName());
    Move move3 = Move.pawn(new Position(2, 4));

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
