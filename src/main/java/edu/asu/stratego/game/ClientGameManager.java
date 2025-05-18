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
import edu.asu.stratego.media.PlaySound;
import edu.asu.stratego.util.AlertUtils;
import edu.asu.stratego.util.HashTables;
import edu.asu.stratego.util.HashTables.SoundType;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import models.GamePlayer;

import services.GamePlayerService;
import services.GameService;
import services.PlayerService;

/**
 * Task to handle the Stratego game on the client-side.
 */
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

    /**
     * Creates a new instance of ClientGameManager
     * 
     * @param stage the stage that the client is set in
     */
    public ClientGameManager(ClientStage stage) {
        this.stage = stage;
        this.sceneController = new SceneController(stage, this);
    }

    /**
     * See ServerGameManager's run() method to understand how the client
     * interacts with the server
     * 
     * @see edu.asu.stratego.Game.ServerGameManager
     */
    @Override
    public void run() {
        connectToServer();
        sceneController.showMainMenu();
    }

    /**
     * @return Object used for communication between the Setup Board GUI and
     *         the ClientGameManager to indicate when the player has finished
     *         setting
     *         up their pieces
     */
    public static Object getSetupPieces() {
        return setupPieces;
    }

    /**
     * Executes the ConnectToServer thread. Blocks the current thread until
     * the ConnectToServer thread terminates
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
     * Closes any existing connection streams and sockets to the server
     * Ensures that resources are properly released
     */
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

    /**
     * Establish I/O streams between the client and the server. Send player
     * information to the server. Then, wait until an object containing player
     * information about the opponent is received from the server
     * 
     * <p>
     * After the player information has been sent and opponent information has
     * been received, the method terminates indicating that it is time to set up
     * the game.
     * </p>
     */
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

            Game.getPlayer().setColor(null);
            Game.setOpponent(null);
            Game.setStatus(GameStatus.SETTING_UP);
            Game.setTurn(PieceColor.RED);
            Game.setMove(new Move());
            Game.setMoveStatus(MoveStatus.OPP_TURN);

            // Exchange of information with the server
            toServer.writeObject(Game.getPlayer());
            Game.setOpponent((Player) fromServer.readObject());

            // Infer the player's color
            if (Game.getOpponent().getColor() == PieceColor.RED)
                Game.getPlayer().setColor(PieceColor.BLUE);
            else
                Game.getPlayer().setColor(PieceColor.RED);

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

    /**
     * Switches to the game setup scene. Players will place their pieces to
     * their initial starting positions. Once the pieces are placed, their
     * =======
     * Switches to the game setup scene. Players will place their pieces to
     * their initial starting positions. Once the pieces are placed, their
     * >>>>>>> 905380814e461334e371dc85a26d0c2a01e12ebd
     * positions are sent to the server
     */
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

    /**
     * Starts the game loop. The game loop handles the main game logic,
     * including player turns, moves, and game status updates
     */
    public void playGame() {
        initializeGameBoard();
        addAbandonButton();
        Game.setStartTime(LocalDateTime.now());

        // Main loop (when playing)
        while (Game.getStatus() == GameStatus.IN_PROGRESS) {

            try {
                handleTurn();
                if (Game.getStatus() == GameStatus.RED_DISCONNECTED ||
                        Game.getStatus() == GameStatus.BLUE_DISCONNECTED ||
                        Game.getMove() == null) {
                    logger.info("Game was abandoned, returning to main menu");
                    handleGameEnd();
                    return;
                }
                processAttackMove();
                updateBoardAndGUI();
            } catch (ClassNotFoundException | IOException | InterruptedException e) {
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
        handleGameEnd();
    }

    /**
     * Handles the end of the game, including displaying the result and
     * saving the game to the database
     */
    private void handleGameEnd() {
        Platform.runLater(() -> {
            String message = "";
            boolean isWinner = false;

            if (Game.getStatus() == GameStatus.RED_CAPTURED ||
                    Game.getStatus() == GameStatus.RED_NO_MOVES) {
                message = (Game.getPlayer().getColor() == PieceColor.BLUE) ? "¡Has ganado!" : "Has perdido";
                isWinner = Game.getPlayer().getColor() == PieceColor.BLUE;
            } else if (Game.getStatus() == GameStatus.BLUE_CAPTURED ||
                    Game.getStatus() == GameStatus.BLUE_NO_MOVES) {
                message = (Game.getPlayer().getColor() == PieceColor.RED) ? "¡Has ganado!" : "Has perdido";
                isWinner = Game.getPlayer().getColor() == PieceColor.RED;
            } else if (Game.getStatus() == GameStatus.RED_DISCONNECTED ||
                    Game.getStatus() == GameStatus.BLUE_DISCONNECTED) {
                message = "El oponente ha abandonado la partida";
                clearLocalBoard();
            }

            // Save the game to the database
            saveGameToDatabase(isWinner, Game.getStatus() == GameStatus.RED_DISCONNECTED
                    || Game.getStatus() == GameStatus.BLUE_DISCONNECTED);

            AlertUtils.showGameEndAlert(
                    "Fin de la partida",
                    message,
                    "¿Quieres jugar otra partida?",
                    () -> {
                        closeExistingConnection();
                        Game.resetGame();
                        Platform.runLater(sceneController::showMainMenu);
                    },
                    Platform::exit);

            // Clean up the game scene
            BoardScene.getRootPane().getChildren().clear();
        });
    }

    /**
     * Saves the game to the database, including player information and game status
     * 
     * @param isWinner     Indicates if the current player is the winner
     * @param wasAbandoned Indicates if the game was abandoned
     */
    private void saveGameToDatabase(boolean isWinner, boolean wasAbandoned) {
        try {
            GameService gameService = new GameService();
            PlayerService playerService = new PlayerService();
            GamePlayerService gamePlayerService = new GamePlayerService();

            // 1. Search for players in the database by their nickname
            models.Player currentPlayer = playerService.findByNickname(Game.getPlayer().getNickname());
            models.Player opponentPlayer = playerService.findByNickname(Game.getOpponent().getNickname());

            if (currentPlayer == null || opponentPlayer == null) {
                logger.warning("No se pudo encontrar uno o ambos jugadores en la BD");
                return;
            }

            // 2. Create and configure the new game
            models.Game game = new models.Game();
            game.setFinished(true);
            game.setStartTime(Game.getStartTime());
            game.setEndTime(LocalDateTime.now());
            game.setWinner(isWinner ? currentPlayer : opponentPlayer);
            game.setWasAbandoned(wasAbandoned);

            // 3. Save the game to the database
            gameService.saveGame(game);

            // 4.Creating relationships between players and the game
            GamePlayer currentGP = new GamePlayer();
            currentGP.setGame(game);
            currentGP.setPlayer(currentPlayer);
            currentGP.setRedTeam(Game.getPlayer().getColor() == PieceColor.RED);

            GamePlayer opponentGP = new GamePlayer();
            opponentGP.setGame(game);
            opponentGP.setPlayer(opponentPlayer);
            opponentGP.setRedTeam(Game.getOpponent().getColor() == PieceColor.RED);

            gamePlayerService.saveGamePlayer(currentGP);
            gamePlayerService.saveGamePlayer(opponentGP);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al guardar partida en BD", e);
        }
    }

    /**
     * Visually and memory clears the home player's board.
     */
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

    /**
     * Adds an "Abandon Game" button to the game scene. When clicked, it sends an
     * abandon signal to the server and returns to the main menu
     */
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
                    // Send abandon signal to the server
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

    /**
     * Initializes the game board by setting up the UI components,
     * loading any necessary resources, and preparing the initial game state
     * for the start of a new match
     */
    private void initializeGameBoard() {
        // Remove setup panel
        Platform.runLater(() -> {
            BoardScene.getRootPane().getChildren().remove(BoardScene.getSetupPanel());
        });

        // Get game status from the server
        try {
            Game.setStatus((GameStatus) fromServer.readObject());
        } catch (ClassNotFoundException | IOException e1) {
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

    /**
     * Handles the logic for a player's turn during the game
     * This includes receiving moves, processing game state updates,
     * and managing turn synchronization between client and server
     * 
     * @throws InterruptedException   if the thread is interrupted while waiting
     * @throws ClassNotFoundException if the received object from the stream is of
     *                                an unknown class
     * @throws IOException            if an I/O error occurs during communication
     *                                with the server
     */
    private void handleTurn() throws InterruptedException, ClassNotFoundException, IOException {
        // Get message from server
        Object received = fromServer.readObject();

        // Check if it's a game status (like abandon)
        if (received instanceof GameStatus) {
            GameStatus status = (GameStatus) received;
            if (status == GameStatus.RED_DISCONNECTED || status == GameStatus.BLUE_DISCONNECTED) {
                Game.setStatus(status);
                return;
            }
        }

        // Otherwise it should be the turn color
        Game.setTurn((PieceColor) received);

        // If the turn is the client's, set move status to none selected
        if (Game.getPlayer().getColor() == Game.getTurn()) {
            Game.setMoveStatus(MoveStatus.NONE_SELECTED);
        } else {
            Game.setMoveStatus(MoveStatus.OPP_TURN);
        }

        // Notify turn indicator
        synchronized (BoardTurnIndicator.getTurnIndicatorTrigger()) {
            BoardTurnIndicator.getTurnIndicatorTrigger().notify();
        }

        // Send move to the server if it's our turn
        if (Game.getPlayer().getColor() == Game.getTurn() && Game.getMoveStatus() != MoveStatus.SERVER_VALIDATION) {
            synchronized (sendMove) {
                sendMove.wait();
                toServer.writeObject(Game.getMove());
                Game.setMoveStatus(MoveStatus.SERVER_VALIDATION);
            }
        }

        // Receive move from the server
        received = fromServer.readObject();
        if (received instanceof Move) {
            Game.setMove((Move) received);
        } else if (received instanceof GameStatus) {
            Game.setStatus((GameStatus) received);
        }
    }

    /**
     * Processes an attack move during the game, handling the logic
     * for resolving combat between pieces, updating the game state,
     * and communicating the result to the server
     *
     * @throws InterruptedException   if the thread is interrupted while waiting
     * @throws ClassNotFoundException if a received object is of an unknown class
     * @throws IOException            if an I/O error occurs during server
     *                                communication
     */
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

    /**
     * Calculates the direction shift based on the given delta
     * Returns -1 if delta is positive, 1 if delta is negative, or 0 if delta is
     * zero
     * 
     * @param delta the difference value to evaluate
     * @return an integer indicating the direction shift (-1, 0, or 1)
     */
    private int getShift(int delta) {
        return Integer.compare(0, delta); // Returns 1 if delta > 0, -1 if delta < 0, 0 if 0
    }

    /**
     * Moves the scout piece ahead of an attack based on the given move offsets
     * 
     * @param moveX the movement in the X direction (rows)
     * @param moveY the movement in the Y direction (columns)
     */
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

    /**
     * Updates the scout piece position on the server side by calculating
     * the direction of movement using the given offsets
     * 
     * @param moveX the movement offset in the X direction (rows)
     * @param moveY the movement offset in the Y direction (columns)
     */
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

    /**
     * Displays the result of an attack to the player
     * This method may pause the execution temporarily to allow the player to view
     * the outcome
     * 
     * @throws InterruptedException if the thread is interrupted while waiting
     */
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

    /**
     * Applies a fade-out animation effect to the given game piece
     * 
     * @param pieceNode the visual representation of the game piece to fade out
     */
    private void fadeOutPiece(ClientSquare pieceNode) {
        FadeTransition fade = new FadeTransition(Duration.millis(1500), pieceNode.getPiecePane().getPiece());
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.play();
        fade.setOnFinished(new ResetImageVisibility());
    }

    /**
     * Updates the game board state and refreshes the graphical user interface
     * accordingly
     * This method may involve communication with the server and can throw
     * exceptions related to thread interruption, class deserialization, and I/O
     * errors
     * 
     * @throws InterruptedException   if the thread is interrupted during execution
     * @throws ClassNotFoundException if a class required during deserialization is
     *                                not found
     * @throws IOException            if an input/output error occurs during
     *                                communication
     */
    private void updateBoardAndGUI() throws InterruptedException, ClassNotFoundException, IOException {
        // Update GUI.
        Platform.runLater(() -> {
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

    /**
     * Returns the object used to synchronize when a move is sent to the server
     * Also plays the move sound effect when called
     * 
     * @return the synchronization object for sending moves
     */
    public static Object getSendMove() {
        PlaySound.playEffect(SoundType.MOVE, 100);
        return sendMove;
    }

    /**
     * Returns the object used to synchronize when a move is received from the
     * server
     * 
     * @return the synchronization object for receiving moves
     */
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
    // untested though
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

    // Finicky, ill-advised to edit. Resets the opacity, rotation, and piece to null
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