package fr.bordeaux.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphBuilder {
  public static final int SIZE = 9;
  public static final int NB_VERTICES = SIZE * SIZE;

  public static int id(int row, int col) {
    return row * SIZE + col;
  }

  public static Map<Integer, List<Integer>> buildAdjacencyList() {
    Map<Integer, List<Integer>> adj = new HashMap<>();

    // création des sommets
    for (int v = 0; v < NB_VERTICES; v++) {
      adj.put(v, new ArrayList<>(4));
    }

    // ajout des arêtes entre cases adjacentes
    for (int row = 0; row < SIZE; row++) {
      for (int col = 0; col < SIZE; col++) {
        int u = id(row, col);

        // Pour éviter d'ajouter deux fois la même arête,
        // on relie seulement vers le bas et vers la droite.
        if (row < SIZE - 1) addUndirectedEdge(adj, u, id(row + 1, col));
        if (col < SIZE - 1) addUndirectedEdge(adj, u, id(row, col + 1));
      }
    }
    return adj;
  }

  // Ajoute une arête non orientée entre u et v
  private static void addUndirectedEdge(Map<Integer, List<Integer>> adj, int u, int v) {
    adj.get(u).add(v);
    adj.get(v).add(u);
  }
}
