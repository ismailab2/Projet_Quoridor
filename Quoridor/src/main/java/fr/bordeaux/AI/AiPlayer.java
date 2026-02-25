package fr.bordeaux.AI;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import fr.bordeaux.core.Board;
import fr.bordeaux.core.GameState;
import fr.bordeaux.core.Move;
import fr.bordeaux.core.Player;
import fr.bordeaux.core.Position;

public class AiPlayer { // MiniMax and IterativeDeepening are limited because it works with only 2 players. They will not work if the number of players is larger.


    private int reflexionTime = 5; // In seconds
    private final ScheduledExecutorService reflexionTimer = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> cleaner;
    private int d;
    private int currentTurn = 0;
    private Supplier<Move> mode ;
    private GameState g;
    private volatile Move saveMove = null;

    public AiPlayer(GameState g, int d, int reflexionTime, String selectedMode){
        this.g = g;
        this.reflexionTime = reflexionTime;
        this.d = d;
        if (g.getPlayers().size != 2) {
            throw new IllegalArgumentException(
                " IA can only be used in a 1v1");
        }
        if(selectedMode.equals("Iterative-Deepening")){
            mode = () -> IterativeDeepening();
        }
        else if(selectedMode.equals("Random")){
            mode = () -> randomMove();
        }
        else{
            mode = () -> MiniMax(this.d);
        }
    }

    public AiPlayer(GameState g, int d){
        this(g, d, 5, null);
    }

    public AiPlayer(GameState g, String selectedMode){
        this(g, 1, 5, selectedMode);
    }

    public void play(){
        this.currentTurn++;
        int idTurn = this.currentTurn;

        if(cleaner != null && !cleaner.isDone()){
            cleaner.cancel(false);
        }

        CompletableFuture<Move> move = CompletableFuture.supplyAsync(() -> mode.get() );

        cleaner = reflexionTimer.schedule(() ->{
            if(idTurn == currentTurn){
                move.cancel(true);
            }
        }, reflexionTime, TimeUnit.SECONDS);

        try{
            Move play_move = move.get(reflexionTime*1000 + 100, TimeUnit.MILLISECONDS); // Je rajoute un peu plus de temps question de sécurité pour eviter que les 2 threads se marche dessus

            g.applyMove(play_move);

            cleaner.cancel(false); // Nettoyer le timer 

        }
        catch (Exception e){
            if(saveMove == null){
                g.applyMove(randomMove(g));
            }
            else{
                g.applyMove(saveMove);
            }
        }

    }

    public Move IterativeDeepening(){
        int depth = 1;
        saveMove = null;
        while (!Thread.currentThread().isInterrupted()) {

            Move temp = MiniMax(depth);
            if (temp == null) {
                break; 
            }

            saveMove = temp;
            depth++;
        }

        return saveMove;
    }


    public Move MiniMax(int depth){
        Move best_move = null;
        int best_score = Integer.MIN_VALUE;
        for (Move s : g.generate_legal_moves()){
            g.push(s);
            int alpha = Math.max(best_score, MinMax(g, depth-1, Integer.MIN_VALUE ,Integer.MAX_VALUE));
            if(alpha > best_score){
                best_score = alpha;
                best_move = s;
            }
            g.pop();
        }

        return best_move;
    }



    private int MaxMin(GameState g,int d, int alpha, int beta){

        if(g.isGameOver() || d==0 || Thread.currentThread().isInterrupted()){
            return heuristic(g);
        }

        for (Move s : g.generate_legal_moves()){
            g.push(s);
            alpha = Math.max(alpha, MinMax(g, d-1, alpha ,beta));
            g.pop();
            if(alpha>=beta){
                return beta;
            }
        }
        return alpha;
    }



    private int MinMax(GameState g,int d, int alpha, int beta){

        if(g.isGameOver() || d==0 || Thread.currentThread().isInterrupted()){
            return heuristic(g);
        }

        for (Move s : g.generate_legal_moves()){
            g.push(s);
            beta = Math.min(beta, MaxMin(g, d-1, alpha ,beta));
            g.pop();
            if(alpha>=beta){
                return alpha;
            }
        }
        return beta;
    }


    public Move randomMove(GameState g){ // It is not planned to be able to launch it in the game, it will just be used for testing
        List<Move> l = g.generate_legal_moves();
        Random random = new Random();
        return l.get( random.nextInt(l.size()));
    }

    // Simple heuristic that count the difference between the column current position and the column start position. 
    // If the player start at the first row, this heuristic compare row instead of columns
    private int h1(GameState g){  
        Player player = g.currentPlayer();
        Position startingPosition = player.getStartingPosition();
        Position actualPosition = player.getPosition();

        if(startingPosition.getX() == 0){
            return actualPosition.getX() - startingPosition.getX();
        }
        else{
            return actualPosition.getY() - startingPosition.getY();
        }
        
    }

    // Very similar than the previous one but now we also make importance at the enemie position
    private int h2(GameState g){  
        Player player = g.currentPlayer();
        List<Player> lp = g.getPlayers();
        Player enemie = null;
        for (Player p : lp){
            if(p != player){   // PAS SUR QUE LA COMPARAISON ENTRE PLAYER SOIT IDEAL
                enemie = p;
                break;
            }

        }

        Position playerStartingPosition = player.getStartingPosition();
        Position playerActualPosition = player.getPosition();
        if( enemie == null){
            throw new IllegalStateException("No enemie found !");
        }
        Position enemieStartingPosition = enemie.getStartingPosition();
        Position enemieActualPosition = enemie.getPosition();

        if(startingPosition.getX() == 0){
            return (playerActualPosition.getX() - playerStartingPosition.getX()) - (enemieActualPosition.getX() - enemieStartingPosition.getX());
        }
        else{
            return (playerActualPosition.getY() - playerStartingPosition.getY()) - (enemieActualPosition.getY() - enemieStartingPosition.getY());
        }
        
    }

    private int h3(GameState g){
        // K1 and K2 are the coefficients of wall remaining fot the player and his enemie, at the end of the project we will use a neuronal network to determine them.
        float k1 = 1.2f;
        float k2 = 0.8f;


        List<Integer> l_max = g.getBoard().findPath( g.currentPlayer().getPosition(), g.getWinCondition(g.currentPlayer()));

        List<Player> lp = g.getPlayers();
        Player enemie = null;
        for (Player p : lp){
            if(p != player){   // PAS SUR QUE LA COMPARAISON ENTRE PLAYER SOIT IDEAL
                enemie = p;
                break;
            }

        }

        List<Integer> l_min = g.getBoard().findPath( enemie.getPosition(), g.getWinCondition(enemie));

        int nb_wall_player = g.currentPlayer().getRemainingWalls();
        int nb_wall_enemie = enemie.getRemainingWalls();

        return (int) l_min.size() - l_max.size() + (nb_wall_player * k1 - nb_wall_enemie * k2);
    }

}
