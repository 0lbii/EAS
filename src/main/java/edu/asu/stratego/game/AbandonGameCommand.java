package edu.asu.stratego.game;

import java.util.logging.Logger;

/**
 * Command class used to handle the abandonment of a game by a player
 * It encapsulates the logic for notifying the game manager to terminate the
 * game session
 */
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