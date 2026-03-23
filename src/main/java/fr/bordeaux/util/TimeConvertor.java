package fr.bordeaux.util;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for time format conversion in the Quoridor game.
 * Provides methods to transform millisecond values into human-readable strings
 * adapted for game timers (Blitz mode).
 */
public final class TimeConvertor {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private TimeConvertor(){}


    /**
     * Formats a duration in milliseconds into a specific string format based on the remaining time.
     * * <p>The formatting rules are as follows:
     * <ul>
     * <li>If {@code timeMilli <= 0}: returns "00:000".</li>
     * <li>If the duration is less than a minute: returns "Seconds : Milliseconds" (e.g., "45 : 123").</li>
     * <li>If the duration is one minute or more: returns "Minutes : Seconds" (e.g., "12 : 34").</li>
     * </ul>
     *
     * @param timeMilli the duration to format in milliseconds.
     * @return a formatted {@link String} representing the time.
     */
    public static String formatTime(final long timeMilli){
        

        final long nbMin = TimeUnit.MILLISECONDS.toMinutes(timeMilli);
        final long nbSeconds = TimeUnit.MILLISECONDS.toSeconds(timeMilli) - TimeUnit.MINUTES.toSeconds(nbMin);
        final long nbMillis = timeMilli - TimeUnit.SECONDS.toMillis(nbSeconds) - TimeUnit.MINUTES.toMillis(nbMin);

        String result;

        if(timeMilli <=0){
            result = "00 : 000";
        }
        else if(nbMin <= 0){
            result = String.format("%02d : %03d", nbSeconds, nbMillis);
        }
        else {
            result = String.format("%02d : %02d", nbMin, nbSeconds);
        }

        return result;
    }
}
