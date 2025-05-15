package edu.asu.stratego.gui.rankingIterator;

import java.util.List;
import models.Player;

public class RankingIterator implements PlayerIterator {

    private final List<Player> players;
    private int index = 0;

    public RankingIterator(List<Player> players) {
        this.players = players;
    }

    @Override
    public boolean hasNext() {
        return index < players.size();
    }

    @Override
    public Player next() {
        return players.get(index++);
    }
}