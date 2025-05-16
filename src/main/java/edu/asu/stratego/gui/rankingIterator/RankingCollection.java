package edu.asu.stratego.gui.rankingIterator;

import java.util.List;

import models.Player;
import services.PlayerService;

public class RankingCollection {

    private final List<Player> players;

    public RankingCollection() {
        this.players = new PlayerService().findAllOrderByPointsDesc();
    }

    public PlayerIterator iterator() {
        return new RankingIterator(players);
    }
}