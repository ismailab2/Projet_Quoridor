package fr.bordeaux.core;

import fr.bordeaux.graph.*;

import java.util.*;
import java.util.function.Predicate;

/** Represents the Quoridor board. The size of the board is odd and between 3x3 and 15x15. */
public class Board {

    private final int size;

    private final Graph graph;

    public Board(int size) {
        if (size < 3 || size > 15 || size % 2 == 0) {
            throw new IllegalArgumentException("Size must be odd and between 3 and 15, but was : " + size);
        }
        //Create the graph representing the board
        graph = GraphBuilder.buildEmptyGrid(size);
        this.size = size;
    }

    public int id(int row, int col) {
        return row * size + col;
    }

    //Method to add a wall by removing corresponding edges.
    public void addWall(Position pos, Orientation orientation) {
        int u1 = id(pos.row(), pos.col());
        int u2 = id(pos.row(), pos.col() + 1);
        int v1 = id(pos.row() + 1, pos.col());
        int v2 = id(pos.row() + 1, pos.col() + 1);

        if (orientation == Orientation.HORIZONTAL) {
            graph.removeEdge(u1, v1);
            graph.removeEdge(u2, v2);
        } else {
            graph.removeEdge(u1, u2);
            graph.removeEdge(v1, v2);
        }
    }

    //Method to remove a wall by adding back the corresponding edges
    public void removeWall(Position pos, Orientation orientation) {
        int u1 = id(pos.row(), pos.col());
        int u2 = id(pos.row(), pos.col() + 1);
        int v1 = id(pos.row() + 1, pos.col());
        int v2 = id(pos.row() + 1, pos.col() + 1);

        if (orientation == Orientation.HORIZONTAL) {
            graph.addEdge(u1, v1);
            graph.addEdge(u2, v2);
        } else {
            graph.addEdge(u1, u2);
            graph.addEdge(v1, v2);
        }
    }

    //Check if position is inside the board.
    public boolean isInside(Position position) {
        return position.row() >= 0 && position.row() < size && position.col() >= 0 && position.col() < size;
    }

    public Graph getGraph() {
        return graph;
    }

    public int getSize() {
        return size;
    }

    public boolean isWallCollision(Position to, Orientation orientation) {
        int u1 = id(to.row(), to.col());
        int u2 = id(to.row(), to.col() + 1);
        int v1 = id(to.row() + 1, to.col());
        int v2 = id(to.row()+ 1, to.col() + 1);

        //Overlap : At least a part of the wall is colliding with another wall
        //Cross : There is already a wall at the same place but different orientation, can't form a cross
        boolean hOverlap = !graph.hasEdge(u1, v1) || !graph.hasEdge(u2, v2);
        boolean hCross = !graph.hasEdge(u1, u2) && !graph.hasEdge(v1, v2);

        boolean vOverlap = !graph.hasEdge(u1, u2) || !graph.hasEdge(v1, v2);
        boolean vCross = !graph.hasEdge(u1, v1) && !graph.hasEdge(u2, v2);

        return (orientation == Orientation.HORIZONTAL) ? hOverlap || hCross : vOverlap || vCross;
    }

    /**
     * Finds the shortest path from a starting vertex to any vertex satisfying the goal condition.
     * This method implements the Breadth-First Search (BFS) algorithm, which guarantees
     * finding the shortest path in an unweighted graph. It is used to verify if a player
     * can still reach their winning side and to calculate the necessary moves.
     *
     * @param startId The ID of the starting vertex (typically the player's current position).
     * @param isGoal  A predicate defining the victory condition (e.g., reaching the opposite baseline).
     * @return An ordered List of vertex IDs representing the path from start to goal (inclusive).
     * Returns an empty list if the destination is unreachable (e.g., player is blocked by walls).
     */
    public List<Integer> findPath(int startId, Predicate<Integer> isGoal) {
        Queue<Integer> queue = new LinkedList<>(); // vertices to visit
        queue.add(startId);

        Map<Integer, Integer> parentMap = new HashMap<>(); // Map representing the parents of each vertex
        parentMap.put(startId, null);

        while (!queue.isEmpty()) { //While there are vertices to visit
            int current = queue.poll(); //Pop the next one

            //Check if there is a path to victory, if there is returns the path
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

            //Add next vertices to visit
            for (int neighbor : graph.getNeighbors(current)) {
                if (!parentMap.containsKey(neighbor)) {
                    parentMap.put(neighbor,  current);
                    queue.add(neighbor);
                }
            }
        }
        return new ArrayList<>(); //Returns empty list if path doesn't exist
    }


}
