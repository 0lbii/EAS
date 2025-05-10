package edu.asu.stratego.gui;

import edu.asu.stratego.game.ResourceBundleManager;
import edu.asu.stratego.languages.LanguageObservable;
import edu.asu.stratego.languages.LanguageObserver;
import edu.asu.stratego.media.ImageConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class HistoryScene implements LanguageObserver {

    private Scene scene;
    private Button backButton = new Button();

    private Label titleLabel = new Label();
    private Label messageLabel = new Label();

    private static final int SIDE = ClientStage.getSide();

    public HistoryScene(Runnable onBack) {
        LanguageObservable.addObserver(this);

        // Styles
        titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");
        messageLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
        backButton.setStyle("-fx-font-size: 18px; -fx-pref-width: 220px; -fx-pref-height: 45px;");

        updateTexts();

        // Layout
        VBox historyBox = new VBox(20, titleLabel, messageLabel, backButton);
        historyBox.setAlignment(Pos.CENTER);

        ImageView logoImage = new ImageView(ImageConstants.stratego_logo);
        logoImage.setFitWidth(SIDE / 2.0);
        logoImage.setPreserveRatio(true);
        VBox.setMargin(logoImage, new Insets(0, 0, 20, 0));

        VBox content = new VBox(logoImage, historyBox);
        content.setAlignment(Pos.CENTER);

        ImageView backgroundImage = new ImageView(ImageConstants.MAIN_MENU);
        backgroundImage.setFitHeight(SIDE);
        backgroundImage.setFitWidth(SIDE);
        backgroundImage.setPreserveRatio(false);

        StackPane root = new StackPane(backgroundImage, content);
        root.setMaxSize(SIDE, SIDE);
        this.scene = new Scene(root, SIDE, SIDE);

        backButton.setOnAction(e -> onBack.run());
    }

    @Override
    public void onLanguageChanged() {
        updateTexts();
    }

    private void updateTexts() {
        titleLabel.setText(ResourceBundleManager.get("menu.history"));
        messageLabel.setText(ResourceBundleManager.get("history.notimplemented"));
        backButton.setText(ResourceBundleManager.get("menu.back"));
    }

    public Scene getScene() {
        return scene;
    }
}
