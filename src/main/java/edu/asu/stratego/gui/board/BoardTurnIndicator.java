package edu.asu.stratego.gui.board;

import edu.asu.stratego.game.Game;
import edu.asu.stratego.game.pieces.PieceColor;
import edu.asu.stratego.gui.ClientStage;
import javafx.animation.FillTransition;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * JavaFX rectangle that is layered behind the semi-transparent board border to
 * indicate to the players whose turn it is. Depending on which player's turn
 * it is, the rectangle will be colored either red or blue.
 */
public class BoardTurnIndicator {

    // Constants for colors
    private static final Color RED_COLOR = new Color(0.48, 0.13, 0.13, 1.0);
    private static final Color BLUE_COLOR = new Color(0.22, 0.24, 0.55, 1.0);

    // Constants for animation times
    private static final int RED_TRANSITION_TIME = 2000;
    private static final int BLUE_TRANSITION_TIME = 3000;

    // Synchronization object for turn changes
    private static final Object turnIndicatorTrigger = new Object();

    // The visual indicator
    private static Rectangle turnIndicator;

    /**
     * Creates a new instance of BoardTurnIndicator.
     */
    public BoardTurnIndicator() {
        final int SIDE = ClientStage.getSide();
        turnIndicator = new Rectangle(0, 0, SIDE, SIDE);

        // Set the initial turn color based on player's assigned color
        initializeIndicatorColor();

        // Start thread to automatically update turn color
        startUpdateThread();
    }

    /**
     * Initializes the indicator color based on the player's assigned color
     */
    private void initializeIndicatorColor() {
        if (Game.getPlayer().getColor() == PieceColor.RED) {
            turnIndicator.setFill(RED_COLOR);
        } else {
            turnIndicator.setFill(BLUE_COLOR);
        }
    }

    /**
     * Starts the thread that listens for turn changes
     */
    private void startUpdateThread() {
        Thread updateColor = new Thread(new UpdateColor());
        updateColor.setDaemon(true);
        updateColor.setName("TurnIndicator-Thread");
        updateColor.start();
    }

    /**
     * Returns the turn indicator (JavaFX rectangle)
     * 
     * @return the turn indicator rectangle
     */
    public static Rectangle getTurnIndicator() {
        return turnIndicator;
    }

    /**
     * Returns the object used to communicate between the ClientGameManager and the
     * BoardTurnIndicator to indicate when the player turn color has changed.
     * 
     * @return object used for turn change synchronization
     */
    public static Object getTurnIndicatorTrigger() {
        return turnIndicatorTrigger;
    }

    /**
     * Runnable implementation that updates the turn indicator color
     * whenever it receives a notification
     */
    private class UpdateColor implements Runnable {
        @Override
        public void run() {
            synchronized (turnIndicatorTrigger) {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        // Wait for player turn color to change
                        turnIndicatorTrigger.wait();

                        // Update UI on the JavaFX application thread
                        Platform.runLater(this::updateIndicatorColor);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Turn indicator update thread interrupted. Exiting thread.");
                }
            }
        }

        /**
         * Updates the indicator color based on the current turn
         */
        private void updateIndicatorColor() {
            PieceColor currentTurn = Game.getTurn();
            Color currentFill = (Color) turnIndicator.getFill();

            // Only perform transition if color is actually changing
            if (currentTurn == PieceColor.RED && currentFill != RED_COLOR) {
                // Transition from blue to red
                performTransition(BLUE_COLOR, RED_COLOR, RED_TRANSITION_TIME);
            } else if (currentTurn == PieceColor.BLUE && currentFill != BLUE_COLOR) {
                // Transition from red to blue
                performTransition(RED_COLOR, BLUE_COLOR, BLUE_TRANSITION_TIME);
            }
        }

        /**
         * Performs a color transition animation on the turn indicator
         * 
         * @param fromColor starting color
         * @param toColor   ending color
         * @param duration  duration in milliseconds
         */
        private void performTransition(Color fromColor, Color toColor, int duration) {
            FillTransition transition = new FillTransition(
                    Duration.millis(duration),
                    turnIndicator,
                    fromColor,
                    toColor);
            transition.play();
        }
    }

}
