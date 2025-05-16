package edu.asu.stratego.gui;

import edu.asu.stratego.game.ResourceBundleManager;
import edu.asu.stratego.languages.LanguageObservable;
import edu.asu.stratego.languages.LanguageObserver;
import edu.asu.stratego.media.ImageConstants;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class EditProfileScene implements LanguageObserver {

    private final Scene scene;
    private final TextField nicknameField = new TextField();
    private final TextField passwordField  = new TextField();
    private final Button saveButton = new Button();
    private final Button backButton = new Button();
    private final Label titleLabel = new Label();

    private static final int SIDE = ClientStage.getSide();

    public EditProfileScene(models.Player player, Runnable onBackAction) {
        Platform.runLater(() -> LanguageObservable.addObserver(this));

        titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");
        nicknameField.setStyle("-fx-font-size: 16px; -fx-pref-height: 40px;");
        passwordField .setStyle("-fx-font-size: 16px; -fx-pref-height: 40px;");
        nicknameField.setMaxWidth(220);
        passwordField .setMaxWidth(220);
        saveButton.setPrefWidth(180);
        backButton.setPrefWidth(180);
        saveButton.setStyle("-fx-font-size: 16px;");
        backButton.setStyle("-fx-font-size: 16px;");

        nicknameField.setText(player.getNickname());
        passwordField .setText(player.getPassword());

        saveButton.setOnAction(e -> {
            player.setNickname(nicknameField.getText());
            player.setPassword(passwordField.getText());

            new services.PlayerService().savePlayer(player);
            onBackAction.run();
        });

        backButton.setOnAction(e -> onBackAction.run());

        updateTexts();

        VBox content = new VBox(15, titleLabel, nicknameField, passwordField, saveButton, backButton);
        content.setAlignment(Pos.CENTER);

        ImageView background = new ImageView(ImageConstants.MAIN_MENU);
        background.setFitWidth(SIDE);
        background.setFitHeight(SIDE);
        background.setPreserveRatio(false);

        StackPane root = new StackPane(background, content);
        scene = new Scene(root, SIDE, SIDE);
    }

    @Override
    public void updateTexts() {
        titleLabel.setText(ResourceBundleManager.get("menu.editprofile"));
        nicknameField.setPromptText(ResourceBundleManager.get("profile.nickname"));
        passwordField.setPromptText(ResourceBundleManager.get("profile.password"));
        saveButton.setText(ResourceBundleManager.get("menu.save"));
        backButton.setText(ResourceBundleManager.get("menu.back"));
    }

    public Scene getScene() {
        return scene;
    }
}