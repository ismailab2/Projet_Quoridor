package fr.bordeaux.graph;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import org.junit.Test;

public class TestGraphBuilder {

  @Test
  public void testGraphHas81Vertices() {
    Map<Integer, List<Integer>> adj = GraphBuilder.buildAdjacencyList();
    assertEquals(81, adj.size());
  }

  @Test
  public void testCornerNeighbors() {
    Map<Integer, List<Integer>> adj = GraphBuilder.buildAdjacencyList();

    assertTrue(adj.get(0).contains(1));
    assertTrue(adj.get(0).contains(9));
    assertEquals(2, adj.get(0).size());

    assertTrue(adj.get(80).contains(79));
    assertTrue(adj.get(80).contains(71));
    assertEquals(2, adj.get(80).size());
  }

  @Test
  public void testCenterNeighbors() {
    Map<Integer, List<Integer>> adj = GraphBuilder.buildAdjacencyList();

    int id = GraphBuilder.id(4, 4);

    assertTrue(adj.get(id).contains(GraphBuilder.id(3, 4)));
    assertTrue(adj.get(id).contains(GraphBuilder.id(5, 4)));
    assertTrue(adj.get(id).contains(GraphBuilder.id(4, 3)));
    assertTrue(adj.get(id).contains(GraphBuilder.id(4, 5)));

    assertEquals(4, adj.get(id).size());
  }

  @Test
  public void testUndirectedEdges() {
    Map<Integer, List<Integer>> adj = GraphBuilder.buildAdjacencyList();
    assertTrue(adj.get(0).contains(1));
    assertTrue(adj.get(1).contains(0));
  }
}
