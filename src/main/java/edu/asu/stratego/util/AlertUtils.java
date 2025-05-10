package edu.asu.stratego.util;

import java.util.Optional;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

public class AlertUtils {

    /**
     * Shows a retry/cancel alert dialog on the JavaFX thread.
     *
     * @param title    the title of the alert window
     * @param header   the header text of the alert
     * @param content  the content text of the alert
     * @param onRetry  code to run if the user clicks "Retry"
     * @param onCancel code to run if the user clicks "Cancel"
     */
    public static void showRetryAlert(String title, String header, String content, Runnable onRetry,
            Runnable onCancel) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);

            ButtonType retry = new ButtonType("Retry");
            ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(retry, cancel);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                if (result.get() == retry) {
                    onRetry.run();
                } else if (result.get() == cancel) {
                    onCancel.run();
                }
            }
        });
    }

    /**
     * Shows an informational alert dialog on the JavaFX thread.
     *
     * @param title   the title of the alert window
     * @param header  the header text of the alert
     * @param content the content text of the alert
     */
    public static void showInfoAlert(String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

}
