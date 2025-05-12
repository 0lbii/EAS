package edu.asu.stratego.game;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import edu.asu.stratego.game.pieces.Piece;

import edu.asu.stratego.game.board.ClientSquare;
import edu.asu.stratego.game.pieces.PieceColor;
import edu.asu.stratego.game.pieces.PieceType;
import edu.asu.stratego.gui.BoardScene;
import edu.asu.stratego.gui.ClientStage;
import edu.asu.stratego.gui.ConfigurationScene;
import edu.asu.stratego.gui.ConnectionScene;
import edu.asu.stratego.gui.ExitScene;
import edu.asu.stratego.gui.HistoryScene;
import edu.asu.stratego.gui.MainMenuScene;
import edu.asu.stratego.gui.ProfileScene;
import edu.asu.stratego.gui.board.BoardTurnIndicator;
import edu.asu.stratego.media.ImageConstants;
import edu.asu.stratego.media.PlaySound;
import edu.asu.stratego.util.AlertUtils;
import edu.asu.stratego.util.HashTables;
import edu.asu.stratego.util.HashTables.SoundType;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

/**
 * Task to handle the Stratego game on the client-side.
 */
public class ClientGameManager implements Runnable {

    private static final Logger logger = Logger.getLogger(ClientGameManager.class.getName());

    private static Object setupPieces = new Object();
    private static Object sendMove = new Object();
    private static Object receiveMove = new Object();
    private static Object waitFade = new Object();
    private static Object waitVisible = new Object();

    private ObjectOutputStream toServer;
    private ObjectInputStream fromServer;

    private ClientStage stage;

    private MainMenuScene mainMenuScene;

    /**
     * Creates a new instance of ClientGameManager.
     * 
     * @param stage the stage that the client is set in
     */
    public ClientGameManager(ClientStage stage) {
        this.stage = stage;
    }

    /**
     * See ServerGameManager's run() method to understand how the client
     * interacts with the server.
     * 
     * @see edu.asu.stratego.Game.ServerGameManager
     */
    @Override
    public void run() {
        connectToServer();
        showMainMenu();
    }

    /**
     * @return Object used for communication between the Setup Board GUI and
     *         the ClientGameManager to indicate when the player has finished
     *         setting
     *         up their pieces.
     */
    public static Object getSetupPieces() {
        return setupPieces;
    }

