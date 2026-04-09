package fr.bordeaux.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Provides internationalization support for the application. Manages resource bundles, locale
 * settings, and translated UI strings.
 */
public final class I18n {
  /** Base path of the translation bundles. */
  private static final String BUNDLE_NAME = "international/messages";

  /** Currently loaded translation bundle. */
  private static ResourceBundle bundle;

  /** Initializes the default locale used by the application. */
  static {
    if (System.getProperty("quoridor.test") != null) {
      setLocale(Locale.ENGLISH);
    } else {
      setLocale(Locale.getDefault());
    }
  }

  /**
   * Set the language that will be used
   *
   * @param locale the location to set the language
   */
  public static void setLocale(final Locale locale) {
    bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
  }

  /**
   * Get a simple message
   *
   * @param key the text to display from properties
   * @return text depending on the language choosen
   */
  public static String get(final String key) {
    return bundle.getString(key);
  }

  /**
   * Get a dynamic message.
   *
   * @param key the text to display from properties
   * @param params more text
   * @return text depending on the language choosen
   */
  public static String get(final String key, final Object... params) {
    final String pattern = bundle.getString(key);
    final String message;
    if (params.length > 0 && pattern.contains("{0}")) {
      message = java.text.MessageFormat.format(pattern, params);
    } else {
      final StringBuilder messageBuilder = new StringBuilder(pattern);
      for (final Object param : params) {
        messageBuilder.append(' ').append(param);
      }
      message = messageBuilder.toString();
    }
    return message;
  }

  /** Constructs a new I18n manager. */
  private I18n() {}
}
