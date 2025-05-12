package edu.asu.stratego.gui;

import edu.asu.stratego.game.Game;
import edu.asu.stratego.game.ResourceBundleManager;
import edu.asu.stratego.languages.LanguageObservable;
import edu.asu.stratego.languages.LanguageObserver;
import edu.asu.stratego.media.ImageConstants;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ProfileScene implements LanguageObserver {

    private final Scene scene;
    private final Button backButton = new Button();
    private final Label nicknameLabel = new Label();
    private final Label emailLabel = new Label();
    private final Label pointsLabel = new Label();
    private final Label titleLabel = new Label();

    private static final int SIDE = ClientStage.getSide();

    public ProfileScene(Runnable onBackAction) {
        LanguageObservable.addObserver(this);

        // Styles
        titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");
        nicknameLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
        emailLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
        pointsLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
        backButton.setStyle("-fx-font-size: 16px;");
        backButton.setPrefWidth(180);
        backButton.setPrefHeight(40);
        backButton.setOnAction(e -> onBackAction.run());

        updateTexts();

        VBox content = new VBox(15, titleLabel, nicknameLabel, emailLabel, pointsLabel, backButton);
        content.setAlignment(Pos.CENTER);

        ImageView background = new ImageView(ImageConstants.MAIN_MENU);
        background.setFitWidth(SIDE);
        background.setFitHeight(SIDE);
        background.setPreserveRatio(false);

        StackPane root = new StackPane(background, content);
        scene = new Scene(root, SIDE, SIDE);
    }

    @Override
    public void onLanguageChanged() {
        updateTexts();
    }

    private void updateTexts() {
        String nickname = Game.getPlayer().getNickname();
        String email = Game.getPlayer().getEmail();
        Integer points = Game.getPlayer().getPoints();

        titleLabel.setText(ResourceBundleManager.get("menu.profile"));
        nicknameLabel.setText(ResourceBundleManager.get("profile.nickname") + ": " + nickname);
        emailLabel.setText(ResourceBundleManager.get("profile.email") + ": " + email);
        pointsLabel.setText(ResourceBundleManager.get("profile.points") + ": " + points);
        backButton.setText(ResourceBundleManager.get("menu.back"));
    }

    public Scene getScene() {
        return scene;
    }
}

