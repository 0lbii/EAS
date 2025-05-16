package edu.asu.stratego.gui;

import edu.asu.stratego.game.ResourceBundleManager;
import edu.asu.stratego.gui.rankingIterator.PlayerIterator;
import edu.asu.stratego.gui.rankingIterator.RankingCollection;
import edu.asu.stratego.languages.LanguageObservable;
import edu.asu.stratego.languages.LanguageObserver;
import edu.asu.stratego.media.ImageConstants;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import models.Player;

public class RankingScene implements LanguageObserver {

    private final Scene scene;
    private final Button backButton = new Button();
    private final VBox rankingBox = new VBox(10);
    private final Label titleLabel = new Label();

    public RankingScene(Runnable onBackAction) {
        Platform.runLater(() -> LanguageObservable.addObserver(this));

        titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");

        backButton.setOnAction(e -> onBackAction.run());
        backButton.setStyle("-fx-font-size: 16px;");
        backButton.setPrefWidth(220);
        backButton.setPrefHeight(45);

        rankingBox.setAlignment(Pos.CENTER);
        rankingBox.setPadding(new Insets(20));
        rankingBox.setStyle("-fx-background-color: transparent;");

        ScrollPane scrollPane = new ScrollPane(rankingBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxHeight(400);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        // Logo
        ImageView logoImage = new ImageView(ImageConstants.stratego_logo);
        logoImage.setFitWidth(ClientStage.getSide() / 2.0);
        logoImage.setPreserveRatio(true);
        VBox.setMargin(logoImage, new Insets(0, 0, 20, 0));

        VBox content = new VBox(logoImage, titleLabel, scrollPane, backButton);
        content.setAlignment(Pos.CENTER);
        content.setSpacing(20);

        // Background
        ImageView backgroundImage = new ImageView(ImageConstants.MAIN_MENU);
        backgroundImage.setFitHeight(ClientStage.getSide());
        backgroundImage.setFitWidth(ClientStage.getSide());
        backgroundImage.setPreserveRatio(false);

        StackPane root = new StackPane(backgroundImage, content);
        root.setPrefSize(ClientStage.getSide(), ClientStage.getSide());

        scene = new Scene(root, ClientStage.getSide(), ClientStage.getSide());

        updateTexts();
        loadRanking();
    }

    private void loadRanking() {
        rankingBox.getChildren().clear();
        RankingCollection collection = new RankingCollection();
        PlayerIterator iterator = collection.iterator();
        int rank = 1;

        while (iterator.hasNext()) {
            Player p = iterator.next();
            Label label = new Label(rank++ + ". " + p.getNickname() + " - " + p.getPoints() + " pts");
            label.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
            rankingBox.getChildren().add(label);
        }
    }

    @Override
    public void updateTexts() {
        backButton.setText(ResourceBundleManager.get("menu.back"));
        titleLabel.setText(ResourceBundleManager.get("menu.ranking"));
    }

    public Scene getScene() {
        return scene;
    }
}