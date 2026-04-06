package fr.bordeaux.main;

import fr.bordeaux.ai.mcts.MctsPlayer;
import fr.bordeaux.config.ConfigManager;
import fr.bordeaux.core.*;
import fr.bordeaux.persistence.GameLoadException;
import fr.bordeaux.persistence.GameLoader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Launcher for contest mode. Reads a save file, computes best move via MCTS, and prints it in
 * algebraic notation.
 */
@SuppressWarnings("PMD.SystemPrintln")
public final class ContestLauncher implements AppLauncher {

  ContestLauncher() {
    // Hidden constructor
  }

  @Override
  public void launch(final String[] args, final ConfigManager config) {
    final String filePath = config.getContestFile();

    if (filePath == null || filePath.isBlank()) {
      System.err.println("[contest] No position file specified. Use: --contest <file>");
    }

    assert filePath != null;
    if (!Files.exists(Path.of(filePath))) {
      System.err.println("[contest] File not found: " + filePath);
    }

    try {
      final GameEngine engine = createDummyEngine(config);
      final GameState state = GameLoader.loadGame(engine, filePath);
      final Move best = computeBestMove(state, config);
      System.out.println(moveToString(best));
    } catch (final IOException e) {
      System.err.println("[contest] I/O error reading file: " + e.getMessage());
      System.exit(1);
    } catch (final GameLoadException e) {
      System.err.println(
          "[contest] Invalid save file ["
              + e.getErrorType()
              + "] at line "
              + e.getLineNumber()
              + ": "
              + e.getMessage());
    } catch (final Exception e) {
      System.err.println("[contest] Unexpected error: " + e.getMessage());
    }
  }

  /**
   * @return placeholder engine for GameLoader.
   */
  private static GameEngine createDummyEngine(final ConfigManager config) {
    final Board dummyBoard = new Board(9);
    final List<Player> dummyPlayers =
        List.of(
            new HumanPlayer("P1", new Position(0, 4), Color.WHITE),
            new HumanPlayer("P2", new Position(8, 4), Color.BLACK));
    final GameState dummyState = new GameState(dummyBoard, dummyPlayers, 0);

    return new GameEngine(
        dummyState, false, false, true, false, config.getInt("initial-walls", 10));
  }

  private static Move computeBestMove(final GameState state, final ConfigManager config) {
    final long timeBudgetMs = config.getInt("ai1.time", 3) * 1000L;

    try {
      return MctsPlayer.hint(state.getCurrentPlayer(), state, timeBudgetMs, false);
    } catch (final Exception e) {
      System.err.println(
          "[contest] MCTS failed (" + e.getMessage() + "), falling back to first legal move.");
      final List<Move> legal = state.generateLegalMoves();
      if (legal.isEmpty()) {
        throw new IllegalStateException("No legal moves available.", e);
      }
      return legal.get(0);
    }
  }

  private static String moveToString(final Move move) {
    if (move.getType() == Move.Type.PAWN) {
      return posToString(move.getFrom()) + "-" + posToString(move.getTo());
    }
    final char orientation = move.getOrientation() == Orientation.HORIZONTAL ? 'h' : 'v';
    return posToString(move.getTo()) + orientation;
  }

  private static String posToString(final Position p) {
    final char letter = (char) ('a' + p.getX());
    return String.valueOf(letter) + p.getY();
  }
}
