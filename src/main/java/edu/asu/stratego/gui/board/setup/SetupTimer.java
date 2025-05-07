package edu.asu.stratego.gui.board.setup;

import edu.asu.stratego.gui.ClientStage;
import edu.asu.stratego.gui.board.BoardSquareEventPane;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

/**
 * SetupTimer is responsible for displaying and running a countdown during the
 * setup phase.
 * When the timer reaches zero, a random setup is triggered automatically.
 */
public class SetupTimer {

    private static final int DEFAULT_DURATION = 300; // Default setup time in seconds
    private final Timeline timeline;
    private final Label timerLabel;
    private final IntegerProperty secondsLeft;
    private final BoardSquareEventPane boardPane;


    /**
     * Constructs a SetupTimer with the default duration (5 minutes).
     */
    public SetupTimer(BoardSquareEventPane boardPane) {
        this(DEFAULT_DURATION, boardPane);
    }

    /**
     * Constructs a SetupTimer with a custom duration.
     * 
     * @param durationInSeconds The number of seconds for the countdown.
     */
    public SetupTimer(int durationInSeconds, BoardSquareEventPane boardPane) {
        this.timeline = new Timeline();
        this.timerLabel = new Label();
        this.secondsLeft = new SimpleIntegerProperty(durationInSeconds);
        this.boardPane = boardPane;

        final double UNIT = ClientStage.getUnit();

        // Bind the label's text to the countdown value
        timerLabel.textProperty().bind(secondsLeft.asString());
        timerLabel.setFont(Font.font("Century Gothic", UNIT / 3));
        timerLabel.setTextFill(new Color(0.9, 0.5, 0.0, 1.0));
        timerLabel.setAlignment(Pos.TOP_LEFT);
    }

    /**
     * Starts the countdown timer from the initial time.
     */
    public void startTimer() {
        secondsLeft.set((int) secondsLeft.get());

        timeline.getKeyFrames().setAll(
                new KeyFrame(Duration.seconds(secondsLeft.get() + 1),
                        new KeyValue(secondsLeft, 0)));
        timeline.setOnFinished(new TimerFinished());
        timeline.playFromStart();
    }

    /**
     * @return A JavaFX label showing the remaining time.
     */
    public Label getLabel() {
        return timerLabel;
    }

    /**
     * Triggered when the timer reaches zero. Starts random board setup.
     */
    private class TimerFinished implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            boardPane.randomSetup(); // Automatically place remaining pieces
        }
    }
}
