package edu.asu.stratego.languages;

import java.util.ArrayList;
import java.util.List;

public class LanguageObservable {
    private static final List<LanguageObserver> observers = new ArrayList<>();

    public static void addObserver(LanguageObserver observer) {
        observers.add(observer);
    }

    public static void removeObserver(LanguageObserver observer) {
        observers.remove(observer);
    }

    public static void notifyObservers() {
        for (LanguageObserver observer : observers) {
            observer.onLanguageChanged();
        }
    }
}
