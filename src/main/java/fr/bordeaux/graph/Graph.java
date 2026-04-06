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
  public Graph(final Map<Integer, List<Integer>> adjacencyList) {
    this.adjacencyList = new java.util.concurrent.ConcurrentHashMap<>(adjacencyList);
  }

  /**
   * Returns the neighbors of a given vertex.
   *
   * @param vertex identifier of the vertex
   * @return list of adjacent vertices
   */
  public List<Integer> getNeighbors(final int vertex) {
    return adjacencyList.get(vertex);
  }

  /**
   * Removes the undirected edge between two vertices.
   *
   * @param vertexU first vertex
   * @param vertexV second vertex
   */
  public void removeEdge(final int vertexU, final int vertexV) {
    if (adjacencyList.containsKey(vertexU)) {
      adjacencyList.get(vertexU).remove(Integer.valueOf(vertexV));
    }
    if (adjacencyList.containsKey(vertexV)) {
      adjacencyList.get(vertexV).remove(Integer.valueOf(vertexU));
    }
  }

  /**
   * Checks whether an edge exists between two vertices.
   *
   * @param currentId first vertex
   * @param targetId second vertex
   * @return true if an edge exists, false otherwise
   */
  public boolean hasEdge(final int currentId, final int targetId) {
    return adjacencyList.get(currentId).contains(targetId);
  }

  /**
   * Adds an undirected edge between two vertices.
   *
   * @param vertexU first vertex
   * @param vertexV second vertex
   */
  public void addEdge(final int vertexU, final int vertexV) {
    if (!adjacencyList.get(vertexU).contains(vertexV)) {
      adjacencyList.get(vertexU).add(vertexV);
    }
    if (!adjacencyList.get(vertexV).contains(vertexU)) {
      adjacencyList.get(vertexV).add(vertexU);
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
    final Queue<Integer> queue = new LinkedList<>();
    queue.add(startId);

    final Map<Integer, Integer> parentMap = new HashMap<>();
    parentMap.put(startId, null);

    List<Integer> result = new ArrayList<>();
    while (!queue.isEmpty()) {
      final int current = queue.poll();

      if (isGoal.test(current)) {
        result = reconstructPath(parentMap, current);
        break;
      }

      for (final int neighbor : this.getNeighbors(current)) {
        if (!parentMap.containsKey(neighbor)) {
          parentMap.put(neighbor, current);
          queue.add(neighbor);
        }
      }
    }
    return result;
  }

  /**
   * Reconstructs the path from the start vertex to the goal vertex using the parent map.
   *
   * @param parentMap map storing the parent of each visited vertex
   * @param goalId the identifier of the goal vertex
   * @return ordered list of vertex IDs representing the path
   */
  private List<Integer> reconstructPath(final Map<Integer, Integer> parentMap, final int goalId) {
    final List<Integer> path = new ArrayList<>();
    Integer current = goalId;
    while (current != null) {
      path.add(current);
      current = parentMap.get(current);
    }
    Collections.reverse(path);
    return path;
  }

  /**
   * Creates a deep copy of this graph.
   *
   * @return a new graph containing the same vertices and edges
   */
  public Graph copy() {
    final Map<Integer, List<Integer>> newAdj = new java.util.concurrent.ConcurrentHashMap<>();

    for (final Map.Entry<Integer, List<Integer>> entry : this.adjacencyList.entrySet()) {
      newAdj.put(entry.getKey(), new ArrayList<>(entry.getValue()));
    }
    return new Graph(newAdj);
  }
}