    /**
     * Executes the ConnectToServer thread. Blocks the current thread until
     * the ConnectToServer thread terminates.
     * 
     * @see edu.asu.stratego.gui.ConnectionScene.ConnectToServer
     */
    private void connectToServer() {
        ConnectionScene.ConnectToServer connectToServer = new ConnectionScene.ConnectToServer();
        Thread serverConnectThread = new Thread(connectToServer);
        serverConnectThread.setDaemon(true);

        try {
            serverConnectThread.start();
            serverConnectThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log(Level.SEVERE, "Error occurred while trying to connect to the server", e);
            // Show the error message in the interface
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

    /**
     * Establish I/O streams between the client and the server. Send player
     * information to the server. Then, wait until an object containing player
     * information about the opponent is received from the server.
     * 
     * <p>
     * After the player information has been sent and opponent information has
     * been received, the method terminates indicating that it is time to set up
     * the game.
     * </p>
     */
    private void waitForOpponent() {
        Platform.runLater(() -> {
            stage.setWaitingScene();
        });

        try {
            // I/O Streams.
            toServer = new ObjectOutputStream(ClientSocket.getInstance().getOutputStream());
            fromServer = new ObjectInputStream(ClientSocket.getInstance().getInputStream());

            // Exchange player information.
            toServer.writeObject(Game.getPlayer());
            Game.setOpponent((Player) fromServer.readObject());

            // Infer player color from opponent color.
            if (Game.getOpponent().getColor() == PieceColor.RED)
                Game.getPlayer().setColor(PieceColor.BLUE);
            else
                Game.getPlayer().setColor(PieceColor.RED);
        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Error occurred during opponent communication", e);
            // Show the error message in the interface
            Platform.runLater(() -> {
                AlertUtils.showRetryAlert(
                        "Communication problem",
                        "Communication problem with the opponent",
                        "The opponent's information could not be received. Do you want to try again?",
                        this::connectToServer,
                        Platform::exit);
            });
        }
    }

    /**
     * Displays the main menu scene on the JavaFX application thread.
     * The menu provides options for starting a new game, viewing match
     * history, accessing the user profile, and adjusting settings.
     * When "Nueva partida" or "New Game" is selected, the setup and gameplay
     * sequence begins.
     */
    private void showMainMenu() {
        Platform.runLater(() -> {
            if (mainMenuScene == null) {
                mainMenuScene = new MainMenuScene();
                mainMenuScene.setNewGameAction(() -> {
                    new Thread(() -> {
                        waitForOpponent();
                        setupBoard();
                        playGame();
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
     * Displays the exit scene, allowing the user to exit the application
     */
    private void showExitScreen() {
        Platform.runLater(() -> {
            ExitScene exitScene = new ExitScene();
            stage.setScene(exitScene.getScene());
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
     * Switches to the game setup scene. Players will place their pieces to
     * their initial starting positions. Once the pieces are placed, their
     * =======
     * Switches to the game setup scene. Players will place their pieces to
     * their initial starting positions. Once the pieces are placed, their
     * >>>>>>> 905380814e461334e371dc85a26d0c2a01e12ebd
     * positions are sent to the server.
     */
    private void setupBoard() {
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
                // Show the error message in the interface
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

    private void playGame() {
        initializeGameBoard();

        // Main loop (when playing)
        while (Game.getStatus() == GameStatus.IN_PROGRESS) {

            try {
                handleTurn();
                processAttackMove();
                updateBoardAndGUI();
            } catch (ClassNotFoundException | IOException | InterruptedException e) {
                logger.log(Level.SEVERE, "Error occurred during the game", e);
                // Show the error message in the interface
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
    }

    private void initializeGameBoard() {
        // Remove setup panel
        Platform.runLater(() -> {
            BoardScene.getRootPane().getChildren().remove(BoardScene.getSetupPanel());
        });

        // Get game status from the server
        try {
            Game.setStatus((GameStatus) fromServer.readObject());
        } catch (ClassNotFoundException | IOException e1) {
            logger.log(Level.SEVERE, "Error retrieving game status", e1);
            // Show the error message in the interface
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
        // Get turn color from server.
        Game.setTurn((PieceColor) fromServer.readObject());

        // If the turn is the client's, set move status to none selected.
        if (Game.getPlayer().getColor() == Game.getTurn())
            Game.setMoveStatus(MoveStatus.NONE_SELECTED);
        else
            Game.setMoveStatus(MoveStatus.OPP_TURN);

        // Notify turn indicator.
        synchronized (BoardTurnIndicator.getTurnIndicatorTrigger()) {
            BoardTurnIndicator.getTurnIndicatorTrigger().notify();
        }

        // Send move to the server.
        if (Game.getPlayer().getColor() == Game.getTurn() && Game.getMoveStatus() != MoveStatus.SERVER_VALIDATION) {
            synchronized (sendMove) {
                sendMove.wait();
                toServer.writeObject(Game.getMove());
                Game.setMoveStatus(MoveStatus.SERVER_VALIDATION);
            }
        }

        // Receive move from the server.
        Game.setMove((Move) fromServer.readObject());
    }

    private void processAttackMove() throws InterruptedException, ClassNotFoundException, IOException {
        Piece startPiece = Game.getMove().getStartPiece();
        Piece endPiece = Game.getMove().getEndPiece();

        if (Game.getMove().isAttackMove() == true) {
            Piece attackingPiece = Game.getBoard().getSquare(Game.getMove().getStart().x, Game.getMove().getStart().y)
                    .getPiece();
            if (attackingPiece.getPieceType() == PieceType.SCOUT) {
                // Check if the scout is attacking over more than one square
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

        // Update board with new pieces
        Game.getBoard().getSquare(Game.getMove().getStart().x, Game.getMove().getStart().y).setPiece(startPiece);
        Game.getBoard().getSquare(Game.getMove().getEnd().x, Game.getMove().getEnd().y).setPiece(endPiece);
    }

    private int getShift(int delta) {
        return Integer.compare(0, delta); // Returns 1 if delta > 0, -1 if delta < 0, 0 if 0
    }

    private void moveScoutAheadOfAttack(int moveX, int moveY) {
        Platform.runLater(() -> {
            try {
                int shiftX = getShift(moveX);
                int shiftY = getShift(moveY);

                // Move the scout in front of the piece it's attacking before actually fading
                // out
                ClientSquare scoutSquare = Game.getBoard().getSquare(Game.getMove().getEnd().x + shiftX,
                        Game.getMove().getEnd().y + shiftY);
                ClientSquare startSquare = Game.getBoard().getSquare(Game.getMove().getStart().x,
                        Game.getMove().getStart().y);
                scoutSquare.getPiecePane()
                        .setPiece(HashTables.PIECE_MAP.get(startSquare.getPiece().getPieceSpriteKey()));
                startSquare.getPiecePane().setPiece(null);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error moving the scout ahead of the attack", e);
                // Show the error message in the interface
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

        // Fix the clientside software boards (and move) to reflect new scout location,
        // now attacks like a normal piece
        Game.getBoard().getSquare(Game.getMove().getEnd().x + shiftX, Game.getMove().getEnd().y + shiftY)
                .setPiece(startSquare.getPiece());
        Game.getBoard().getSquare(Game.getMove().getStart().x, Game.getMove().getStart().y).setPiece(null);
    }

    private void showAttackResult() throws InterruptedException {
        Platform.runLater(() -> {
            try {
                // Set the face images visible to both players (from the back that doesn't show
                // piecetype)
                ClientSquare startSquare = Game.getBoard().getSquare(Game.getMove().getStart().x,
                        Game.getMove().getStart().y);
                ClientSquare endSquare = Game.getBoard().getSquare(Game.getMove().getEnd().x,
                        Game.getMove().getEnd().y);
                Piece animStartPiece = startSquare.getPiece();
                Piece animEndPiece = endSquare.getPiece();
                startSquare.getPiecePane().setPiece(HashTables.PIECE_MAP.get(animStartPiece.getPieceSpriteKey()));
                endSquare.getPiecePane().setPiece(HashTables.PIECE_MAP.get(animEndPiece.getPieceSpriteKey()));
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error revealing the pieces involved in the attack", e);
                // Show the error message in the interface
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

        // Wait three seconds (the image is shown to client, then waits 2 seconds)
        Thread.sleep(2000);

        // Fade out pieces that lose (or draw)
        Platform.runLater(() -> {
            try {
                ClientSquare startSquare = Game.getBoard().getSquare(Game.getMove().getStart().x,
                        Game.getMove().getStart().y);
                ClientSquare endSquare = Game.getBoard().getSquare(Game.getMove().getEnd().x,
                        Game.getMove().getEnd().y);
                // If the piece dies, fade it out (also considers a draw, where both "win" are
                // set to false)
                PlaySound.playEffect(SoundType.ATTACK, 100);
                if (Game.getMove().isAttackWin() == false) {
                    fadeOutPiece(startSquare);
                }
                if (Game.getMove().isDefendWin() == false) {
                    fadeOutPiece(endSquare);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error removing defeated pieces from the board", e);
                // Show the error message in the interface
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

        // Wait 1.5 seconds while the image fades out
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
        // Update GUI.
        Platform.runLater(() -> {
            // obselete: ClientSquare startSquare =
            // Game.getBoard().getSquare(Game.getMove().getStart().x,
            // Game.getMove().getStart().y);
            ClientSquare endSquare = Game.getBoard().getSquare(Game.getMove().getEnd().x, Game.getMove().getEnd().y);
            // Get the piece at the end square
            Piece endPiece = endSquare.getPiece();
            // Draw
            if (endPiece == null)
                endSquare.getPiecePane().setPiece(null);
            else {
                // If not a draw, set the end piece to the PieceType face
                if (endPiece.getPieceColor() == Game.getPlayer().getColor()) {
                    endSquare.getPiecePane().setPiece(HashTables.PIECE_MAP.get(endPiece.getPieceSpriteKey()));
                }
                // ...unless it is the opponent's piece which it will display the back instead
                else {
                    if (endPiece.getPieceColor() == PieceColor.BLUE)
                        endSquare.getPiecePane().setPiece(ImageConstants.BLUE_BACK);
                    else
                        endSquare.getPiecePane().setPiece(ImageConstants.RED_BACK);
                }
            }
        });

        // If it is an attack, wait 0.05 seconds to allow the arrow to be visible
        if (Game.getMove().isAttackMove()) {
            Thread.sleep(50);
        }

        Platform.runLater(() -> {
            // Arrow
            ClientSquare arrowSquare = Game.getBoard().getSquare(Game.getMove().getStart().x,
                    Game.getMove().getStart().y);
            // Change the arrow to an image (and depending on what color the arrow should
            // be)
            if (Game.getMove().getMoveColor() == PieceColor.RED)
                arrowSquare.getPiecePane().setPiece(ImageConstants.MOVEARROW_RED);
            else
                arrowSquare.getPiecePane().setPiece(ImageConstants.MOVEARROW_BLUE);
            // Rotate the arrow to show the direction of the move
            if (Game.getMove().getStart().x > Game.getMove().getEnd().x)
                arrowSquare.getPiecePane().getPiece().setRotate(0);
            else if (Game.getMove().getStart().y < Game.getMove().getEnd().y)
                arrowSquare.getPiecePane().getPiece().setRotate(90);
            else if (Game.getMove().getStart().x < Game.getMove().getEnd().x)
                arrowSquare.getPiecePane().getPiece().setRotate(180);
            else
                arrowSquare.getPiecePane().getPiece().setRotate(270);
            // Fade out the arrow
            FadeTransition ft = new FadeTransition(Duration.millis(1500), arrowSquare.getPiecePane().getPiece());
            ft.setFromValue(1.0);
            ft.setToValue(0.0);
            ft.play();
            ft.setOnFinished(new ResetSquareImage());
        });

        // Wait for fade animation to complete before continuing.
        synchronized (waitFade) {
            waitFade.wait();
        }

        // Get game status from server.
        Game.setStatus((GameStatus) fromServer.readObject());
    }

    public static Object getSendMove() {
        PlaySound.playEffect(SoundType.MOVE, 100);
        return sendMove;
    }

    public static Object getReceiveMove() {
        return receiveMove;
    }

    private void revealAll() {
        // End game, reveal all pieces
        PlaySound.playEffect(SoundType.WIN, 100);
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

    // Finicky, ill-advised to edit. Resets the opacity, rotation, and piece to null
    // Duplicate "ResetImageVisibility" class was intended to not set piece to null,
    // untested though.
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

    // read above comments
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