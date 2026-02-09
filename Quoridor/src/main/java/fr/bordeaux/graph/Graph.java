package fr.bordeaux.graph;
import java.util.List;
import java.util.Map;

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
        return  adjacencyList.get(currentId).contains(targetId);
    }

    public void addEdge(int u, int v) {
        if (!adjacencyList.get(u).contains(v)) adjacencyList.get(u).add(v);
        if (!adjacencyList.get(v).contains(u)) adjacencyList.get(v).add(u);
    }
}