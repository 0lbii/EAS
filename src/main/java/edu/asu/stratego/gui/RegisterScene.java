package edu.asu.stratego.gui;

import services.PlayerService;

import edu.asu.stratego.media.ImageConstants;

import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 * Wrapper class for the registration scene in the application.
 * This JavaFX scene provides the user interface for new players to register
 * by inputting their nickname, email, and password. It handles validation,
 * user feedback, and integrates with the PlayerService to persist new users.
 */
public class RegisterScene {

    private Scene scene;

    private static final int SIDE = ClientStage.getSide();

    /**
     * Creates a new instance of RegisterScene.
     */
    public RegisterScene() {
        // Input fields
        TextField nicknameField = new TextField();
        TextField emailField = new TextField();
        TextField passwordField = new TextField();
        Button registerBtn = new Button("Register");
        Button loginBtn = new Button("Back to Login");

        nicknameField.setPromptText("Nickname");
        emailField.setPromptText("Email");
        passwordField.setPromptText("Password");

        Label statusLabel = new Label();

        // Create layout grid for the registration form
        GridPane registerPanel = new GridPane();
        registerPanel.add(new Label("Nickname: "), 0, 0);
        registerPanel.add(nicknameField, 1, 0);
        registerPanel.add(new Label("Email: "), 0, 1);
        registerPanel.add(emailField, 1, 1);
        registerPanel.add(new Label("Password: "), 0, 2);
        registerPanel.add(passwordField, 1, 2);
        registerPanel.add(registerBtn, 1, 3);
        registerPanel.add(loginBtn, 1, 4);
        registerPanel.add(statusLabel, 1, 5);

        // Adjust spacing and alignment
        registerPanel.setHgap(10);
        registerPanel.setVgap(10);
        registerPanel.setAlignment(Pos.CENTER);
        GridPane.setHalignment(registerBtn, HPos.CENTER);
        GridPane.setHalignment(loginBtn, HPos.CENTER);
        GridPane.setHalignment(statusLabel, HPos.CENTER);

        // Add the logo image
        ImageView logoImage = new ImageView(ImageConstants.stratego_logo);
        logoImage.setFitWidth(SIDE / 2.0);
        logoImage.setPreserveRatio(true);
        VBox.setMargin(logoImage, new Insets(0, 0, 20, 0));

        // Combine logo and form in a vertical layout
        VBox content = new VBox(logoImage, registerPanel);
        content.setAlignment(Pos.CENTER);

        // Add background image
        ImageView backgroundImage = new ImageView(ImageConstants.LOGIN_REGISTER);
        backgroundImage.setFitHeight(SIDE);
        backgroundImage.setFitWidth(SIDE);
        backgroundImage.setPreserveRatio(false);

        // Final root pane
        StackPane root = new StackPane(backgroundImage, content);
        root.setMaxSize(SIDE, SIDE);
        this.scene = new Scene(root, SIDE, SIDE);

        // Register button action
        registerBtn.setOnAction(e -> {
            PlayerService service = new PlayerService();

            String nick = nicknameField.getText();
            String mail = emailField.getText();
            String pass = passwordField.getText();

            // Input validation
            if (nick.isBlank()) {
                statusLabel.setText("The nickname cannot be empty");
                return;
            }
            if (service.findByNickname(nick) != null) {
                statusLabel.setText("The nickname is already in use");
                return;
            }
            if (pass.length() <= 5) {
                statusLabel.setText("Password must be more than 5 characters long");
                return;
            }
            if (service.findByEmail(mail) != null) {
                statusLabel.setText("The email is already in use");
                return;
            }
            if (!mail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.(com|es|edu|org)$")) {
                statusLabel.setText("The email is not in a valid format");
                return;
            }

            // Create and persist new player
            models.Player p = new models.Player();

            p.setNickname(nick);
            p.setEmail(mail);
            p.setPassword(pass);

            service.savePlayer(p);
            statusLabel.setText("Successfully registered user");

            // After a short delay, redirect to login screen
            Platform.runLater(() -> {
                ConnectionScene connectionScene = new ConnectionScene();
                Stage stage = (Stage) loginBtn.getScene().getWindow();
                stage.setScene(connectionScene.getScene());
            });
        });

        // Login button action
        loginBtn.setOnAction(e -> {
            ConnectionScene connectionScene = new ConnectionScene();
            Stage clientStage = (Stage) loginBtn.getScene().getWindow();
            clientStage.setScene(connectionScene.getScene());
        });
    }

    /**
     * Returns the JavaFX scene for the registration screen.
     * 
     * @return The JavaFX scene.
     */
    public Scene getScene() {
        return scene;
    }

}
