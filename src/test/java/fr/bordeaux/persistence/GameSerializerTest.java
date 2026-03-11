package fr.bordeaux.persistence;

import fr.bordeaux.core.*;
import java.io.*;
import java.util.*;

public class GameSerializerTest {
  /*

  @Test
  public void testSaveGame() throws IOException {
      Board board = new Board(9);
      board.addWall(new Position(2, 3), Orientation.HORIZONTAL);
      board.addWall(new Position(4, 5), Orientation.VERTICAL);

      List<Player> players = new ArrayList<>();
      players.add(new HumanPlayer("Joueur1", new Position(0, 4)));
      players.add(new HumanPlayer("Joueur2", new Position(8, 4)));

      GameState state = new GameState(board, players, 1);
      GameSerializer.saveGame(state, "test.txt");
      System.out.println("Fichier généré !");
  }

  @Test
  public void testLoadGame() throws IOException, GameLoadException {
      GameState loaded = GameSerializer.loadGame("test.txt");

      System.out.println("Joueur courant : " + (loaded.getCurrentIndexPlayer() + 1));
      System.out.println("Joueur 1 : " + loaded.getPlayers().get(0).getPosition());
      System.out.println("Joueur 2 : " + loaded.getPlayers().get(1).getPosition());
      System.out.println("Murs J1 : " + loaded.getPlayers().get(0).getRemainingWalls());
      System.out.println("Murs J2 : " + loaded.getPlayers().get(1).getRemainingWalls());
  }

  @Test
  public void testLoadGameErreur() throws IOException {
      PrintWriter writer = new PrintWriter(new FileWriter("erreur.txt"));
      writer.println("mauvais_entete");
      writer.println("2  # Couleur du joueur courant");
      writer.close();

      try {
          GameSerializer.loadGame("erreur.txt");
      } catch (GameLoadException e) {
          System.out.println("Type erreur : " + e.getErrorType());
          System.out.println("Ligne : " + e.getLineNumber());
          System.out.println("Message : " + e.getMessage());
      }
  }

   */
}
