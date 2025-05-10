package edu.asu.stratego.gui;

import edu.asu.stratego.game.Game;
import edu.asu.stratego.game.ResourceBundleManager;
import edu.asu.stratego.media.ImageConstants;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ExitScene {
    private Scene scene;

    public ExitScene() {
        // UI Components
        Label goodbyeLabel = new Label("¡Adiós, hasta pronto!");
        goodbyeLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");

        Label userLabel = new Label(Game.getPlayer().getNickname());
        userLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: white;");

        Button exitButton = new Button("Salir definitivamente");
        exitButton.setStyle("-fx-font-size: 18px; -fx-pref-width: 220px; -fx-pref-height: 45px;");
        exitButton.setOnAction(e -> Platform.exit());

        // Layout
        VBox exitBox = new VBox(20, goodbyeLabel, userLabel, exitButton);
        exitBox.setAlignment(Pos.CENTER);

        ImageView backgroundImage = new ImageView(ImageConstants.MAIN_MENU);
        backgroundImage.setFitHeight(ClientStage.getSide());
        backgroundImage.setFitWidth(ClientStage.getSide());
        backgroundImage.setPreserveRatio(false);

        StackPane root = new StackPane(backgroundImage, exitBox);
        root.setMaxSize(ClientStage.getSide(), ClientStage.getSide());
        this.scene = new Scene(root, ClientStage.getSide(), ClientStage.getSide());
    }

    public Scene getScene() {
        return scene;
    }
}
