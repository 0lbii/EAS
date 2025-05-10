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

    private static final int SIDE = ClientStage.getSide();

    public ProfileScene(Runnable onBackAction) {
        LanguageObservable.addObserver(this);

        String nickname = Game.getPlayer().getNickname();
        String email = Game.getPlayer().getEmail();

        nicknameLabel.setText(ResourceBundleManager.get("profile.nickname") + ": " + nickname);
        nicknameLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");

        emailLabel.setText(ResourceBundleManager.get("profile.email") + ": " + email);
        emailLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");

        backButton.setText(ResourceBundleManager.get("menu.back"));
        backButton.setOnAction(e -> onBackAction.run());
        backButton.setPrefWidth(180);
        backButton.setPrefHeight(40);
        backButton.setStyle("-fx-font-size: 16px;");

        VBox content = new VBox(15, nicknameLabel, emailLabel, backButton);
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
        String nickname = Game.getPlayer().getNickname();
        String email = Game.getPlayer().getEmail();

        nicknameLabel.setText(ResourceBundleManager.get("profile.nickname") + ": " + nickname);
        emailLabel.setText(ResourceBundleManager.get("profile.email") + ": " + email);
        backButton.setText(ResourceBundleManager.get("button.back"));
    }

    public Scene getScene() {
        return scene;
    }
}
