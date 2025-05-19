package edu.asu.stratego.game;

import java.time.LocalDateTime;

import models.GamePlayer;
import services.GamePlayerService;
import services.GameService;
import services.PlayerService;
import edu.asu.stratego.game.pieces.PieceColor;

public class GameDatabaseManager {

    public static void saveGameToDatabase(boolean isWinner, boolean wasAbandoned) {
        try {
            GameService gameService = new GameService();
            PlayerService playerService = new PlayerService();
            GamePlayerService gamePlayerService = new GamePlayerService();

            // 1. Search for players in the database by their nickname
            models.Player currentPlayer = playerService.findByNickname(Game.getPlayer().getNickname());
            models.Player opponentPlayer = playerService.findByNickname(Game.getOpponent().getNickname());

            if (currentPlayer == null || opponentPlayer == null) {
                return;
            }

            // 2. Create and configure the new game
            models.Game game = new models.Game();
            game.setFinished(true);
            game.setStartTime(Game.getStartTime());
            game.setEndTime(LocalDateTime.now());
            game.setWinner(isWinner ? currentPlayer : opponentPlayer);
            game.setWasAbandoned(wasAbandoned);

            // 3. Save the game to the database
            gameService.saveGame(game);

            // 4.Creating relationships between players and the game
            GamePlayer currentGP = new GamePlayer();
            currentGP.setGame(game);
            currentGP.setPlayer(currentPlayer);
            currentGP.setRedTeam(Game.getPlayer().getColor() == PieceColor.RED);

            GamePlayer opponentGP = new GamePlayer();
            opponentGP.setGame(game);
            opponentGP.setPlayer(opponentPlayer);
            opponentGP.setRedTeam(Game.getOpponent().getColor() == PieceColor.RED);

            gamePlayerService.saveGamePlayer(currentGP);
            gamePlayerService.saveGamePlayer(opponentGP);
        } catch (Exception e) {
        }
    }
}