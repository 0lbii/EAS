package edu.asu.stratego.gui;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

import edu.asu.stratego.game.Game;
import edu.asu.stratego.game.ResourceBundleManager;
import edu.asu.stratego.languages.LanguageObservable;
import edu.asu.stratego.languages.LanguageObserver;
import edu.asu.stratego.media.ImageConstants;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import models.Player;
import services.GameService;
import services.PlayerService;

public class HistoryScene implements LanguageObserver {

    private final Scene scene;
    private final VBox gamesContainer = new VBox(15);
    private final Label titleLabel = new Label();
    private final Button backButton = new Button();

    private final Button prevButton = new Button("« " + ResourceBundleManager.get("history.previous"));
    private final Button nextButton = new Button(ResourceBundleManager.get("history.next") + " »");

    private static final int SIDE = ClientStage.getSide();

    private List<models.Game> games;
    private int currentPage = 0;
    private static final int PAGE_SIZE = 4;

    public HistoryScene(Runnable onBack) {
        Platform.runLater(() -> LanguageObservable.addObserver(this));

        titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");
        backButton.setStyle("-fx-font-size: 18px; -fx-pref-width: 220px; -fx-pref-height: 45px;");
        gamesContainer.setAlignment(Pos.CENTER);
        gamesContainer.setPadding(new Insets(10));

        // Layout con botones de navegación
        HBox navButtons = new HBox(20, prevButton, nextButton);
        navButtons.setAlignment(Pos.CENTER);

        VBox historyBox = new VBox(20, titleLabel, gamesContainer, navButtons, backButton);
        historyBox.setAlignment(Pos.CENTER);

        ImageView logoImage = new ImageView(ImageConstants.stratego_logo);
        logoImage.setFitWidth(SIDE / 2.0);
        logoImage.setPreserveRatio(true);
        VBox.setMargin(logoImage, new Insets(0, 0, 20, 0));

        VBox content = new VBox(logoImage, historyBox);
        content.setAlignment(Pos.CENTER);

        ImageView backgroundImage = new ImageView(ImageConstants.MAIN_MENU);
        backgroundImage.setFitHeight(SIDE);
        backgroundImage.setFitWidth(SIDE);
        backgroundImage.setPreserveRatio(false);

        StackPane root = new StackPane(backgroundImage, content);
        root.setMaxSize(SIDE, SIDE);
        this.scene = new Scene(root, SIDE, SIDE);

        backButton.setOnAction(e -> onBack.run());

        prevButton.setOnAction(e -> {
            if (currentPage > 0) {
                currentPage--;
                showPage(currentPage);
            }
        });

        nextButton.setOnAction(e -> {
            if ((currentPage + 1) * PAGE_SIZE < games.size()) {
                currentPage++;
                showPage(currentPage);
            }
        });

        Platform.runLater(this::updateTexts);
        loadGamesHistory();
    }

    @Override
    public void updateTexts() {
        titleLabel.setText(ResourceBundleManager.get("menu.history"));
        backButton.setText(ResourceBundleManager.get("menu.back"));
        prevButton.setText("« " + ResourceBundleManager.get("history.previous"));
        nextButton.setText(ResourceBundleManager.get("history.next") + " »");
    }

    public Scene getScene() {
        return scene;
    }

    private void loadGamesHistory() {
        PlayerService playerService = new PlayerService();
        GameService gameService = new GameService();

        String nickname = Game.getPlayer().getNickname();
        Player dbPlayer = playerService.findByNickname(nickname);

        if (dbPlayer == null)
            return;

        games = gameService.findGamesByPlayerNickname(nickname);

        currentPage = 0;

        Platform.runLater(() -> {
            if (games.isEmpty()) {
                gamesContainer.getChildren().clear();
                Label noGamesLabel = new Label(ResourceBundleManager.get("history.nogames"));
                noGamesLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
                gamesContainer.getChildren().add(noGamesLabel);
                prevButton.setDisable(true);
                nextButton.setDisable(true);
            } else {
                showPage(currentPage);
            }
        });
    }

    private void showPage(int page) {
        gamesContainer.getChildren().clear();

        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, games.size());

        Player dbPlayer = new PlayerService().findByNickname(Game.getPlayer().getNickname());

        for (int i = start; i < end; i++) {
            gamesContainer.getChildren().add(createGameCard(games.get(i), dbPlayer));
        }

        prevButton.setDisable(page == 0);
        nextButton.setDisable(end >= games.size());
    }

    private HBox createGameCard(models.Game game, Player player) {
        HBox card = new HBox(20);
        card.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5); -fx-padding: 15; -fx-background-radius: 10;");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(SIDE * 0.8);

        Label dateLabel = new Label(
                game.getEndTime().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)));
        dateLabel.setFont(Font.font(14));
        dateLabel.setTextFill(Color.WHITE);

        Label resultLabel = new Label();
        resultLabel.setFont(Font.font(16));
        if (game.isWasAbandoned()) {
            resultLabel.setText(ResourceBundleManager.get("history.abandoned"));
            resultLabel.setTextFill(Color.GOLD);
        } else if (game.getWinner() == null) {
            resultLabel.setText(ResourceBundleManager.get("history.finished"));
            resultLabel.setTextFill(Color.LIGHTGRAY);
        } else if (game.getWinner().getId().equals(player.getId())) {
            resultLabel.setText(ResourceBundleManager.get("history.won"));
            resultLabel.setTextFill(Color.LIGHTGREEN);
        } else {
            resultLabel.setText(ResourceBundleManager.get("history.lost"));
            resultLabel.setTextFill(Color.INDIANRED);
        }

        Duration duration = Duration.between(game.getStartTime(), game.getEndTime());
        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();

        String durationStr;
        if (minutes > 0) {
            durationStr = String.format("%d %s %d %s",
                    minutes, ResourceBundleManager.get("history.minutes"),
                    seconds, ResourceBundleManager.get("history.seconds"));
        } else {
            durationStr = String.format("%d %s", seconds, ResourceBundleManager.get("history.seconds"));
        }

        Label durationLabel = new Label(durationStr);
        durationLabel.setFont(Font.font(14));
        durationLabel.setTextFill(Color.LIGHTGRAY);

        card.getChildren().addAll(dateLabel, resultLabel, durationLabel);
        return card;
    }
}