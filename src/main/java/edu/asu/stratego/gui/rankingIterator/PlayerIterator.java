package edu.asu.stratego.gui.rankingIterator;


import models.Player;

public interface PlayerIterator {
    boolean hasNext();
    Player next();
}