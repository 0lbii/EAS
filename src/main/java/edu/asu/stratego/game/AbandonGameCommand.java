package edu.asu.stratego.game;

import java.util.logging.Logger;

public class AbandonGameCommand {
    private static final Logger logger = Logger.getLogger(AbandonGameCommand.class.getName());

    private final ServerGameManager gameManager;

    public AbandonGameCommand(ServerGameManager gameManager) {
        this.gameManager = gameManager;
    }

    public void execute() {
        gameManager.abandonGame();
    }
}