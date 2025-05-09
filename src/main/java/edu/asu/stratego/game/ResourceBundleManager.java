package edu.asu.stratego.game;

import java.util.Locale;
import java.util.ResourceBundle;


public class ResourceBundleManager {
    private static Locale currentLocale = new Locale("en");
    private static ResourceBundle bundle = ResourceBundle.getBundle("messages", currentLocale);

    public static void setLocale(Locale locale) {
        currentLocale = locale;
        bundle = ResourceBundle.getBundle("messages", currentLocale);
    }

    public static String get(String key) {
        return bundle.getString(key);
    }

    public static Locale getLocale() {
        return currentLocale;
    }
}
