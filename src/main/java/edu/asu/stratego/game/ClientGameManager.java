package edu.asu.stratego.game;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.time.LocalDateTime;

import edu.asu.stratego.game.board.ClientSquare;
import edu.asu.stratego.game.pieces.Piece;
import edu.asu.stratego.game.pieces.PieceColor;
import edu.asu.stratego.game.pieces.PieceType;
import edu.asu.stratego.gui.BoardScene;
import edu.asu.stratego.gui.ClientStage;
import edu.asu.stratego.gui.ConnectionScene;
import edu.asu.stratego.gui.board.BoardTurnIndicator;
import edu.asu.stratego.media.ImageConstants;
import edu.asu.stratego.util.AlertUtils;
import edu.asu.stratego.util.HashTables;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class ClientGameManager implements Runnable {

    private static final Logger logger = Logger.getLogger(ClientGameManager.class.getName());

    private static final Object setupPieces = new Object();
    private static final Object sendMove = new Object();
    private static final Object receiveMove = new Object();
    private static final Object waitFade = new Object();
    private static final Object waitVisible = new Object();

    private ObjectOutputStream toServer;
    private ObjectInputStream fromServer;

    private final ClientStage stage;
    private final SceneController sceneController;

    public ClientGameManager(ClientStage stage) {
        this.stage = stage;
        this.sceneController = new SceneController(stage, this);
    }

    @Override
    public void run() {
        connectToServer();
        sceneController.showMainMenu();
    }

    public static Object getSetupPieces() {
        return setupPieces;
    }

    private void connectToServer() {
        ConnectionScene.ConnectToServer connectToServer = new ConnectionScene.ConnectToServer();
        Thread serverConnectThread = new Thread(connectToServer);
        serverConnectThread.setDaemon(true);

        try {
            serverConnectThread.start();
            serverConnectThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Platform.runLater(() -> {
                AlertUtils.showRetryAlert(
                        "Connection problem",
                        "Connection interrupted",
                        "An error occurred while trying to connect to the server. Do you want to try again?",
                        this::connectToServer,
                        Platform::exit);
            });
        }
    }

    private void closeExistingConnection() {
        try {
            if (fromServer != null) {
                fromServer.close();
                fromServer = null;
            }
            if (toServer != null) {
                toServer.close();
                toServer = null;
            }
            ClientSocket.getInstance().close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error closing existing connection", e);
        }
    }

    public void waitForOpponent() {
        Platform.runLater(() -> {
            stage.setWaitingScene();
        });

        try {
            if (ClientSocket.getInstance() != null && !ClientSocket.getInstance().isClosed()) {
                ClientSocket.getInstance().close();
                ClientSocket.setInstance(null);
            }
            ClientSocket.connect(Game.getPlayer().getServerIP(), 4212);

            // I/O Streams
            toServer = new ObjectOutputStream(ClientSocket.getInstance().getOutputStream());
            fromServer = new ObjectInputStream(ClientSocket.getInstance().getInputStream());

            GameStateManager.initializeGameState(fromServer, toServer);

        } catch (IOException | ClassNotFoundException e) {
            Platform.runLater(() -> {
                AlertUtils.showRetryAlert(
                        "Communication problem",
                        "Communication problem with the opponent",
                        "The opponent's information could not be received. Do you want to try again?",
                        this::connectToServer,
                        () -> {
                            closeExistingConnection();
                            sceneController.showMainMenu();
                        });
            });
        }
    }

    public void setupBoard() {
        Platform.runLater(() -> {
            stage.setBoardScene();
        });

        synchronized (setupPieces) {
            try {
                // Wait for the player to set up their pieces.
                setupPieces.wait();
                Game.setStatus(GameStatus.WAITING_OPP);

                // Send initial piece positions to server.
                SetupBoard initial = new SetupBoard();
                initial.getPiecePositions();
                toServer.writeObject(initial);

                // Receive opponent's initial piece positions from server.
                final SetupBoard opponentInitial = (SetupBoard) fromServer.readObject();

                // Place the opponent's pieces on the board.
                Platform.runLater(() -> {
                    for (int row = 0; row < 4; ++row) {
                        for (int col = 0; col < 10; ++col) {
                            ClientSquare square = Game.getBoard().getSquare(row, col);
                            square.setPiece(opponentInitial.getPiece(row, col));
                            if (Game.getPlayer().getColor() == PieceColor.RED)
                                square.getPiecePane().setPiece(ImageConstants.BLUE_BACK);
                            else
                                square.getPiecePane().setPiece(ImageConstants.RED_BACK);
                        }
                    }
                });
            } catch (InterruptedException | IOException | ClassNotFoundException e) {
                logger.log(Level.SEVERE, "Error occurred while setting up the board", e);
                Platform.runLater(() -> {
                    AlertUtils.showRetryAlert(
                            "Configuration problem",
                            "Problem configuring the dashboard",
                            "There was a problem configuring the pieces. Do you want to try again?",
                            this::connectToServer,
                            Platform::exit);
                });
            }
        }
    }

    public void playGame() {
        initializeGameBoard();
        addAbandonButton();
        GameStateManager.startNewGame();

        // Main loop (when playing)
        while (Game.getStatus() == GameStatus.IN_PROGRESS) {
            try {
                handleTurn();
                if (GameStateManager.wasGameAbandoned() || Game.getMove() == null) {
                    logger.info("Game was abandoned, returning to main menu");
                    handleGameEnd();
                    return;
                }
                processAttackMove();
                updateBoardAndGUI();
            } catch (ClassNotFoundException | IOException | InterruptedException e) {
                Platform.runLater(() -> {
                    AlertUtils.showRetryAlert(
                            "Game problem",
                            "Problem in the game",
                            "An error occurred during the game. Do you want to try again?",
                            this::connectToServer,
                            Platform::exit);
                });
            }
        }

        revealAll();
        handleGameEnd();
    }

    private void handleGameEnd() {
        Platform.runLater(() -> {
            String message = GameStateManager.getEndGameMessage();

            if (GameStateManager.wasGameAbandoned()) {
                clearLocalBoard();
            }

            GameDatabaseManager.saveGameToDatabase(
                    GameStateManager.isCurrentPlayerWinner(),
                    GameStateManager.wasGameAbandoned());

            AlertUtils.showGameEndAlert(
                    "Fin de la partida",
                    message,
                    "¿Quieres jugar otra partida?",
                    () -> {
                        closeExistingConnection();
                        GameStateManager.resetGameState();
                        Platform.runLater(sceneController::showMainMenu);
                    },
                    Platform::exit);

            // Clean up the game scene
            BoardScene.getRootPane().getChildren().clear();
        });
    }

    private void clearLocalBoard() {
        Platform.runLater(() -> {
            for (int row = 0; row < 10; row++) {
                for (int col = 0; col < 10; col++) {
                    ClientSquare square = Game.getBoard().getSquare(row, col);
                    square.getPiecePane().setPiece(null);
                    square.setPiece(null);
                }
            }
        });
    }

    private void addAbandonButton() {
        Platform.runLater(() -> {
            try {
                StackPane root = BoardScene.getRootPane();
                if (root.getChildren().stream().anyMatch(node -> node instanceof Button &&
                        ((Button) node).getText().equals("Abandon Game"))) {
                    return;
                }

                javafx.scene.control.Button abandonButton = new javafx.scene.control.Button("Abandon Game");
                abandonButton.setStyle(
                        "-fx-font-size: 14px; -fx-padding: 5 10; -fx-background-color: #ff4444; -fx-text-fill: white;");
                abandonButton.setOnAction(e -> {
                    try {
                        if (toServer != null) {
                            toServer.writeObject("ABANDON");
                            toServer.flush();
                        }
                    } catch (IOException ex) {
                        logger.log(Level.SEVERE, "Error sending abandon signal", ex);
                    }
                    Platform.runLater(() -> sceneController.showMainMenu());
                });

                StackPane.setAlignment(abandonButton, Pos.TOP_RIGHT);
                StackPane.setMargin(abandonButton, new Insets(10, 10, 0, 0));
                root.getChildren().add(abandonButton);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error adding abandon button", e);
            }
        });
    }

    private void initializeGameBoard() {
        Platform.runLater(() -> {
            BoardScene.getRootPane().getChildren().remove(BoardScene.getSetupPanel());
        });

        try {
            Game.setStatus((GameStatus) fromServer.readObject());
        } catch (ClassNotFoundException | IOException e1) {
            Platform.runLater(() -> {
                AlertUtils.showRetryAlert(
                        "Connection problem",
                        "Connection interrupted",
                        "An error occurred while retrieving the game status. Do you want to try again?",
                        this::connectToServer,
                        Platform::exit);
            });
        }
    }

    private void handleTurn() throws InterruptedException, ClassNotFoundException, IOException {
        Object received = fromServer.readObject();
        GameStateManager.updateGameStatus(received);

        if (received instanceof GameStatus) {
            GameStatus status = (GameStatus) received;
            if (status == GameStatus.RED_DISCONNECTED || status == GameStatus.BLUE_DISCONNECTED) {
                return;
            }
        }

        synchronized (BoardTurnIndicator.getTurnIndicatorTrigger()) {
            BoardTurnIndicator.getTurnIndicatorTrigger().notify();
        }

        if (Game.getPlayer().getColor() == Game.getTurn() && Game.getMoveStatus() != MoveStatus.SERVER_VALIDATION) {
            synchronized (sendMove) {
                sendMove.wait();
                toServer.writeObject(Game.getMove());
                Game.setMoveStatus(MoveStatus.SERVER_VALIDATION);
            }
        }

        received = fromServer.readObject();
        if (received instanceof Move) {
            Game.setMove((Move) received);
        } else if (received instanceof GameStatus) {
            Game.setStatus((GameStatus) received);
        }
    }

    private void processAttackMove() throws InterruptedException, ClassNotFoundException, IOException {
        Piece startPiece = Game.getMove().getStartPiece();
        Piece endPiece = Game.getMove().getEndPiece();

        if (Game.getMove().isAttackMove() == true) {
            Piece attackingPiece = Game.getBoard().getSquare(Game.getMove().getStart().x, Game.getMove().getStart().y)
                    .getPiece();
            if (attackingPiece.getPieceType() == PieceType.SCOUT) {
                int moveX = Game.getMove().getStart().x - Game.getMove().getEnd().x;
                int moveY = Game.getMove().getStart().y - Game.getMove().getEnd().y;

                if (Math.abs(moveX) > 1 || Math.abs(moveY) > 1) {
                    moveScoutAheadOfAttack(moveX, moveY);
                    Thread.sleep(1000);
                    updateScoutServerSide(moveX, moveY);
                    Game.getMove().setStart(Game.getMove().getEnd().x + getShift(moveX),
                            Game.getMove().getEnd().y + getShift(moveY));
                }
            }
            showAttackResult();
        }

        Game.getBoard().getSquare(Game.getMove().getStart().x, Game.getMove().getStart().y).setPiece(startPiece);
        Game.getBoard().getSquare(Game.getMove().getEnd().x, Game.getMove().getEnd().y).setPiece(endPiece);
    }

    private int getShift(int delta) {
        return Integer.compare(0, delta);
    }

    private void moveScoutAheadOfAttack(int moveX, int moveY) {
        Platform.runLater(() -> {
            try {
                int shiftX = getShift(moveX);
                int shiftY = getShift(moveY);

                ClientSquare scoutSquare = Game.getBoard().getSquare(Game.getMove().getEnd().x + shiftX,
                        Game.getMove().getEnd().y + shiftY);
                ClientSquare startSquare = Game.getBoard().getSquare(Game.getMove().getStart().x,
                        Game.getMove().getStart().y);
                scoutSquare.getPiecePane()
                        .setPiece(HashTables.PIECE_MAP.get(startSquare.getPiece().getPieceSpriteKey()));
                startSquare.getPiecePane().setPiece(null);
            } catch (Exception e) {
                Platform.runLater(() -> {
                    AlertUtils.showRetryAlert(
                            "Game problem",
                            "Problem in the game",
                            "An error occurred while trying to move the Scout ahead of the attack. Do you want to try again?",
                            this::connectToServer,
                            Platform::exit);
                });
            }
        });
    }

    private void updateScoutServerSide(int moveX, int moveY) {
        int shiftX = getShift(moveX);
        int shiftY = getShift(moveY);

        ClientSquare startSquare = Game.getBoard().getSquare(Game.getMove().getStart().x, Game.getMove().getStart().y);

        Game.getBoard().getSquare(Game.getMove().getEnd().x + shiftX, Game.getMove().getEnd().y + shiftY)
                .setPiece(startSquare.getPiece());
        Game.getBoard().getSquare(Game.getMove().getStart().x, Game.getMove().getStart().y).setPiece(null);
    }

    private void showAttackResult() throws InterruptedException {
        Platform.runLater(() -> {
            try {
                ClientSquare startSquare = Game.getBoard().getSquare(Game.getMove().getStart().x,
                        Game.getMove().getStart().y);
                ClientSquare endSquare = Game.getBoard().getSquare(Game.getMove().getEnd().x,
                        Game.getMove().getEnd().y);
                Piece animStartPiece = startSquare.getPiece();
                Piece animEndPiece = endSquare.getPiece();
                startSquare.getPiecePane().setPiece(HashTables.PIECE_MAP.get(animStartPiece.getPieceSpriteKey()));
                endSquare.getPiecePane().setPiece(HashTables.PIECE_MAP.get(animEndPiece.getPieceSpriteKey()));
            } catch (Exception e) {
                Platform.runLater(() -> {
                    AlertUtils.showRetryAlert(
                            "Game problem",
                            "Problem in the game",
                            "An error occurred while revealing the pieces involved in the attack. Do you want to try again?",
                            this::connectToServer,
                            Platform::exit);
                });
            }
        });

        Thread.sleep(2000);

        Platform.runLater(() -> {
            try {
                ClientSquare startSquare = Game.getBoard().getSquare(Game.getMove().getStart().x,
                        Game.getMove().getStart().y);
                ClientSquare endSquare = Game.getBoard().getSquare(Game.getMove().getEnd().x,
                        Game.getMove().getEnd().y);
                GameSoundManager.playAttackSound();
                if (Game.getMove().isAttackWin() == false) {
                    fadeOutPiece(startSquare);
                }
                if (Game.getMove().isDefendWin() == false) {
                    fadeOutPiece(endSquare);
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    AlertUtils.showRetryAlert(
                            "Game problem",
                            "Problem in the game",
                            "An error occurred while removing defeated pieces from the board. Do you want to try again?",
                            this::connectToServer,
                            Platform::exit);
                });
            }
        });

        Thread.sleep(1500);
    }

    private void fadeOutPiece(ClientSquare pieceNode) {
        FadeTransition fade = new FadeTransition(Duration.millis(1500), pieceNode.getPiecePane().getPiece());
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.play();
        fade.setOnFinished(new ResetImageVisibility());
    }

    private void updateBoardAndGUI() throws InterruptedException, ClassNotFoundException, IOException {
        Platform.runLater(() -> {
            ClientSquare endSquare = Game.getBoard().getSquare(Game.getMove().getEnd().x, Game.getMove().getEnd().y);
            Piece endPiece = endSquare.getPiece();
            if (endPiece == null)
                endSquare.getPiecePane().setPiece(null);
            else {
                if (endPiece.getPieceColor() == Game.getPlayer().getColor()) {
                    endSquare.getPiecePane().setPiece(HashTables.PIECE_MAP.get(endPiece.getPieceSpriteKey()));
                } else {
                    if (endPiece.getPieceColor() == PieceColor.BLUE)
                        endSquare.getPiecePane().setPiece(ImageConstants.BLUE_BACK);
                    else
                        endSquare.getPiecePane().setPiece(ImageConstants.RED_BACK);
                }
            }
        });

        if (Game.getMove().isAttackMove()) {
            Thread.sleep(50);
        }

        Platform.runLater(() -> {
            ClientSquare arrowSquare = Game.getBoard().getSquare(Game.getMove().getStart().x,
                    Game.getMove().getStart().y);
            if (Game.getMove().getMoveColor() == PieceColor.RED)
                arrowSquare.getPiecePane().setPiece(ImageConstants.MOVEARROW_RED);
            else
                arrowSquare.getPiecePane().setPiece(ImageConstants.MOVEARROW_BLUE);
            if (Game.getMove().getStart().x > Game.getMove().getEnd().x)
                arrowSquare.getPiecePane().getPiece().setRotate(0);
            else if (Game.getMove().getStart().y < Game.getMove().getEnd().y)
                arrowSquare.getPiecePane().getPiece().setRotate(90);
            else if (Game.getMove().getStart().x < Game.getMove().getEnd().x)
                arrowSquare.getPiecePane().getPiece().setRotate(180);
            else
                arrowSquare.getPiecePane().getPiece().setRotate(270);
            FadeTransition ft = new FadeTransition(Duration.millis(1500), arrowSquare.getPiecePane().getPiece());
            ft.setFromValue(1.0);
            ft.setToValue(0.0);
            ft.play();
            ft.setOnFinished(new ResetSquareImage());
        });

        synchronized (waitFade) {
            waitFade.wait();
        }

        Game.setStatus((GameStatus) fromServer.readObject());
    }

    public static Object getSendMove() {
        GameSoundManager.playMoveSound();
        return sendMove;
    }

    public static Object getReceiveMove() {
        return receiveMove;
    }

    private void revealAll() {
        GameSoundManager.playWinSound();
        Platform.runLater(() -> {
            for (int row = 0; row < 10; row++) {
                for (int col = 0; col < 10; col++) {
                    if (Game.getBoard().getSquare(row, col).getPiece() != null && Game.getBoard().getSquare(row, col)
                            .getPiece().getPieceColor() != Game.getPlayer().getColor()) {
                        Game.getBoard().getSquare(row, col).getPiecePane().setPiece(HashTables.PIECE_MAP
                                .get(Game.getBoard().getSquare(row, col).getPiece().getPieceSpriteKey()));
                    }
                }
            }
        });
    }

    private class ResetSquareImage implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            synchronized (waitFade) {
                waitFade.notify();
                Game.getBoard().getSquare(Game.getMove().getStart().x, Game.getMove().getStart().y).getPiecePane()
                        .getPiece().setOpacity(1.0);
                Game.getBoard().getSquare(Game.getMove().getStart().x, Game.getMove().getStart().y).getPiecePane()
                        .getPiece().setRotate(0.0);
                Game.getBoard().getSquare(Game.getMove().getStart().x, Game.getMove().getStart().y).getPiecePane()
                        .setPiece(null);
                Game.getBoard().getSquare(Game.getMove().getEnd().x, Game.getMove().getEnd().y).getPiecePane()
                        .getPiece().setOpacity(1.0);
                Game.getBoard().getSquare(Game.getMove().getEnd().x, Game.getMove().getEnd().y).getPiecePane()
                        .getPiece().setRotate(0.0);
            }
        }
    }

    private class ResetImageVisibility implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            synchronized (waitVisible) {
                waitVisible.notify();
                Game.getBoard().getSquare(Game.getMove().getStart().x, Game.getMove().getStart().y).getPiecePane()
                        .getPiece().setOpacity(1.0);
                Game.getBoard().getSquare(Game.getMove().getStart().x, Game.getMove().getStart().y).getPiecePane()
                        .getPiece().setRotate(0.0);
                Game.getBoard().getSquare(Game.getMove().getStart().x, Game.getMove().getStart().y).getPiecePane()
                        .setPiece(null);
                Game.getBoard().getSquare(Game.getMove().getEnd().x, Game.getMove().getEnd().y).getPiecePane()
                        .getPiece().setOpacity(1.0);
                Game.getBoard().getSquare(Game.getMove().getEnd().x, Game.getMove().getEnd().y).getPiecePane()
                        .getPiece().setRotate(0.0);
            }
        }
    }
}