package fr.bordeaux.util;

import java.util.concurrent.TimeUnit;

/** Time conversion utility for Quoridor game timers. */
public final class TimeConvertor {

  /** Private constructor to prevent instantiation of this utility class. */
  private TimeConvertor() {}

  /**
   * Formats duration in milliseconds to "MM : SS" or "SS : ms".
   *
   * @param timeMilli duration to format in milliseconds.
   * @return a formatted {@link String} representing the time.
   */
  public static String formatTime(final long timeMilli) {

    final long nbMin = TimeUnit.MILLISECONDS.toMinutes(timeMilli);
    final long nbSeconds =
        TimeUnit.MILLISECONDS.toSeconds(timeMilli) - TimeUnit.MINUTES.toSeconds(nbMin);
    final long nbMillis =
        timeMilli - TimeUnit.SECONDS.toMillis(nbSeconds) - TimeUnit.MINUTES.toMillis(nbMin);

    String result;

    if (timeMilli <= 0) {
      result = "00 : 00";
    } else if (nbMin <= 0) {
      result = String.format("%02d : %02d", nbSeconds, nbMillis / 10);
    } else {
      result = String.format("%02d : %02d", nbMin, nbSeconds);
    }

    return result;
  }
}
