package fr.bordeaux.persistence;

import fr.bordeaux.AI.AiPlayer;
import fr.bordeaux.AI.MCTSPlayer;
import fr.bordeaux.AI.MinimaxPlayer;
import fr.bordeaux.AI.NeuralNetPlayer;
import fr.bordeaux.AI.RandomPlayer;
import fr.bordeaux.core.*;

import java.io.*;
import java.util.*;

public class GameSerializer {
    // Board size : always 9x9
    private static final int SIZE = 9;

    private static final int BOARD_START = 3;
    // Saves the current game state to an ASCII file
    public static void saveGame(GameState state, String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            saveParameters(writer, state); // write all parameters needed to play the game
            writeHeader(writer, state);  // writes [game], current player, comment
            writeBoard(writer, state);   // writes cells and walls
            writeWalls(writer, state);   // writes "walls: 10 10"
        }
    }

    // Loads a file and reconstructs the GameState
    public static GameState loadGame(String filename) throws IOException, GameLoadException {
        List<String> lines = readAllLines(filename);       // reads all lines from file
        validateSettingsHeader(lines);                      // checks [settings] header
        AllPlayerAndLine allp_l = readAllPlayer(lines);     // get players and the last line read
        List<Player> players = allp_l.players;              // creates all players
        int currentLine = allp_l.nextLine;                 // Line after settings
        validateHeader(lines, currentLine);                              // checks [game] header
        Board board = new Board(SIZE);                      // creates empty 9x9 board
        readBoard(lines, board, players, currentLine);                   // places walls and players
        readRemainingWalls(lines, players, currentLine);                 // restores wall counts
        return new GameState(board, players, readCurrentPlayer(lines, currentLine));
    }

    

    // Writes the 3 header lines : [game], current player, comment
    private static void writeHeader(PrintWriter writer, GameState state) {
        writer.println("[game]");
        writer.println((state.getCurrentIndexPlayer() + 1) + "  # Current player color");
        writer.println("# Board (" + state.getPlayers().size() + " players)");
    }

    // Writes all rows of the board : cell lines + wall lines
    private static void writeBoard(PrintWriter writer, GameState state) {
        Board board = state.getBoard();
        for (int row = 0; row < SIZE; row++) {
            writer.println(buildCellLine(row, board, state.getPlayers()));
            // Write horizontal wall line between rows (not after last row)
            if (row < SIZE - 1) writer.println(buildWallLine(row, board));
        }
    }

    // Builds one cell line for a given row
    // Example : ". . . . 1 . . . ." or ". . .|. . . . . ."
    private static String buildCellLine(int row, Board board, List<Player> players) {
        StringBuilder line = new StringBuilder();
        for (int col = 0; col < SIZE; col++) {
            // Add '|' if there is a vertical wall between col-1 and col
            if (col > 0) line.append(hasVerticalWall(board, row, col) ? '|' : ' ');
            // Add player number '1'/'2' or '.' if cell is empty
            line.append(getCellChar(row, col, players));
        }
        return line.toString();
    }

    // Builds one horizontal wall line between two rows
    private static String buildWallLine(int row, Board board) {
        StringBuilder line = new StringBuilder();
        for (int col = 0; col < SIZE; col++) {
            if (col > 0) line.append(" ");
            // Add '_' if there is a horizontal wall between row and row+1
            line.append(hasHorizontalWall(board, row, col) ? '_' : ' ');
        }
        return line.toString();
    }

    // Returns the character to display for a given cell
    // '1' for player 1, '2' for player 2, '.' for empty cell
    private static char getCellChar(int row, int col, List<Player> players) {
        for (int i = 0; i < players.size(); i++) {
            Position position = players.get(i).getPosition();
            if (position.getX() == row && position.getY() == col) {
                return (char) ('1' + i);
            }
        }
        return '.';
    }

    // Returns true if there is a vertical wall between col-1 and col
    private static boolean hasVerticalWall(Board board, int row, int col) {
        return !board.getGraph().hasEdge(board.id(row, col - 1), board.id(row, col));
    }

    // Returns true if there is a horizontal wall between row and row+1
    private static boolean hasHorizontalWall(Board board, int row, int col) {
        return !board.getGraph().hasEdge(board.id(row, col), board.id(row + 1, col));
    }

    // Writes the remaining walls for each player
    // Example : "walls: 10 10"
    private static void writeWalls(PrintWriter writer, GameState state) {
        StringBuilder line = new StringBuilder("walls:");
        for (Player player : state.getPlayers()) {
            line.append(" ").append(player.getRemainingWalls());
        }
        writer.println(line);
    }

    // Opens the file and reads all lines into a list
    private static List<String> readAllLines(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) lines.add(line);
            return lines;
        }
    }

    // Checks that the first line is "[game]"
    // If not, throws GameLoadException with error type and line number
    private static void validateHeader(List<String> lines, int currentLine) throws GameLoadException {
        if (!lines.get(currentLine).equals("[game]"))
            throw new GameLoadException("ERREUR_ENTETE", 1,
                    "Expected [game] but found : " + lines.get(currentLine));
    }

    // Checks that the first line is "[settings]"
    // If not, throws GameLoadException with error type and line number
    private static void validateSettingsHeader(List<String> lines) throws GameLoadException {
        if (!lines.get(0).equals("[settings] # Paramètres du programme"))
            throw new GameLoadException("ERREUR_ENTETE", 1,
                    "Expected [settings] but found : " + lines.get(0));
    }

    // Reads the current player index from line 2
    // Example : "2  # Current player color" , returns 1
    private static int readCurrentPlayer(List<String> lines, int currentLine) {
        return Integer.parseInt(lines.get(currentLine +1).trim().split("#")[0].trim()) - 1;
    }

    // Creates the 2 players with their default starting positions
    private static List<Player> createDefaultPlayers() {
        return new ArrayList<>(Arrays.asList(
                new HumanPlayer("Joueur1", new Position(0, 4)),
                new HumanPlayer("Joueur2", new Position(8, 4))
        ));
    }

    // Reads the full board, repositions players and places all walls
    private static void readBoard(List<String> lines, Board board, List<Player> players, int currentLine) {
        for (int row = 0; row < SIZE; row++) {
            readCellLine(lines.get(BOARD_START + currentLine + row * 2), row, board, players);
            if (row < SIZE - 1)
                readHorizontalWalls(lines.get(BOARD_START + currentLine + row * 2 + 1), row, board);
        }
    }

    // Reads one cell line : repositions players and places vertical walls
    private static void readCellLine(String line, int row, Board board, List<Player> players) {
        for (int col = 0; col < SIZE; col++) {
            // Each cell character is at index col*2 in the line
            char cell = line.charAt(col * 2);
            if (cell == '1') players.get(0).setPosition(new Position(row, col));
            else if (cell == '2') players.get(1).setPosition(new Position(row, col));
            if (col < SIZE - 1 && line.charAt(col * 2 + 1) == '|')
                board.getGraph().removeEdge(board.id(row, col), board.id(row, col + 1));
        }
    }

    // Reads one horizontal wall line and places horizontal walls on the board
    private static void readHorizontalWalls(String line, int row, Board board) {
        for (int col = 0; col < SIZE; col++)
            if (line.charAt(col * 2) == '_')
                board.getGraph().removeEdge(board.id(row, col), board.id(row + 1, col));
    }

    // Reads "walls: 10 10" and restores the correct number of walls for each player
    private static void readRemainingWalls(List<String> lines, List<Player> players, int currentLine) {
        String[] parts = lines.get(BOARD_START + currentLine + SIZE * 2 - 1)
                .replace("walls:", "").trim().split(" ");
        for (int i = 0; i < players.size(); i++) {
            int walls = Integer.parseInt(parts[i]);
            // Call useWall() until the player has the correct number of walls
            while (players.get(i).getRemainingWalls() > walls) players.get(i).useWall();
        }
    }

    private static void writeLine(PrintWriter writer, String key, Object value){
        writer.println(key + "=" + value);
    }


    private static void saveAiSettings(PrintWriter w, GameState g, AiPlayer new_p, int i){
        writeLine(w, "ai-player-"+i+"-name" , new_p.getName());    
        writeLine(w, "ai-player-"+i+"-starting-position" , new_p.getStartingPosition().toString());
        Map<String, Object> aiSettings = new_p.getSettings();
        for(String s : aiSettings.keySet()){
            writeLine(w, s, aiSettings.get(s));
        }
    }

    private static void saveParameters(PrintWriter w, GameState g){
            w.println("[settings] # Paramètres du programme");
            
            writeLine(w, "verbose", g.isVerbose());
            writeLine(w, "blitz", g.isBlitz());
            writeLine(w, "constest", g.isContest());
            writeLine(w, "ai", g.isAi());
            writeLine(w, "turns-limit", g.getTurnLimit());
            writeLine(w, "debug", g.isDebug());
            writeLine(w, "nb-players", g.getPlayers().size());

            int i = 1;
            for(Player p : g.getPlayers()){
                if(p.isAI()){
                    AiPlayer new_p = ((AiPlayer) p);
                    saveAiSettings(w, g, new_p, i);
                }
                else{

                    //writeLine(w, "player-"+i+"-remaining-time" ,  p.getRemainingTime());

                    writeLine(w, "player-"+i+"-name", p.getName());
                
                    writeLine(w, "player-"+i+"-starting-position" , p.getStartingPosition().toString());
                    
                }
                i++;
            }
            
    }

    
    private record PlayerAndLine(Player player, int nextLine) {} // structure to return the player and the last line read
    private record AllPlayerAndLine(List<Player> players, int nextLine) {} // structure to return the player and the last line read

    private static AllPlayerAndLine readAllPlayer(List<String> lines){
        List<Player> lPlayers = new ArrayList<>();
        int currentLine = 8; // 8 because its at the 9 lines that starts the informations about players
        while( currentLine < lines.size() && !lines.get(currentLine).equals("[game]")){
            PlayerAndLine res = readPlayer(lines, currentLine);
            lPlayers.add(res.player);
            currentLine = res.nextLine+1;
        }
        return new AllPlayerAndLine(lPlayers, currentLine);
    }

    private static PlayerAndLine readPlayer(List<String> lines, int currentLine){
        
        String p = lines.get(currentLine); 
        String p_type = p.split("=")[0].split("-")[0];
        String p_name = p.split("=")[1];
        p = lines.get(++currentLine); 
        Position p_postion = readPlayerStartPosition(p);
        

        if(p_type.equals("ai")){
            return readAiPlayer(lines, currentLine, p_name, p_postion);
        }
        else{
            return new PlayerAndLine(new HumanPlayer(p_name, p_postion), currentLine);
        }
    }

    private static Position readPlayerStartPosition(String p){
        String p_start_pos = p.split("=")[1];
        String p_start_pos_coord = p_start_pos.split(":")[1];
        int x = Integer.parseInt(p_start_pos_coord.split(",")[0].replaceAll(" ", "")); // Spaces must be removed because ParseInt does not handle them. 
        int y = Integer.parseInt(p_start_pos_coord.split(",")[1].replaceAll(" ", ""));
        return new Position(x, y);
    }

    private static PlayerAndLine readAiPlayer(List<String> lines, int currentLine, String p_name, Position p_postion){
        Map<String, String> aiSettings = new HashMap<>();
        while(checkStartByAi(lines, ++currentLine)){
            String[] ls = lines.get(currentLine).split("=");
            String[] aiParameter = ls[0].split("-");
            aiSettings.put(aiParameter[aiParameter.length -1], ls[1]);
        }

        switch (aiSettings.get("mode")) {
            case "minimax": {
                String heuristicName = aiSettings.get("heuristic");
                int depth = Integer.parseInt(aiSettings.get("depth"));
                int reflexion_time = Integer.parseInt(aiSettings.get("reflexion"));
                return new PlayerAndLine(MinimaxPlayer.builder(p_name, p_postion).withHeuristic(heuristicName).withDepth(depth).withReflexion(reflexion_time).build(), currentLine);
            }
            case "mcts":
                
                return new PlayerAndLine(new MCTSPlayer(p_name, p_postion), currentLine) ;

            case "neural-network":
                
                return new PlayerAndLine(new NeuralNetPlayer(p_name, p_postion), currentLine) ;

            case "random":
                
                return new PlayerAndLine(new RandomPlayer(p_name, p_postion), currentLine);

            case "minimax-iterative-deepening":{                    
                String heuristicName = aiSettings.get("heuristic");
                int reflexion_time = Integer.parseInt(aiSettings.get("reflexion"));
                return new PlayerAndLine(MinimaxPlayer.builder(p_name, p_postion).withHeuristic(heuristicName).withReflexion(reflexion_time).isIterativeDeepening(true).build(), currentLine) ;
            }
            default:
                throw new IllegalArgumentException("Unknow Ai Type : " + aiSettings.get("mode"));    
                
        }
    }

    private static boolean checkStartByAi(List<String> lines, int currentLine){
        return lines.get(currentLine).startsWith("ai");
    }
}