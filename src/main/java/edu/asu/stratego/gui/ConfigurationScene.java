package edu.asu.stratego.gui;

import java.util.Locale;

import edu.asu.stratego.game.ResourceBundleManager;
import edu.asu.stratego.languages.LanguageObservable;
import edu.asu.stratego.languages.LanguageObserver;
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

public class ConfigurationScene implements LanguageObserver {

    private Scene scene;
    private Button backButton = new Button();
    private Button editProfileBtn = new Button();
    private ComboBox<String> languageComboBox = new ComboBox<>();

    private Label titleLabel = new Label();
    private Label languageLabel = new Label();

    private static final int SIDE = ClientStage.getSide();

    public ConfigurationScene(Runnable onBack) {
        LanguageObservable.addObserver(this);

        // Style
        String buttonStyle = "-fx-font-size: 18px; -fx-pref-width: 220px; -fx-pref-height: 45px;";
        editProfileBtn.setStyle(buttonStyle);
        backButton.setStyle(buttonStyle);
        languageComboBox.setStyle("-fx-font-size: 16px; -fx-pref-width: 220px; -fx-pref-height: 40px;");

        languageComboBox.getItems().addAll("Español", "English");

        Locale current = ResourceBundleManager.getLocale();
        languageComboBox.setValue(current.getLanguage().equals("es") ? "Español" : "English");

        // Layout
        VBox settingsBox = new VBox(15, languageLabel, languageComboBox, editProfileBtn, backButton);
        settingsBox.setAlignment(Pos.CENTER);

        ImageView logoImage = new ImageView(ImageConstants.stratego_logo);
        logoImage.setFitWidth(SIDE / 2.0);
        logoImage.setPreserveRatio(true);
        VBox.setMargin(logoImage, new Insets(0, 0, 20, 0));

        VBox content = new VBox(logoImage, titleLabel, settingsBox);
        content.setAlignment(Pos.CENTER);
        VBox.setMargin(titleLabel, new Insets(0, 0, 15, 0));

        ImageView backgroundImage = new ImageView(ImageConstants.MAIN_MENU);
        backgroundImage.setFitHeight(SIDE);
        backgroundImage.setFitWidth(SIDE);
        backgroundImage.setPreserveRatio(false);

        StackPane root = new StackPane(backgroundImage, content);
        root.setMaxSize(SIDE, SIDE);
        this.scene = new Scene(root, SIDE, SIDE);

        // Buttons
        languageComboBox.setOnAction(e -> {
            String selected = languageComboBox.getValue();
            Locale selectedLocale = selected.equals("Español") ? new Locale("es") : new Locale("en");
            ResourceBundleManager.setLocale(selectedLocale);
        });

        backButton.setOnAction(e -> onBack.run());

        updateTexts();
    }

    @Override
    public void onLanguageChanged() {
        updateTexts();
    }

    private void updateTexts() {
        titleLabel.setText(ResourceBundleManager.get("menu.settings"));
        titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");

        languageLabel.setText(ResourceBundleManager.get("menu.language"));
        languageLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");

        editProfileBtn.setText(ResourceBundleManager.get("menu.editprofile"));
        backButton.setText(ResourceBundleManager.get("menu.back"));
    }

    public Scene getScene() {
        return scene;
    }
}
