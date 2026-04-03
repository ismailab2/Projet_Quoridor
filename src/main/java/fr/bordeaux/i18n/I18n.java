package fr.bordeaux.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Provides internationalization (I18n) support for the application. This class is responsible for
 * managing resource bundles, locale settings, and translating UI strings to support multiple
 * languages.
 */
public class I18n {
  private static final String BUNDLE_NAME = "international/messages";
  private static ResourceBundle bundle;
  private static Locale currentLocale;

  // Default param
  static {
    setLocale(Locale.getDefault());
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

  /** Get a simple message */
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
  public static String get(String key, String... params) {
    String pattern = bundle.getString(key);
    for (String s : params) {
      pattern += s + " ";
    }
    return pattern;
  }

  /** Constructs a new I18n manager. */
  public I18n() {}
}
