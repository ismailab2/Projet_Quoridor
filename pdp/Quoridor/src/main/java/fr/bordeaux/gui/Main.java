package fr.bordeaux.gui;

import fr.bordeaux.core.*;
import java.util.List;

public class Main {

  public static void main(String[] args) {

    Player p1 = new Player("Alice", new Position(4, 0));
    Player p2 = new Player("Bob", new Position(4, 8));
    Board board = new Board();
    System.out.println("le plateau vide du debut : ");
    System.out.println(board);

    GameState state = new GameState(board, List.of(p1, p2));
    GameEngine engine = new GameEngine(state);
    System.out.println("le plateau avec les joueurs : ");
    System.out.println(board);

    System.out.println("Début de la partie");
    System.out.println("Joueur courant : " + state.currentPlayer().getName());
    engine.play(Move.pawn(new Position(4, 1)));
    System.out.println("Alice avance en " + p1.getPosition());
    System.out.println("le plateau apres le deplacement du joueur 1 : ");
    System.out.println(board);

    engine.play(Move.wall(new Wall(new Position(4, 2), Wall.Orientation.HORIZONTAL)));
    System.out.println("Bob pose un mur horizontal");
    System.out.println("le plateau apres le pose d'un mur  : ");
    System.out.println(board);

    engine.play(Move.pawn(new Position(4, 2)));
    System.out.println("Alice avance encore " + p1.getPosition());
    System.out.println("le plateau apres le deplacement du joueur 1 : ");
    System.out.println(board);

    /*
    engine.play(Move.pawn(new Position(4, 3)));
    System.out.println("Alice avance encore " + p1.getPosition());

     */

    System.out.println("le plateau apres le deplacement du joueur 1 : ");
    System.out.println(board);

    System.out.println("Joueur courant : " + state.currentPlayer().getName());
  }
}
