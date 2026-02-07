package fr.bordeaux.core;

import fr.bordeaux.graph.GraphBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Represents the Quoridor board. The board is a 9x9 grid of cells. */
public class Board {

    public static final int SIZE = 9;

    private final Cell[][] cells = new Cell[SIZE][SIZE];
    private final List<Wall> walls = new ArrayList<>();
    private final Map<Integer, List<Integer>> adjacency;

    public Board() {
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                cells[x][y] = new Cell(new Position(x, y));
            }
        }
        adjacency = GraphBuilder.buildAdjacencyList();
    }

    public Cell getCell(Position p) {
        return cells[p.getX()][p.getY()];
    }

    public boolean isInside(Position p) {
        return p.getX() >= 0 && p.getX() < SIZE &&
                p.getY() >= 0 && p.getY() < SIZE;
    }

    public List<Wall> getWalls() {
        return new ArrayList<>(walls);
    }

    public void addWall(Wall wall) {
        walls.add(wall);
        applyWall(wall);
    }

    private void removeEdge(Position p1, Position p2) {
        int u = GraphBuilder.id(p1.getX(), p1.getY());
        int v = GraphBuilder.id(p2.getX(), p2.getY());
        adjacency.get(u).remove(Integer.valueOf(v));
        adjacency.get(v).remove(Integer.valueOf(u));
        System.out.println("l'arret  est supprimer apres le pose d'une mur");
        System.out.println( "entre le sommet a la position " + u + " et sommet a la position " + v + "\n");
    }

    private void applyWall(Wall w) {
        int x = w.getPosition().getX();
        int y = w.getPosition().getY();

        if (w.getOrientation() == Wall.Orientation.HORIZONTAL) {
            cells[x][y].setBottomWall(true);
            cells[x + 1][y].setBottomWall(true);
            cells[x][y + 1].setTopWall(true);
            cells[x + 1][y + 1].setTopWall(true);

            removeEdge(new Position(x, y), new Position(x, y + 1));
            removeEdge(new Position(x + 1, y), new Position(x + 1, y + 1));

        } else {
            cells[x][y].setRightWall(true);
            cells[x][y + 1].setRightWall(true);
            cells[x + 1][y].setLeftWall(true);
            cells[x + 1][y + 1].setLeftWall(true);

            removeEdge(new Position(x, y), new Position(x + 1, y));
            removeEdge(new Position(x, y + 1), new Position(x + 1, y + 1));
        }
    }

    public List<Integer> getNeighbors(Position p) {
        int id = GraphBuilder.id(p.getX(), p.getY());
        return adjacency.get(id);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Board:\n");

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                sb.append(cells[j][i].hasTopWall() ? "---" : "   ");
                sb.append(" ");
            }
            sb.append("\n");

            for (int k = 0; k < SIZE; k++) {
                sb.append(cells[k][i].hasLeftWall() ? "|" : " ");
                Player p = cells[k][i].getPlayer();
                sb.append(p != null ? " " + p.getName().charAt(0) + " " : " . ");
                sb.append(cells[k][i].hasRightWall() ? "|" : " ");
            }
            sb.append("\n");
        }

        for (int k = 0; k < SIZE; k++) {
            sb.append(cells[k][SIZE - 1].hasBottomWall() ? "---" : "   ");
            sb.append(" ");
        }
        sb.append("\n");

        sb.append("Walls posés: ").append(walls.size());
        return sb.toString();
    }
}
