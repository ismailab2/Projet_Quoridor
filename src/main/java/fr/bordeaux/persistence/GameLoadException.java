package fr.bordeaux.persistence;

/** Exception thrown when an error occurs while loading a game. */
public final class GameLoadException extends Exception {

  /** Stores the type of error, for example: "ERREUR_ENTETE". */
  private final String errorType;

  /** Stores the line number where the error was found. */
  private final int lineNumber;

  /**
   * Creates a load exception with the error type, the line number, and the associated message.
   *
   * @param errorTypeValue type of detected error
   * @param lineNumberValue line number where the error was found
   * @param message detailed error message
   */
  public GameLoadException(
      final String errorTypeValue, final int lineNumberValue, final String message) {
    super(message);
    this.errorType = errorTypeValue;
    this.lineNumber = lineNumberValue;
  }

  /**
   * Returns the error type.
   *
   * @return the error type
   */
  public String getErrorType() {
    return errorType;
  }

  /**
   * Returns the line number where the error was found.
   *
   * @return the line number
   */
  public int getLineNumber() {
    return lineNumber;
  }
}
