package fr.bordeaux.gui;

import fr.bordeaux.core.*;
import java.util.Arrays;

public class Main {

  public static void main(String[] args) {
    /*
       Player alice = new Player("Alice", new Position(0, 4));
       Player bob = new Player("Bob", new Position(8, 4));

       System.out.println("Joueurs créés :");
       System.out.println(alice);
       System.out.println(bob);
       System.out.println();

       try {
           Move move1 = new Move(alice, alice.getPlayerPosition(), new Position(0, 5)); // bas
           alice.setPlayerPosition(move1.getTo());
           System.out.println("Déplacement valide : " + move1);
       } catch (IllegalArgumentException e) {
           System.out.println("Déplacement invalide détecté : " + e.getMessage());
       }

       try {
           Move move2 = new Move(bob, bob.getPlayerPosition(), new Position(7, 4)); // gauche
           bob.setPlayerPosition(move2.getTo());
           System.out.println("Déplacement valide : " + move2);
       } catch (IllegalArgumentException e) {
           System.out.println("Déplacement invalide détecté : " + e.getMessage());
       }

       try {
           Move move3 = new Move(alice, alice.getPlayerPosition(), new Position(1, 6)); // diagonale interdite
           alice.setPlayerPosition(move3.getTo());
           System.out.println("Déplacement : " + move3);
       } catch (IllegalArgumentException e) {
           System.out.println("Déplacement interdit détecté correctement : " + e.getMessage());
       }

       Wall wall = new Wall(new Position(4, 4), Wall.OrientationWall.HORIZONTAL);
       Move wallMove = new Move(bob, wall);
       bob.useWall();
       System.out.println("Placement de mur : " + wallMove);

       System.out.println();
       System.out.println("Positions finales :");
       System.out.println(alice);
       System.out.println(bob);


    */

    Player alice = new Player("Alice", new Position(0, 4));
    Player bob = new Player("Bob", new Position(8, 4));

    Board board = new Board(Arrays.asList(alice, bob));
    System.out.println("Plateau initial :");
    System.out.println(board);

    Position movePos = new Position(1, 4);
    if (board.isMoveAllowed(alice.getPlayerPosition(), movePos)) {
      board.movePlayer(alice, movePos);
      System.out.println("Alice se déplace à " + movePos);
    }
    System.out.println(board);

    try {
      Position invalid = new Position(-1, 4);
      board.movePlayer(alice, invalid);
    } catch (IllegalArgumentException e) {
      System.out.println("Déplacement invalide détecté : " + e.getMessage());
    }

    Wall wall = new Wall(new Position(4, 4), Wall.OrientationWall.HORIZONTAL);
    board.placeWall(wall);
    System.out.println("Mur placé en (4,4) horizontal");
    System.out.println(board);

    // --- Déplacement bloqué par un mur ---
    Position blockedMove = new Position(4, 5);
    if (!board.isMoveAllowed(new Position(4, 4), blockedMove)) {
      System.out.println("Déplacement de (4,4) à (4,5) interdit à cause du mur");
    }

    Position winningRowBob = new Position(8, 5);
    board.movePlayer(bob, winningRowBob);
    System.out.println("Bob se déplace à " + winningRowBob);
    if (board.hasPlayerWon(bob)) {
      System.out.println("Bob a gagné !");
    }

    System.out.println("État final du plateau :");
    System.out.println(board);
  }
}
