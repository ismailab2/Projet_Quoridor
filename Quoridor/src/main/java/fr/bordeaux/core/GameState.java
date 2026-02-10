package fr.bordeaux.core;

import java.util.List;

/**
 * Represents the state of a Quoridor game. Manages the board, players, current player, and move
 * history. Undo/Redo functionality is implemented using stacks.
 */
public class GameState {
    private final Board board;
    private final List<Player> players;
    private int currentPlayer = 0;

    public GameState(Board board, List<Player> players) {
        if (players.size() != 2 && players.size() != 4) {
            throw new IllegalArgumentException(
                    "Number of players must be 2 or 4, but was : " + players.size());
        }
        this.board = board;
        this.players = players;
    }

    public void applyMove(Move move) {
        if (move.isPawn()) {
            currentPlayer().setPosition(move.getTo());
        } else {
            board.addWall(move.getTo().getX(), move.getTo().getY(), move.getOrientation());
            currentPlayer().useWall();
        }
        nextPlayer();
    }

    public boolean isGameOver() {
        for (Player player : players) {
            if (hasReachedGoal(player)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasReachedGoal(Player player) {
        Position startingPosition = player.getStartingPosition();
        Position actualPosition = player.getPosition();
        // Start first line, middle column
        if (startingPosition.getX() == 0) {
            return actualPosition.getX() == board.getSize() - 1;
        }
        // Start last line, middle column
        else if (startingPosition.getX() == board.getSize() - 1) {
            return actualPosition.getX() == 0;
        }
        // Start first column, middle line
        else if (startingPosition.getY() == 0) {
            return actualPosition.getY() == board.getSize() - 1;
        }
        // Start last column, middle line
        else {
            return actualPosition.getY() == 0;
        }
    }

    public Player currentPlayer() {
        return players.get(currentPlayer);
    }

    public void nextPlayer() {
        currentPlayer = (currentPlayer + 1) % players.size();
    }

    public Board getBoard() {
        return board;
    }

    public List<Player> getPlayers() {
        return players;
    }
}
