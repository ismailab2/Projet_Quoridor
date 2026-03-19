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

  public static void setLocale(Locale locale) {
    currentLocale = locale;
    bundle = ResourceBundle.getBundle(BUNDLE_NAME, currentLocale);
  }

  /** Get a simple message */
  public static String get(String key) {
    return bundle.getString(key);
  }

  /**
   * Récupère un message dynamique avec des paramètres Exemple : "Bonjour {0}, vous avez {1}
   * messages."
   */
  public static String get(String key, Object... params) {
    String pattern = bundle.getString(key);
    return MessageFormat.format(pattern, params);
  }

  /** Constructs a new I18n manager. */
  public I18n() {}
}
