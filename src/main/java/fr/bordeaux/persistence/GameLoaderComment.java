package fr.bordeaux.persistence;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to read a save file while handling comments.
 *
 * <p>Removes inline comments starting with '#', block comments delimited by '{' and '}', and empty
 * lines.
 */
public final class GameLoaderComment {
  /** Private constructor to prevent instantiation. */
  private GameLoaderComment() {}

  /**
   * Reads a file and returns a list of cleaned lines.
   *
   * <p>Supports inline ('#') and block ('{', '}') comments.
   *
   * @param filename the path to the file to read
   * @return a list of cleaned, non-empty lines
   * @throws IOException if an error occurs while reading the file
   */
  public static List<String> readCleanLines(final String filename) throws IOException {
    final List<String> result = new ArrayList<>();
    try (BufferedReader reader =
        Files.newBufferedReader(Paths.get(filename), StandardCharsets.UTF_8)) {
      String line;
      final boolean[] inComment = {false};
      boolean inGame = false;

      while ((line = reader.readLine()) != null) {
        final String rawCleaned = cleanLine(line, inComment);
        final String cleaned = rawCleaned.trim();

        if ("[game]".equals(cleaned)) {
          inGame = true;
          result.add(cleaned);
        } else if ("[History]".equals(cleaned)) {
          inGame = false;
          result.add(cleaned);
        } else if (inGame) {
          if (!cleaned.isEmpty() || !rawCleaned.isEmpty()) {
            result.add(rawCleaned);
          }
        } else if (!cleaned.isEmpty()) {
          result.add(cleaned);
        }
      }
    }
    return result;
  }

  private static String cleanLine(final String line, final boolean[] inComment) {
    final StringBuilder builder = new StringBuilder();
    for (int i = 0; i < line.length(); i++) {
      final char chr = line.charAt(i);
      if (inComment[0]) {
        if (chr == '}') {
          inComment[0] = false;
        }
        continue;
      }
      if (chr == '{') {
        inComment[0] = true;
        continue;
      }
      if (chr == '#') {
        break;
      }
      builder.append(chr);
    }
    return builder.toString();
  }
}
