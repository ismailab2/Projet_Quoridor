package fr.bordeaux.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Represents the Quoridor board. The board is a 9x9 grid of cells. */
public class Board {
  public static final int SIZE = 9;
  private Grid[][] cells;
  private List<Wall> walls;
  private List<Player> players;

  public Board(List<Player> players) {
    this.players = players;

    this.cells = new Grid[SIZE][SIZE];
    for (int i = 0; i < SIZE; i++) {
      for (int j = 0; j < SIZE; j++) {
        cells[i][j] = new Grid(new Position(i, j));
      }
    }

    for (int i = 0; i < players.size(); i++) {
      placePlayer(players.get(i), players.get(i).getPlayerPosition());
    }
    this.walls = new ArrayList<>();
  }

  /** Place a player on the board */
  public void placePlayer(Player player, Position position) {
    if (!isValidCell(position)) {
      throw new IllegalArgumentException("Position hors plateau");
    }

    Grid grid = cells[position.getX()][position.getY()];
    if (grid.getPlayer() != null) {
      throw new IllegalArgumentException("Case déjà occupée");
    }
    grid.setPlayer(player);
    player.setPlayerPosition(position);
  }

  /** Returns true if the cell is inside the board */
  public boolean isValidCell(Position position) {
    return position.getX() >= 0
        && position.getX() < SIZE
        && position.getY() >= 0
        && position.getY() < SIZE;
  }

  /** Returns true if a move is allowed (no wall blocking and inside bounds) */
  public boolean isMoveAllowed(Position from, Position to) {
    if (!isValidCell(from) || !isValidCell(to)) return false;

    int dx = Math.abs(to.getX() - from.getX());
    int dy = Math.abs(to.getY() - from.getY());

    if (!((dx == 1 && dy == 0) || (dx == 0 && dy == 1))) return false;

    for (Wall wall : walls) {
      if (wall.blocksMovement(from, to)) {
        return false;
      }
    }

    if (cells[to.getX()][to.getY()].getPlayer() != null) {
      return false;
    }
    return true;
  }

  /** Move a player if allowed */
  public void movePlayer(Player player, Position to) {
    Position from = player.getPlayerPosition();
    if (!isMoveAllowed(from, to)) {
      throw new IllegalArgumentException("Déplacement interdit");
    }

    cells[from.getX()][from.getY()].setPlayer(null);
    cells[to.getX()][to.getY()].setPlayer(player);
    player.setPlayerPosition(to);
  }

    /** Place a wall if allowed */
    public void placeWall(Wall wall) {
        Objects.requireNonNull(wall, "Mur null");

        Position pos = wall.getPositionWall();
        int x = pos.getX();
        int y = pos.getY();

        if (!isValidWallPosition(x, y)) {
            throw new IllegalArgumentException("Mur hors plateau");
        }

        for (Wall w : walls) {
            if (w.conflictsWith(wall)) {
                throw new IllegalArgumentException("Mur en conflit avec un autre mur");
            }
        }

        walls.add(wall);

        if (wall.getOrientation() == Wall.OrientationWall.HORIZONTAL) {
            placeHorizontalWall(x, y);
        } else {
            placeVerticalWall(x, y);
        }
    }

    private boolean isValidWallPosition(int x, int y) {
        return x >= 0 && y >= 0
                && x < cells.length - 1
                && y < cells[0].length - 1;
    }

    private void placeHorizontalWall(int x, int y) {
        // Cellules du haut → mur en bas
        cells[x][y].setBottomWall(true);
        cells[x + 1][y].setBottomWall(true);

        // Cellules du bas → mur en haut
        cells[x][y + 1].setTopWall(true);
        cells[x + 1][y + 1].setTopWall(true);
    }

    private void placeVerticalWall(int x, int y) {
        // Cellules de gauche → mur à droite
        cells[x][y].setRightWall(true);
        cells[x][y + 1].setRightWall(true);

        // Cellules de droite → mur à gauche
        cells[x + 1][y].setLeftWall(true);
        cells[x + 1][y + 1].setLeftWall(true);
    }



    /** Returns the row that a player must reach to win */
  public int getWinningRowForPlayer(Player player) {
    if (player.equals(players.get(0))) return SIZE - 1; // bas du plateau
    else if (player.equals(players.get(1))) return 0; // haut du plateau
    else throw new IllegalArgumentException("Joueur inconnu");
  }

  /** Returns true if a player has reached the winning row */
  public boolean hasPlayerWon(Player player) {
    return player.getPlayerPosition().getY() == getWinningRowForPlayer(player);
  }

  /** Returns all walls on the board */
  public List<Wall> getWalls() {
    return new ArrayList<>(walls);
  }

  /** Returns the player at a given position, or null */
  public Player getPlayerAt(Position pos) {
    if (!isValidCell(pos)) return null;
    return cells[pos.getX()][pos.getY()].getPlayer();
  }

  /** Returns a copy of the board cells */
  public Grid[][] getCells() {
    Grid[][] copy = new Grid[SIZE][SIZE];
    for (int x = 0; x < SIZE; x++) {
      for (int y = 0; y < SIZE; y++) {
        copy[x][y] = cells[x][y];
      }
    }
    return copy;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Board:\n");

    for (int y = 0; y < SIZE; y++) {
      for (int x = 0; x < SIZE; x++) {
        sb.append(cells[x][y].hasTopWall() ? "———" : "   ");
        sb.append(" ");
      }
      sb.append("\n");

      for (int x = 0; x < SIZE; x++) {
        sb.append(cells[x][y].hasLeftWall() ? "|" : " ");
        Player p = cells[x][y].getPlayer();
        sb.append(p != null ? " " + p.getName().charAt(0) + " " : " . ");
        sb.append(cells[x][y].hasRightWall() ? "|" : " ");
      }
      sb.append("\n");
    }

    for (int x = 0; x < SIZE; x++) {
      sb.append(cells[x][SIZE - 1].hasBottomWall() ? "———" : "   ");
      sb.append(" ");
    }
    sb.append("\n");

    sb.append("Walls posés: ").append(walls.size());
    return sb.toString();
  }
}
