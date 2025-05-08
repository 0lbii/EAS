package edu.asu.stratego.gui;

import services.PlayerService;

import edu.asu.stratego.game.ClientSocket;
import edu.asu.stratego.game.Game;
import edu.asu.stratego.media.ImageConstants;

import java.io.IOException;

import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Wrapper class for a JavaFX scene. Contains a scene UI and its associated
 * event handlers for retrieving network connection information from the player
 * and connecting to the network.
 */
public class ConnectionScene {

    private static final Object playerLogin = new Object();

    private static StackPane root = new StackPane();
    private static GridPane loginPanel = new GridPane();

    private Button loginButton = new Button("Login");
    private Button registerButton = new Button("Register if you don't have an account");
    private TextField emailField = new TextField();
    private TextField passwordField = new TextField();
    private TextField serverIPField = new TextField();
    static Label statusLabel = new Label();

    private static String email, password, serverIP;

    private final int SIDE = ClientStage.getSide();

    Scene scene;

    /**
     * Creates a new instance of ConnectionScene.
     */
    public ConnectionScene() {
        root.getChildren().clear();
        StackPane sceneRoot = new StackPane();
        sceneRoot.setMaxSize(SIDE, SIDE);
        loginPanel.getChildren().clear();

        // Add form fields
        loginPanel.add(new Label("Email: "), 0, 0);
        loginPanel.add(emailField, 1, 0);
        loginPanel.add(new Label("Password: "), 0, 1);
        loginPanel.add(passwordField, 1, 1);
        loginPanel.add(new Label("Server IP: "), 0, 2);
        loginPanel.add(serverIPField, 1, 2);
        loginPanel.add(loginButton, 1, 3);
        loginPanel.add(registerButton, 1, 4);
        loginPanel.add(statusLabel, 1, 5);

        // Adjust spacing and alignment
        loginPanel.setHgap(10);
        loginPanel.setVgap(10);
        loginPanel.setAlignment(Pos.CENTER);
        GridPane.setHalignment(loginButton, HPos.CENTER);
        GridPane.setHalignment(registerButton, HPos.CENTER);
        GridPane.setHalignment(statusLabel, HPos.CENTER);

        // Add the logo image
        ImageView logoImage = new ImageView(ImageConstants.stratego_logo);
        logoImage.setFitWidth(SIDE / 2.0);
        logoImage.setPreserveRatio(true);
        VBox.setMargin(logoImage, new Insets(0, 0, 20, 0));

        // Combine logo and form in a vertical layout
        VBox content = new VBox(logoImage, loginPanel);
        content.setAlignment(Pos.CENTER);

        // Add background image
        ImageView backgroundImage = new ImageView(ImageConstants.LOGIN_REGISTER);
        backgroundImage.setFitHeight(SIDE);
        backgroundImage.setFitWidth(SIDE);
        backgroundImage.setPreserveRatio(false);

        // Compose final scene
        sceneRoot.getChildren().addAll(backgroundImage, content);
        scene = new Scene(sceneRoot, SIDE, SIDE);

        // Set button actions
        loginButton.setOnAction(e -> Platform.runLater(new ProcessFields()));
        registerButton.setOnAction(e -> {
            RegisterScene registerScene = new RegisterScene();
            Stage clientStage = (Stage) loginButton.getScene().getWindow();
            clientStage.setScene(registerScene.getScene());
        });
    }

    /**
     * Handles the login process including validation and status updates.
     */
    private class ProcessFields implements Runnable {
        @Override
        public void run() {
            // Retrieve user inputs
            email = emailField.getText();
            password = passwordField.getText();
            serverIP = serverIPField.getText();

            // Set default values if empty
            if (email.equals(""))
                email = "Player";
            if (serverIP.equals(""))
                serverIP = "localhost";

            // Authenticate user
            PlayerService service = new PlayerService();
            models.Player player = service.findByEmailAndPassword(email, password);
            if (player != null) {
                Game.getPlayer().setNickname(player.getNickname());
                statusLabel.setText("Welcome " + player.getNickname() + "!");
            } else {
                statusLabel.setText("Invalid credentials");
                return;
            }

            // Disable inputs during connection
            emailField.setEditable(false);
            passwordField.setEditable(false);
            serverIPField.setEditable(false);
            loginButton.setDisable(true);
            registerButton.setDisable(true);

            // Notify connection thread and wait
            synchronized (playerLogin) {
                try {
                    playerLogin.notify();
                    playerLogin.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    statusLabel.setText("Connection interrupted. Please try again.");
                }
            }

            // Re-enable inputs
            emailField.setEditable(true);
            passwordField.setEditable(true);
            serverIPField.setEditable(true);
            loginButton.setDisable(false);
            registerButton.setDisable(false);
        }
    }

    /**
     * A Runnable task for establishing a connection to a Stratego server.
     * The task will continue running until a successful connection has
     * been made. The connection attempt loop is structured like so:
     *
     * <ol>
     * <li>
     * Wait for the player to invoke button event in the ConnectionScene.
     * </li>
     * <li>
     * Attempt to connect to a Stratego server using the information retrieved
     * from the UI and wake up the button event thread.
     * </li>
     * <li>
     * If connection succeeds, signal the isConnected condition to indicate to
     * other threads a successful connection attempt and then terminate the
     * task. Otherwise, output error message to GUI, and go to #1.
     * </li>
     * </ol>
     *
     * @see edu.asu.stratego.gui.ConnectionScene.ProcessFields
     */
    public static class ConnectToServer implements Runnable {
        @Override
        public void run() {
            while (ClientSocket.getInstance() == null) {
                synchronized (playerLogin) {
                    try {
                        // Wait for submitFields button event
                        playerLogin.wait();
                        // Attempt connection to server
                        ClientSocket.connect(serverIP, 4212);
                    } catch (IOException | InterruptedException e) {
                        Platform.runLater(() -> {
                            statusLabel.setText("Cannot connect to the Server");
                        });
                    } finally {
                        // Wake up button event thread
                        playerLogin.notify();
                    }
                }
            }
        }
    }

    /**
     * Returns the scene instance.
     */
    public Scene getScene() {
        return scene;
    }

    /**
     * Returns the root StackPane for further use if needed.
     */
    public static StackPane getRootPane() {
        return root;
    }

    /**
     * Returns the GridPane containing the login form.
     */
    public static GridPane getLoginPanel() {
        return loginPanel;
    }

}
