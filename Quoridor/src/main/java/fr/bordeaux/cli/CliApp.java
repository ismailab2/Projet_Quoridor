package fr.bordeaux.cli;

public class CliApp {
    /** A modifier (travail de Cengiz)
     @Override
     public String toString() {
     StringBuilder sb = new StringBuilder();
     sb.append("Board:\n");

     for (int i = 0; i < size; i++) {
     for (int j = 0; j < size; j++) {
     sb.append(cells[j][i].hasTopWall() ? "---" : "   ");
     sb.append(" ");
     }
     sb.append("\n");

     for (int k = 0; k < size; k++) {
     sb.append(cells[k][i].hasLeftWall() ? "|" : " ");
     Player p = cells[k][i].getPlayer();
     sb.append(p != null ? " " + p.getName().charAt(0) + " " : " . ");
     sb.append(cells[k][i].hasRightWall() ? "|" : " ");
     }
     sb.append("\n");
     }

     for (int k = 0; k < size; k++) {
     sb.append(cells[k][size - 1].hasBottomWall() ? "---" : "   ");
     sb.append(" ");
     }
     sb.append("\n");

     sb.append("Walls posés: ").append(walls.size());
     return sb.toString();
     }
     */
}
