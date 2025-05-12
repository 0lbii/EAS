package edu.asu.stratego.game;

import edu.asu.stratego.gui.ConfigurationScene;
import edu.asu.stratego.gui.ExitScene;
import edu.asu.stratego.gui.HistoryScene;
import edu.asu.stratego.gui.MainMenuScene;
import edu.asu.stratego.gui.ProfileScene;
import javafx.application.Platform;
import javafx.stage.Stage;

public class SceneController {
    
    private final Stage stage;

    private final ClientGameManager gameManager;

    private MainMenuScene mainMenuScene;

    // private final models.Player player;

    public SceneController(Stage stage, ClientGameManager gameManager) { // , models.Player player
        this.stage = stage;
        this.gameManager = gameManager;
        // this.player = player;
    }

    /**
     * Displays the main menu scene on the JavaFX application thread.
     * The menu provides options for starting a new game, viewing match
     * history, accessing the user profile, and adjusting settings.
     * When "Nueva partida" or "New Game" is selected, the setup and gameplay
     * sequence begins.
     */
    public void showMainMenu() {
        Platform.runLater(() -> {
            if (mainMenuScene == null) {
                mainMenuScene = new MainMenuScene();
                mainMenuScene.setNewGameAction(() -> {
                    new Thread(() -> {
                        gameManager.waitForOpponent();
                        gameManager.setupBoard();
                        gameManager.playGame();
                    }).start();
                });
                mainMenuScene.setSettingsAction(this::showSettingsScreen);

                mainMenuScene.setProfileAction(this::showProfileScreen);

                mainMenuScene.setHistoryAction(this::showHistoryScreen);

                mainMenuScene.setExitAction(this::showExitScreen);

            }
            stage.setScene(mainMenuScene.getScene());
        });
    }

    /**
     * Displays the settings scene, allowing the user to change application language
     * and navigate to the profile editing screen.
     */
    private void showSettingsScreen() {
        Platform.runLater(() -> {
            ConfigurationScene configScene = new ConfigurationScene(this::showMainMenu);
            configScene.setEditProfileAction(() -> {
                //EditProfileScene editScene = new EditProfileScene(this::showSettingsScreen);
                //stage.setScene(editScene.getScene());
            });
            stage.setScene(configScene.getScene());
        });
    }

    /**
     * Displays the history scene, allowing the user to replay un unfinished game
     */
    private void showHistoryScreen() {
        Platform.runLater(() -> {
            HistoryScene historyScene = new HistoryScene(this::showMainMenu);
            stage.setScene(historyScene.getScene());
        });
    }

    /**
     * Displays the profile scene, allowing the user to see their own profile
     */
    private void showProfileScreen() {
        Platform.runLater(() -> {
            ProfileScene profileScene = new ProfileScene(this::showMainMenu);
            stage.setScene(profileScene.getScene());
        });
    }

    /**
     * Displays the exit scene, allowing the user to exit the application
     */
    private void showExitScreen() {
        Platform.runLater(() -> {
            ExitScene exitScene = new ExitScene();
            stage.setScene(exitScene.getScene());
        });
    }
}
