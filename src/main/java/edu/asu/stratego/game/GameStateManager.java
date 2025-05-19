package edu.asu.stratego.game;

import java.time.LocalDateTime;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import edu.asu.stratego.game.pieces.PieceColor;

public class GameStateManager {

    public static void initializeGameState(ObjectInputStream fromServer, ObjectOutputStream toServer)
            throws IOException, ClassNotFoundException {
        Game.getPlayer().setColor(null);
        Game.setOpponent(null);
        Game.setStatus(GameStatus.SETTING_UP);
        Game.setTurn(PieceColor.RED);
        Game.setMove(new Move());
        Game.setMoveStatus(MoveStatus.OPP_TURN);

        // Exchange of information with the server
        toServer.writeObject(Game.getPlayer());
        Game.setOpponent((Player) fromServer.readObject());

        // Infer the player's color
        if (Game.getOpponent().getColor() == PieceColor.RED) {
            Game.getPlayer().setColor(PieceColor.BLUE);
        } else {
            Game.getPlayer().setColor(PieceColor.RED);
        }
    }

    public static void startNewGame() {
        Game.setStartTime(LocalDateTime.now());
        Game.setStatus(GameStatus.IN_PROGRESS);
    }

    public static void resetGameState() {
        Game.resetGame();
    }

    public static void updateGameStatus(Object received) {
        if (received instanceof GameStatus) {
            Game.setStatus((GameStatus) received);
        } else if (received instanceof PieceColor) {
            Game.setTurn((PieceColor) received);
            updateMoveStatusBasedOnTurn();
        }
    }

    private static void updateMoveStatusBasedOnTurn() {
        if (Game.getPlayer().getColor() == Game.getTurn()) {
            Game.setMoveStatus(MoveStatus.NONE_SELECTED);
        } else {
            Game.setMoveStatus(MoveStatus.OPP_TURN);
        }
    }

    public static String getEndGameMessage() {
        String messageKey = "";

        if (Game.getStatus() == GameStatus.RED_CAPTURED || Game.getStatus() == GameStatus.RED_NO_MOVES) {
            messageKey = (Game.getPlayer().getColor() == PieceColor.BLUE) ? "game.end.you_win" : "game.end.you_lose";
        } else if (Game.getStatus() == GameStatus.BLUE_CAPTURED || Game.getStatus() == GameStatus.BLUE_NO_MOVES) {
            messageKey = (Game.getPlayer().getColor() == PieceColor.RED) ? "game.end.you_win" : "game.end.you_lose";
        } else if (Game.getStatus() == GameStatus.RED_DISCONNECTED
                || Game.getStatus() == GameStatus.BLUE_DISCONNECTED) {
            messageKey = "game.end.opponent_disconnected";
        }

        return ResourceBundleManager.get(messageKey);
    }

    public static boolean isCurrentPlayerWinner() {
        if (Game.getStatus() == GameStatus.RED_CAPTURED || Game.getStatus() == GameStatus.RED_NO_MOVES) {
            return Game.getPlayer().getColor() == PieceColor.BLUE;
        } else if (Game.getStatus() == GameStatus.BLUE_CAPTURED || Game.getStatus() == GameStatus.BLUE_NO_MOVES) {
            return Game.getPlayer().getColor() == PieceColor.RED;
        }
        return false;
    }

    public static boolean wasGameAbandoned() {
        return Game.getStatus() == GameStatus.RED_DISCONNECTED || Game.getStatus() == GameStatus.BLUE_DISCONNECTED;
    }
}