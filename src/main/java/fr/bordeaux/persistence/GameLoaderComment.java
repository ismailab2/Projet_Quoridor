package fr.bordeaux.persistence;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class used to read a save file while handling comments.
 *
 * <p>This class processes a file and removes:
 *
 * <ul>
 *   <li>Inline comments starting with '#' (ignored until end of line)
 *   <li>Block comments delimited by '{' and '}' (can span multiple lines)
 *   <li>Empty or whitespace-only lines
 * </ul>
 *
 * <p>The returned lines are cleaned and ready to be parsed safely.
 */
public final class GameLoaderComment {
  /** Private constructor to prevent instantiation. */
  private GameLoaderComment() {}

  /**
   * Reads a file and returns a list of cleaned lines with all comments removed.
   *
   * <p>This method supports:
   *
   * <ul>
   *   <li>Inline comments: everything after '#' is ignored
   *   <li>Block comments: everything between '{' and '}' is ignored, even across multiple lines
   * </ul>
   *
   * @param filename the path to the file to read
   * @return a list of cleaned, non-empty lines
   * @throws IOException if an error occurs while reading the file
   */
  public static List<String> readCleanLines(String filename) throws IOException {
    List<String> result = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
      String line;
      boolean inBlockComment = false;
      boolean inGameSection = false;

      while ((line = reader.readLine()) != null) {
        StringBuilder cleanLine = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
          char currentChar = line.charAt(i);
          // If currently inside a block comment, ignore everything
          if (inBlockComment) {
            if (currentChar == '}') {
              inBlockComment = false;
            }
            continue;
          }
          if (currentChar == '{') {
            inBlockComment = true;
            continue;
          }
          if (currentChar == '#') {
            break;
          }
          cleanLine.append(currentChar);
        }

        String rawCleaned = cleanLine.toString();
        String cleaned = rawCleaned.trim();

        if ("[game]".equals(cleaned)) {
          inGameSection = true;
          result.add(cleaned);
          continue;
        }

        if ("[History]".equals(cleaned)) {
          inGameSection = false;
          result.add(cleaned);
          continue;
        }

        if (inGameSection) {
          if (!cleaned.isEmpty() || rawCleaned.length() > 0) {
            result.add(rawCleaned);
          }
        } else if (!cleaned.isEmpty()) {
          result.add(cleaned);
        }
      }
    }

    return result;
  }
}
