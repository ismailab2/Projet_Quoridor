package fr.bordeaux.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Utility class used to build graph instances.
 */
public class GraphBuilder {


    /**
     * Private constructor to prevent instantiation.
     */
    private GraphBuilder() {
    }

    /**
     * Builds an empty square grid graph of the given size.
     *
     * @param size number of rows and columns in the grid
     * @return a graph representing the empty grid
     */
    public static Graph buildEmptyGrid(int size) {
        Map<Integer, List<Integer>> adj = new HashMap<>();
        int nbVertices = size * size;

        for (int v = 0; v < nbVertices; v++) {
            adj.put(v, new ArrayList<>(4));
        }

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                int u = row * size + col;

                if (row < size - 1) {
                    addUndirectedEdge(adj, u, (row + 1) * size + col);
                }
                if (col < size - 1) {
                    addUndirectedEdge(adj, u, row * size + (col + 1));
                }
            }
        }
        return new Graph(adj);
    }

    /**
     * Adds an undirected edge between two vertices.
     *
     * @param adj adjacency list of the graph
     * @param u   first vertex
     * @param v   second vertex
     */
    private static void addUndirectedEdge(Map<Integer, List<Integer>> adj, int u, int v) {
        adj.get(u).add(v);
        adj.get(v).add(u);
    }
}
