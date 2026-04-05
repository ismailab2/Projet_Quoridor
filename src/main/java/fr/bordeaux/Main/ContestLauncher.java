package fr.bordeaux.Main;

import fr.bordeaux.ai.MctsPlayer;
import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.core.*;
import fr.bordeaux.persistence.GameLoadException;
import fr.bordeaux.persistence.GameLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Launcher for contest mode, integrated with the custom AppLauncher interface.
 *
 * <p>Reads a game position from a save file (standard persistence format),
 * computes the best move for the current player using MCTS, and prints it
 * in algebraic notation.
 */
public final class ContestLauncher implements AppLauncher {

    ContestLauncher() {}

    @Override
    public void launch(String[] args, ConfigManager config) {
        String filePath = config.getContestFile();

        if (filePath == null || filePath.isBlank()) {
            System.err.println("[contest] No position file specified. Use: --contest <file>");
        }

        assert filePath != null;
        if (!Files.exists(Path.of(filePath))) {
            System.err.println("[contest] File not found: " + filePath);
        }

        try {
            GameEngine engine = createDummyEngine(config);
            GameState state = GameLoader.loadGame(engine, filePath);
            Move best = computeBestMove(state, config);
            System.out.println(moveToString(best));
        } catch (IOException e) {
            System.err.println("[contest] I/O error reading file: " + e.getMessage());
            System.exit(1);
        } catch (GameLoadException e) {
            System.err.println("[contest] Invalid save file ["
                    + e.getErrorType() + "] at line " + e.getLineNumber()
                    + ": " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[contest] Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Creates a minimal GameEngine with a placeholder state, used solely to
     * satisfy GameLoader.loadGame(). The real state is rebuilt by GameLoader
     * and returned independently — this engine's state is never used.
     */
    private static GameEngine createDummyEngine(ConfigManager config) {
        Board dummyBoard = new Board(9);
        List<Player> dummyPlayers = List.of(
                new HumanPlayer("P1", new Position(0, 4), Color.WHITE),
                new HumanPlayer("P2", new Position(8, 4), Color.BLACK)
        );
        GameState dummyState = new GameState(dummyBoard, dummyPlayers, 0);

        return new GameEngine(
                dummyState,
                false,
                false,
                true,
                false,
                config.getInt("initial-walls", 10)
        );
    }

    private static Move computeBestMove(GameState state, ConfigManager config) {
        long timeBudgetMs = (long) config.getInt("ai1.time", 3) * 1000L;

        try {
            return MctsPlayer.hint(state.getCurrentPlayer(), state, timeBudgetMs, false);
        } catch (Exception e) {
            System.err.println("[contest] MCTS failed (" + e.getMessage()
                    + "), falling back to first legal move.");
            List<Move> legal = state.generateLegalMoves();
            if (legal.isEmpty())
                throw new IllegalStateException("No legal moves available.");
            return legal.get(0);
        }
    }

    private static String moveToString(Move move) {
        if (move.getType() == Move.Type.PAWN)
            return posToString(move.getFrom()) + "-" + posToString(move.getTo());
        char orientation = move.getOrientation() == Orientation.HORIZONTAL ? 'h' : 'v';
        return posToString(move.getTo()) + orientation;
    }

    private static String posToString(Position p) {
        char letter = (char) ('a' + p.getX());
        return "" + letter + p.getY();
    }
}