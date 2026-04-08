package fr.bordeaux.main;

import fr.bordeaux.config.ConfigManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Handles AI-specific command line arguments and validation. */
public final class AiOptionsHandler {

  private AiOptionsHandler() {}

  /**
   * Processes AI-related arguments.
   *
   * @param arg    The current argument.
   * @param args   The full array of arguments.
   * @param index  The current index in the array.
   * @param config The configuration manager.
   * @return The new index after consuming arguments, or -1 if the argument is not handled here.
   */
  public static int handle(final String arg, final String[] args, final int index, final ConfigManager config) {
    int i = index;
    switch (arg) {
      case "-a", "--ai" -> {
        String aiOption = "";
        if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
          aiOption = args[++i].toLowerCase(Locale.ROOT);
        }
        if (aiOption.isEmpty()) {
          config.setOption("ai2", "true");
        } else if ("a".equals(aiOption)) {
          config.setOption("ai1", "true");
          config.setOption("ai2", "true");
        } else if ("blanc".equals(aiOption) || "1".equals(aiOption)) {
          config.setOption("ai1", "true");
        } else if ("noir".equals(aiOption) || "2".equals(aiOption)) {
          config.setOption("ai2", "true");
        } else {
          throw new IllegalArgumentException(
              "--ai value must be: {blanc, noir, 1, 2, a} or empty (defaults to noir)");
        }
      }
      case "--ai-mode" -> {
        checkAiString(args[++i], "mode", new ArrayList<>(List.of("minimax", "mcts", "iterative")));
        applyAiOption(args[i], "mode", config);
      }
      case "--ai-minimax-depth" -> {
        checkAiInt(args[++i], "minimax-depth");
        applyAiOption(args[i], "depth", config);
      }
      case "--ai-time" -> {
        checkAiInt(args[++i], "time");
        applyAiOption(args[i], "time", config);
      }
      case "--ai-minimax-scoring" -> {
        checkAiString(args[++i], "scoring", new ArrayList<>(List.of("simple", "intermediate", "advanced")));
        applyAiOption(args[i], "scoring", config);
      }
      case "--ai-mcts-selection" -> applyMctsOption(args[++i], config);
      default -> {
        return -1; // Not a game option
      }
    }
    return i;
  }


  /**
   * Validates that AI is only enabled for 2-player games.
   *
   * @param config The configuration manager instance.
   * @throws IllegalArgumentException If AI is active with more than 2 players.
   */
  public static void validateAiPlayerCount(final ConfigManager config) {
    final int nbPlayers = Integer.parseInt(config.getOption("nb-players", "2"));
    final boolean hasAi = config.getBoolean("ai1", false) || config.getBoolean("ai2", false);

    if (hasAi && nbPlayers > 2) {
      throw new IllegalArgumentException(
          "AI mode is not supported for more than 2 players (currently: " + nbPlayers + ")");
    }
  }

  /**
   * Validates that a comma-separated string contains only positive integers.
   *
   * @param value The raw string to validate (e.g., "3,5").
   * @param parameter The parameter name for the error message.
   * @throws IllegalArgumentException If value is null or not a positive integer.
   */
  private static void checkAiInt(final String value, final String parameter) {
    if (value == null) {
      throw new IllegalArgumentException("AI " + parameter + " require a number");
    }
    final String[] values = value.split(",");
    for (final String val : values) {
      if (Integer.parseInt(val) <= 0) {
        throw new IllegalArgumentException("--ai-" + parameter + " should be higher than 0");
      }
    }
  }

  /**
   * Validates that a comma-separated string contains only allowed keywords.
   *
   * @param value The raw string to validate (e.g., "minimax,mcts,iterative").
   * @param parameter The parameter name for the error message.
   * @param accpetedMode List of permitted string values.
   * @throws IllegalArgumentException If value is null or contains an invalid mode.
   */
  private static void checkAiString(
      final String value, final String parameter, final List<String> accpetedMode) {
    if (value == null) {
      throw new IllegalArgumentException(
          "AI " + parameter + " require an argument in " + accpetedMode);
    }
    final String[] values = value.split(",");
    for (final String val : values) {
      if (!accpetedMode.contains(val.toLowerCase())) {
        throw new IllegalArgumentException(
            "The only valid AI " + parameter + " are " + accpetedMode);
      }
    }
  }

  /** Sets AI options only for players currently flagged as AI. */
  private static void applyAiOption(
      final String value, final String suffix, final ConfigManager config) {
    final String[] values = value.split(",");

    // Apply to Player 1 if AI is enabled
    if (config.getBoolean("ai1", false)) {
      config.setOption("ai1." + suffix, values[0].trim());
    }

    // Apply to Player 2 if AI is enabled
    if (config.getBoolean("ai2", false)) {
      // Use the second value if provided, otherwise fallback to the first
      final String valToSet = (values.length > 1) ? values[1] : values[0];
      config.setOption("ai2." + suffix, valToSet.trim());
    }
  }

  /** Specialized helper for MCTS selection with active AI check. */
  private static void applyMctsOption(final String value, final ConfigManager config) {
    final String[] values = value.split(",");
    for (final String v : values) {
      final String trimmed = v.trim();
      if (!"UCT".equals(trimmed) && !"ML".equals(trimmed)) {
        throw new IllegalArgumentException("Invalid option: " + trimmed + " for MCTS selection.");
      }
    }

    if (config.getBoolean("ai1", false)) {
      config.setOption("ai1.selection", values[0].trim());
    }
    if (config.getBoolean("ai2", false)) {
      final String valToSet = (values.length > 1) ? values[1] : values[0];
      config.setOption("ai2.selection", valToSet.trim());
    }
  }
}