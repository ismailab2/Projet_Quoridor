package fr.bordeaux.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphBuilder {

  private GraphBuilder() {}

  public static Graph buildEmptyGrid(int size) {
    Map<Integer, List<Integer>> adj = new HashMap<>();
    int nbVertices = size * size;
    // Create vertices
    for (int v = 0; v < nbVertices; v++) {
      adj.put(v, new ArrayList<>(4));
    }

    // Add edges between adjacent cells
    for (int row = 0; row < size; row++) {
      for (int col = 0; col < size; col++) {
        int u = row * size + col;

        // To avoid adding the same edge twice,
        // we only connect to the bottom and to the right neighbors.
        if (row < size - 1) addUndirectedEdge(adj, u, (row + 1) * size + col);
        if (col < size - 1) addUndirectedEdge(adj, u, row * size + (col + 1));
      }
    }
    return new  Graph(adj);
  }

  // Adds an undirected edge between vertices u and v
  private static void addUndirectedEdge(Map<Integer, List<Integer>> adj, int u, int v) {
    adj.get(u).add(v);
    adj.get(v).add(u);
  }
}
