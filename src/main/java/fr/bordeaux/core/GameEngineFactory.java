package fr.bordeaux.core;

import fr.bordeaux.Main.PlayerFactory;
import fr.bordeaux.config.ConfigManager;

import java.util.List;

/**
 * Factory used to create and configure a GameEngine.
 */
public class GameEngineFactory {

    /** Default constructor. */
    public GameEngineFactory() {}

    /**
     * Creates a GameEngine from the given configuration.
     *
     * @param configManager configuration manager
     * @return an initialized GameEngine
     */
    public static GameEngine createGameEngine(ConfigManager configManager) {
        int boardSize = configManager.getInt("board-size", 9);
        int nbWalls = configManager.getInt("nb-walls", 10);
        boolean blitz = configManager.getBoolean("blitz", false);

        Board board = new Board(boardSize);
        List<Player> players = PlayerFactory.createPlayers(configManager);
        GameState state = new GameState(board, players);

        if (blitz) {
            int timeout = configManager.getInt("timeout", 30);
            state.setLimitTimePlayer(timeout * 60 * 1000L);
        }

        GameEngine engine = new GameEngine(
                state,
                blitz,
                configManager.getBoolean("verbose", false),
                configManager.getBoolean("contest", false),
                configManager.getBoolean("debug", false),
                nbWalls
        );

        initializeWalls(engine);
        return engine;
    }

    /**
     * Initializes the number of walls for each player.
     *
     * @param engine game engine
     */
    private static void initializeWalls(GameEngine engine) {
        for (Player p : engine.getState().getPlayers()) {
            p.setRemainingWalls(engine.getInitialWalls());
        }
    }
}