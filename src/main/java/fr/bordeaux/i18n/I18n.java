package fr.bordeaux.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Provides internationalization (I18n) support for the application. This class is responsible for
 * managing resource bundles, locale settings, and translating UI strings to support multiple
 * languages.
 */
public final class I18n {
  private static final String BUNDLE_NAME = "international/messages";
  private static ResourceBundle bundle;
  private static Locale currentLocale;

  // Default param
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
  public static void setLocale(Locale locale) {
    currentLocale = locale;
    bundle = ResourceBundle.getBundle(BUNDLE_NAME, currentLocale);
  }

  /**
   * Get a simple message
   *
   * @param key the text to display from properties
   * @return text depending on the language choosen
   */
  public static String get(String key) {
    return bundle.getString(key);
  }

  /**
   * Get a dynamic message.
   *
   * @param key the text to display from properties
   * @param params more text
   * @return text depending on the language choosen
   */
  public static String get(String key, Object... params) {
    String pattern = bundle.getString(key);
    if (params.length > 0 && pattern.contains("{0}")) {
      return java.text.MessageFormat.format(pattern, params);
    }
    StringBuilder sb = new StringBuilder(pattern);
    for (Object s : params) {
      sb.append(" ").append(s);
    }
    return sb.toString();
  }

  /** Constructs a new I18n manager. */
  private I18n() {}
}
