package edu.asu.stratego.gui;

import java.util.Locale;

import edu.asu.stratego.game.ResourceBundleManager;
import edu.asu.stratego.media.ImageConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ConfigurationScene {

    private Scene scene;
    private Button backButton = new Button();
    private ComboBox<String> languageComboBox = new ComboBox<>();

    private static final int SIDE = ClientStage.getSide();

    public ConfigurationScene(Runnable onBack) {
        // Components UI
        Label languageLabel = new Label(ResourceBundleManager.get("menu.language"));
        languageLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
        languageComboBox.getItems().addAll("Español", "English");
        Locale current = ResourceBundleManager.getLocale();
        if (current.getLanguage().equals("es")) {
            languageComboBox.setValue("Español");
        } else {
            languageComboBox.setValue("English");
        }

        Button editProfileBtn = new Button(ResourceBundleManager.get("menu.editprofile"));
        backButton.setText(ResourceBundleManager.get("menu.back"));

        // Style
        String buttonStyle = "-fx-font-size: 18px; -fx-pref-width: 220px; -fx-pref-height: 45px;";
        editProfileBtn.setStyle(buttonStyle);
        backButton.setStyle(buttonStyle);

        // Layout
        VBox settingsBox = new VBox(15, languageLabel, languageComboBox, editProfileBtn, backButton);
        settingsBox.setAlignment(Pos.CENTER);

        ImageView logoImage = new ImageView(ImageConstants.stratego_logo);
        logoImage.setFitWidth(SIDE / 2.0);
        logoImage.setPreserveRatio(true);
        VBox.setMargin(logoImage, new Insets(0, 0, 20, 0));

        VBox content = new VBox(logoImage, settingsBox);
        content.setAlignment(Pos.CENTER);

        ImageView backgroundImage = new ImageView(ImageConstants.LOGIN_REGISTER);
        backgroundImage.setFitHeight(SIDE);
        backgroundImage.setFitWidth(SIDE);
        backgroundImage.setPreserveRatio(false);

        StackPane root = new StackPane(backgroundImage, content);
        root.setMaxSize(SIDE, SIDE);
        this.scene = new Scene(root, SIDE, SIDE);

        // Logic
        languageComboBox.setOnAction(e -> {
            String selected = languageComboBox.getValue();
            if (selected.equals("Español")) {
                ResourceBundleManager.setLocale(new Locale("es"));
            } else {
                ResourceBundleManager.setLocale(new Locale("en"));
            }
            onBack.run();
        });

        backButton.setOnAction(e -> onBack.run());

        languageComboBox.setStyle("-fx-font-size: 16px; -fx-pref-width: 220px; -fx-pref-height: 40px;");
    }

    public Scene getScene() {
        return scene;
    }
}
