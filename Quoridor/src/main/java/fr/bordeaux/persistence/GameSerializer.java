package fr.bordeaux.persistence;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import fr.bordeaux.AI.AiPlayer;
import fr.bordeaux.AI.MinimaxPlayer;
import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Player;

public class GameSerializer {

    public void saveParameters(String filePath, GameState g){
        try(BufferedWriter w = new BufferedWriter(new FileWriter(filePath))){
            w.write("[settings] # Paramètres du programme");
            w.newLine();
            String mode = g.getMode();
            writeLine(w, "mode", mode);
            writeLine(w, "turns-limit", g.getTurnLimit());
            writeLine(w, "debug", g.isDebug());
            writeLine(w, "nb-players", g.getPlayers().size());

            int i = 1;
            for(Player p : g.getPlayers()){
                if(p.isAI()){
                    AiPlayer new_p = ((AiPlayer) p);
                    writeLine(w, "ai"+i+"-mode", new_p.getAiMode());
                    if("minimax".equals(new_p.getAiMode()) || "iterative-deepening".equals(new_p.getAiMode())){
                        MinimaxPlayer mini = (MinimaxPlayer) new_p;
                        writeLine(w, "ai"+i+"-heuristic", mini.getHeuristic());
                        if ("minimax".equals(new_p.getAiMode())) {
                            writeLine(w, "ai"+i+"-depth", mini.getDepth());
                        }
                    }
                    writeLine(w, "ai"+i+"-reflexionTime", new_p.getReflexionTime());
                    if(mode.equals("blitz")){
                        writeLine(w, "ai-player"+i+"-remaining-time" , new_p.getRemainingTime());
                    }
                   writeLine(w, "ai-player"+i+"-name" + new_p.getName());
                    
                   writeLine(w, "ai-player"+i+"-starting-position" , new_p.getStartingPosition());
                    
                }
                else{
                    if(mode.equals("blitz")){
                       writeLine(w, "player"+i+"-remaining-time" ,  p.getRemainingTime());
                        
                    }
                    
                   writeLine(w, "player"+i+"-name", p.getName());
                    

                   writeLine(w, "player"+i+"-starting-position" , p.getStartingPosition());
                    
                }
                i++;
            }
            
        }
        catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde des parametres du fichier : " + e.getMessage());
        }
    }




    private void writeLine(BufferedWriter w, String key, Object value) throws IOException {
        w.write(key + "=" + value);
        w.newLine();
    }

}
