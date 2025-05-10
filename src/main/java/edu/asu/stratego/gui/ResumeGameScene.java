package edu.asu.stratego.gui;

import edu.asu.stratego.game.ResourceBundleManager;
import edu.asu.stratego.media.ImageConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ResumeGameScene {
    private Scene scene;
    private Button backButton = new Button();

    private static final int SIDE = ClientStage.getSide();

    public ResumeGameScene(Runnable onBack) {
        // UI Components
        Label titleLabel = new Label(ResourceBundleManager.get("menu.resume"));
        titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");

        Label messageLabel = new Label("No hay partidas en progreso");
        messageLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");

        backButton.setText(ResourceBundleManager.get("menu.back"));

        // Style
        String buttonStyle = "-fx-font-size: 18px; -fx-pref-width: 220px; -fx-pref-height: 45px;";
        backButton.setStyle(buttonStyle);

        // Layout
        VBox resumeBox = new VBox(20, titleLabel, messageLabel, backButton);
        resumeBox.setAlignment(Pos.CENTER);

        ImageView logoImage = new ImageView(ImageConstants.stratego_logo);
        logoImage.setFitWidth(SIDE / 2.0);
        logoImage.setPreserveRatio(true);
        VBox.setMargin(logoImage, new Insets(0, 0, 20, 0));

        VBox content = new VBox(logoImage, resumeBox);
        content.setAlignment(Pos.CENTER);

        ImageView backgroundImage = new ImageView(ImageConstants.MAIN_MENU);
        backgroundImage.setFitHeight(SIDE);
        backgroundImage.setFitWidth(SIDE);
        backgroundImage.setPreserveRatio(false);

        StackPane root = new StackPane(backgroundImage, content);
        root.setMaxSize(SIDE, SIDE);
        this.scene = new Scene(root, SIDE, SIDE);

        // Logic
        backButton.setOnAction(e -> onBack.run());
    }

    public Scene getScene() {
        return scene;
    }
}
