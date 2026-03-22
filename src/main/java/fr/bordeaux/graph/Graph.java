package fr.bordeaux.graph;

import java.util.*;
import java.util.function.Predicate;

/** Represents an undirected graph using an adjacency list. */
public class Graph {

  /** Adjacency list storing neighbors for each vertex. */
  private final Map<Integer, List<Integer>> adjacencyList;

  /**
   * Creates a graph from the given adjacency list.
   *
   * @param adjacencyList adjacency list representing the graph
   */
  public Graph(Map<Integer, List<Integer>> adjacencyList) {
    this.adjacencyList = adjacencyList;
  }

  /**
   * Returns the neighbors of a given vertex.
   *
   * @param vertex identifier of the vertex
   * @return list of adjacent vertices
   */
  public List<Integer> getNeighbors(int vertex) {
    return adjacencyList.get(vertex);
  }

  /**
   * Removes the undirected edge between two vertices.
   *
   * @param u first vertex
   * @param v second vertex
   */
  public void removeEdge(int u, int v) {
    if (adjacencyList.containsKey(u)) {
      adjacencyList.get(u).remove(Integer.valueOf(v));
    }
    if (adjacencyList.containsKey(v)) {
      adjacencyList.get(v).remove(Integer.valueOf(u));
    }
  }

  /**
   * Checks whether an edge exists between two vertices.
   *
   * @param currentId first vertex
   * @param targetId second vertex
   * @return true if an edge exists, false otherwise
   */
  public boolean hasEdge(int currentId, int targetId) {
    return adjacencyList.get(currentId).contains(targetId);
  }

  /**
   * Adds an undirected edge between two vertices.
   *
   * @param u first vertex
   * @param v second vertex
   */
  public void addEdge(int u, int v) {
    if (!adjacencyList.get(u).contains(v)) {
      adjacencyList.get(u).add(v);
    }
    if (!adjacencyList.get(v).contains(u)) {
      adjacencyList.get(v).add(u);
    }
  }

  /**
   * Finds the shortest path from a starting vertex to any vertex satisfying the goal condition.
   * This method implements the Breadth-First Search (BFS) algorithm, which guarantees finding the
   * shortest path in an unweighted graph. It is used to verify if a player can still reach their
   * winning side and to calculate the necessary moves.
   *
   * @param startId The ID of the starting vertex (typically the player's current position).
   * @param isGoal A predicate defining the victory condition (e.g., reaching the opposite
   *     baseline).
   * @return An ordered List of vertex IDs representing the path from start to goal (inclusive).
   *     Returns an empty list if the destination is unreachable (e.g., player is blocked by walls).
   */
  public List<Integer> findPathBFS(final int startId, final Predicate<Integer> isGoal) {
    Queue<Integer> queue = new LinkedList<>();
    queue.add(startId);

    Map<Integer, Integer> parentMap = new HashMap<>();
    parentMap.put(startId, null);

    while (!queue.isEmpty()) {
      int current = queue.poll();

      if (isGoal.test(current)) {
        List<Integer> path = new ArrayList<>();
        Integer parentId = current;
        while (parentId != null) {
          path.add(parentId);
          parentId = parentMap.get(parentId);
        }
        Collections.reverse(path);
        return path;
      }

      for (int neighbor : this.getNeighbors(current)) {
        if (!parentMap.containsKey(neighbor)) {
          parentMap.put(neighbor, current);
          queue.add(neighbor);
        }
      }
    }
    return new ArrayList<>();
  }

  /**
   * Creates a deep copy of this graph.
   *
   * @return a new graph containing the same vertices and edges
   */
  public Graph copy() {
    Map<Integer, List<Integer>> newAdjacencyList = new HashMap<>();

    for (Map.Entry<Integer, List<Integer>> entry : this.adjacencyList.entrySet()) {
      List<Integer> newList = new ArrayList<>(entry.getValue());
      newAdjacencyList.put(entry.getKey(), newList);
    }
    return new Graph(newAdjacencyList);
  }
}
