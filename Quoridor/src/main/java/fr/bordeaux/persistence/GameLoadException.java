package fr.bordeaux.persistence;

public class GameLoadException extends Exception {

    // Stores the type of error ex: "ERREUR_ENTETE"
    private final String errorType;

    // Stores the line number where the error was found
    private final int lineNumber;

    // Constructor : when we create the error we give 3 pieces of info
    public GameLoadException(String errorType, int lineNumber, String message) {
        super(message); // pass the message to the parent Exception class
        this.errorType = errorType;
        this.lineNumber = lineNumber;
    }

    // Allows us to get the error type from outside
    public String getErrorType() { return errorType; }

    // Allows us to get the line number from outside
    public int getLineNumber() { return lineNumber; }
}