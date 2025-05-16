package edu.asu.stratego.gui;

import edu.asu.stratego.game.ResourceBundleManager;
import edu.asu.stratego.languages.LanguageObservable;
import edu.asu.stratego.languages.LanguageObserver;
import edu.asu.stratego.media.ImageConstants;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MainMenuScene implements LanguageObserver {

    private final Scene scene;
    private final Button newGameButton = new Button();
    private final Button historyButton = new Button();
    private final Button profileButton = new Button();
    private final Button rankingButton = new Button();
    private final Button settingsButton = new Button();
    private final Button exitButton = new Button();

    private static final int SIDE = ClientStage.getSide();

    public MainMenuScene() {
        Platform.runLater(() -> LanguageObservable.addObserver(this));

        VBox menuBox = new VBox(15, newGameButton, historyButton, profileButton, rankingButton, settingsButton, exitButton);
        menuBox.setAlignment(Pos.CENTER);

        ImageView logoImage = new ImageView(ImageConstants.stratego_logo);
        logoImage.setFitWidth(SIDE / 2.0);
        logoImage.setPreserveRatio(true);
        VBox.setMargin(logoImage, new Insets(0, 0, 20, 0));

        VBox content = new VBox(logoImage, menuBox);
        content.setAlignment(Pos.CENTER);

        ImageView backgroundImage = new ImageView(ImageConstants.MAIN_MENU);
        backgroundImage.setFitHeight(SIDE);
        backgroundImage.setFitWidth(SIDE);
        backgroundImage.setPreserveRatio(false);

        StackPane root = new StackPane(backgroundImage, content);
        root.setMaxSize(SIDE, SIDE);
        this.scene = new Scene(root, SIDE, SIDE);

        String buttonStyle = "-fx-font-size: 18px; -fx-pref-width: 220px; -fx-pref-height: 45px;";
        newGameButton.setStyle(buttonStyle);
        historyButton.setStyle(buttonStyle);
        profileButton.setStyle(buttonStyle);
        rankingButton.setStyle(buttonStyle);
        settingsButton.setStyle(buttonStyle);
        exitButton.setStyle(buttonStyle);

        Platform.runLater(this::updateTexts);
    }

    @Override
    public void updateTexts() {
        newGameButton.setText(ResourceBundleManager.get("menu.newgame"));
        historyButton.setText(ResourceBundleManager.get("menu.history"));
        profileButton.setText(ResourceBundleManager.get("menu.profile"));
        rankingButton.setText(ResourceBundleManager.get("menu.ranking"));
        settingsButton.setText(ResourceBundleManager.get("menu.settings"));
        exitButton.setText(ResourceBundleManager.get("menu.exit"));
    }

    public void setNewGameAction(Runnable action) {
        newGameButton.setOnAction(e -> action.run());
    }

    public void setSettingsAction(Runnable action) {
        settingsButton.setOnAction(e -> action.run());
    }

    public void setProfileAction(Runnable action) {
        profileButton.setOnAction(e -> action.run());
    }

    public void setRankingAction(Runnable action) {
        rankingButton.setOnAction(e -> action.run());
    }

    public void setHistoryAction(Runnable action) {
        historyButton.setOnAction(e -> action.run());
    }

    public void setExitAction(Runnable action) {
        exitButton.setOnAction(e -> action.run());
    }

    public Scene getScene() {
        return scene;
    }
}
