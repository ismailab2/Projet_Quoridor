package fr.bordeaux.graph;

import java.util.*;
import java.util.function.Predicate;

public class Graph {

  private final Map<Integer, List<Integer>> adjacencyList;

  public Graph(Map<Integer, List<Integer>> adjacencyList) {
    this.adjacencyList = adjacencyList;
  }

  public List<Integer> getNeighbors(int vertex) {
    return adjacencyList.get(vertex);
  }

  public void removeEdge(int u, int v) {
    if (adjacencyList.containsKey(u)) adjacencyList.get(u).remove(Integer.valueOf(v));
    if (adjacencyList.containsKey(v)) adjacencyList.get(v).remove(Integer.valueOf(u));
  }

  public boolean hasEdge(int currentId, int targetId) {
    return adjacencyList.get(currentId).contains(targetId);
  }

  public void addEdge(int u, int v) {
    if (!adjacencyList.get(u).contains(v)) adjacencyList.get(u).add(v);
    if (!adjacencyList.get(v).contains(u)) adjacencyList.get(v).add(u);
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
  public List<Integer> findPathBFS(int startId, Predicate<Integer> isGoal) {
    Queue<Integer> queue = new LinkedList<>(); // vertices to visit
    queue.add(startId);

    Map<Integer, Integer> parentMap =
        new HashMap<>(); // Map representing the parents of each vertex
    parentMap.put(startId, null);

    while (!queue.isEmpty()) { // While there are vertices to visit
      int current = queue.poll(); // Pop the next one

      // Check if there is a path to victory, if there is returns the path
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

      // Add next vertices to visit
      for (int neighbor : this.getNeighbors(current)) {
        if (!parentMap.containsKey(neighbor)) {
          parentMap.put(neighbor, current);
          queue.add(neighbor);
        }
      }
    }
    return new ArrayList<>(); // Returns empty list if path doesn't exist
  }

  public Graph copy() {
    Map<Integer, List<Integer>> newAdjacencyList = new HashMap<>();

    for (Map.Entry<Integer, List<Integer>> entry : this.adjacencyList.entrySet()) {
      List<Integer> newList = new ArrayList<>(entry.getValue());

      newAdjacencyList.put(entry.getKey(), newList);
    }
    return new Graph(newAdjacencyList);
  }
}
