package edu.asu.stratego.game;

import java.util.Locale;
import java.util.ResourceBundle;

import edu.asu.stratego.languages.LanguageObservable;

public class ResourceBundleManager {

    private static Locale currentLocale = Locale.of("en");

    private static ResourceBundle bundle = ResourceBundle.getBundle("messages", currentLocale);

    public static void setLocale(Locale locale) {
        currentLocale = locale;
        bundle = ResourceBundle.getBundle("messages", currentLocale);
        LanguageObservable.notifyObservers();
    }

    public static String get(String key) {
        return bundle.getString(key);
    }

    public static Locale getLocale() {
        return currentLocale;
    }

}
