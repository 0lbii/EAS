package edu.asu.stratego.gui;

import edu.asu.stratego.game.ResourceBundleManager;
import edu.asu.stratego.languages.LanguageObserver;
import edu.asu.stratego.media.ImageConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Wrapper class for a JavaFX scene. Contains a scene UI to indicate that the 
 * client has successfully connected to a server and is currently waiting for 
 * another opponent to connect to the server. The intended function for this 
 * scene is analogous to a loading screen.
 */
public class WaitingScene implements LanguageObserver{

    private static final int SIDE = ClientStage.getSide();
    private Scene scene;
    private final Label waitingLabel = new Label();

    /**
     * Creates a new instance of WaitingScene.
     */
    public WaitingScene() {
        // Create message label
        waitingLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: white;");
        onLanguageChanged();

        // Create logo image
        ImageView logoImage = new ImageView(ImageConstants.stratego_logo);
        logoImage.setFitWidth(SIDE / 2.0);
        logoImage.setPreserveRatio(true);
        VBox.setMargin(logoImage, new Insets(0, 0, 20, 0));

        // Combine logo and label
        VBox content = new VBox(logoImage, waitingLabel);
        content.setAlignment(Pos.CENTER);

        // Background image
        ImageView backgroundImage = new ImageView(ImageConstants.LOGIN_REGISTER);
        backgroundImage.setFitWidth(SIDE);
        backgroundImage.setFitHeight(SIDE);
        backgroundImage.setPreserveRatio(false);

        // Root layout
        StackPane root = new StackPane(backgroundImage, content);
        root.setMaxSize(SIDE, SIDE);

        scene = new Scene(root, SIDE, SIDE);
    }

    public Scene getScene() {
        return scene;
    }

    @Override
    public void onLanguageChanged() {
        waitingLabel.setText(ResourceBundleManager.get("waiting.message"));
    }
}