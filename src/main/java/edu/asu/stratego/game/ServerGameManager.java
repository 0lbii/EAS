package edu.asu.stratego.game;

import java.awt.Point;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;

import edu.asu.stratego.game.board.ServerBoard;
import edu.asu.stratego.game.gameRules.GameRules;
import edu.asu.stratego.game.gameRules.OriginalRulesFactory;
import edu.asu.stratego.game.gameRules.RulesFactory;
import edu.asu.stratego.game.pieces.Piece;
import edu.asu.stratego.game.pieces.PieceColor;
import edu.asu.stratego.game.pieces.PieceType;
import edu.asu.stratego.util.CoordinateUtils;
import services.GameService;
import services.PlayerService;

/**
 * Task to manage a Stratego game between two clients.
 */
public class ServerGameManager implements Runnable {

    private final String session;

    private ServerBoard board = new ServerBoard();

    private ObjectOutputStream toPlayerOne;
    private ObjectOutputStream toPlayerTwo;
    private ObjectInputStream fromPlayerOne;
    private ObjectInputStream fromPlayerTwo;

    private Player playerOne = new Player();
    private Player playerTwo = new Player();

    private Point playerOneFlag;
    private Point playerTwoFlag;

    private PieceColor turn;
    private Move move;

    private final Socket socketOne;
    private final Socket socketTwo;

    private volatile boolean gameAbandoned = false;

    private boolean gameSaved = false;

    private LocalDateTime startTime;

    RulesFactory rulesFactory = new OriginalRulesFactory();
    GameRules gameRules;

    /**
     * Creates a new instance of ServerGameManager.
     * @param sockOne    socket connected to Player 1's client.
     * @param sockTwo    socket connected to Player 2's client.
     * @param sessionNum the nth game session created by Server.
     * 
     * @see edu.asu.stratego.Server
     */
    public ServerGameManager(Socket sockOne, Socket sockTwo, int sessionNum) {
        this.session = "Session " + sessionNum + ": ";
        this.socketOne = sockOne;
        this.socketTwo = sockTwo;

        if (Math.random() < 0.5)
            this.turn = PieceColor.RED;
        else
            this.turn = PieceColor.BLUE;

        this.gameRules = rulesFactory.createOriginalRules(board, this);
    }

    /**
     * See ClientGameManager's run() method to understand how the server
     * interacts with the client.
     * @see edu.asu.stratego.game.ClientGameManager
     */
    @Override
    public void run() {
        createIOStreams();
        exchangePlayers();
        exchangeSetup();

        this.startTime = LocalDateTime.now();
        playGame();
    }

    private void resetServerBoard() {
        this.board = new ServerBoard(); // Create a new empty board
        this.playerOneFlag = null;
        this.playerTwoFlag = null;
        this.turn = (Math.random() < 0.5) ? PieceColor.RED : PieceColor.BLUE;
        this.move = null;
    }

    /**
     * Establish IO object streams to facilitate communication between the client and server.
     */
    private void createIOStreams() {
        try {
            if (socketOne == null || socketTwo == null) {
                return;
            } if (socketOne.isClosed() || socketTwo.isClosed()) {
                return;
            } if (toPlayerOne == null) {
                toPlayerOne = new ObjectOutputStream(socketOne.getOutputStream());
                toPlayerOne.flush();
            } if (fromPlayerOne == null) {
                fromPlayerOne = new ObjectInputStream(socketOne.getInputStream());
            } if (toPlayerTwo == null) {
                toPlayerTwo = new ObjectOutputStream(socketTwo.getOutputStream());
                toPlayerTwo.flush();
            } if (fromPlayerTwo == null) {
                fromPlayerTwo = new ObjectInputStream(socketTwo.getInputStream());
            }

        } catch (IOException e) {
            closeConnections();
            Thread.currentThread().interrupt();
        }
    }

    /** Closes the socket connections and I/O streams safely.*/
    private void closeConnections() {
        try {
            if (toPlayerOne != null)
                toPlayerOne.close();
            if (fromPlayerOne != null)
                fromPlayerOne.close();
            if (toPlayerTwo != null)
                toPlayerTwo.close();
            if (fromPlayerTwo != null)
                fromPlayerTwo.close();
            if (socketOne != null)
                socketOne.close();
            if (socketTwo != null)
                socketTwo.close();
        } catch (IOException e) {
        }
    }

    /**
     * Receive player information from the clients. Determines the players'
     * colors, and sends the player information of the opponents back to the clients.
     */
    private void exchangePlayers() {
        try {
            if (fromPlayerOne == null || fromPlayerTwo == null) {
                return;
            }

            playerOne = (Player) fromPlayerOne.readObject();
            playerTwo = (Player) fromPlayerTwo.readObject();

            if (Math.random() < 0.5) {
                playerOne.setColor(PieceColor.RED);
                playerTwo.setColor(PieceColor.BLUE);
            } else {
                playerOne.setColor(PieceColor.BLUE);
                playerTwo.setColor(PieceColor.RED);
            }

            toPlayerOne.writeObject(playerTwo);
            toPlayerTwo.writeObject(playerOne);
        } catch (ClassNotFoundException | IOException e) {
        }
    }

    /**
     * Handles the initial exchange of the setup boards between the two players.
     * Pieces are registered on the server board and rotated 180 degrees for correct orientation.
     */
    private void exchangeSetup() {
        try {
            if (fromPlayerOne == null || fromPlayerTwo == null) {
                return;
            }
            SetupBoard setupBoardOne = (SetupBoard) fromPlayerOne.readObject();
            SetupBoard setupBoardTwo = (SetupBoard) fromPlayerTwo.readObject();

            if (setupBoardOne == null || setupBoardTwo == null) {
                return;
            }

            // Register pieces on the server board
            for (int row = 0; row < 4; ++row) {
                for (int col = 0; col < 10; ++col) {
                    board.getSquare(row, col).setPiece(setupBoardOne.getPiece(3 - row, 9 - col));
                    board.getSquare(row + 6, col).setPiece(setupBoardTwo.getPiece(row, col));
                    if (setupBoardOne.getPiece(3 - row, 9 - col).getPieceType() == PieceType.FLAG)
                        playerOneFlag = new Point(row, col);
                    if (setupBoardTwo.getPiece(row, col).getPieceType() == PieceType.FLAG)
                        playerTwoFlag = new Point(row + 6, col);
                }
            }

            // Rotate pieces by 180 degrees
            for (int row = 0; row < 2; ++row) {
                for (int col = 0; col < 10; ++col) {
                    // Player One
                    Piece temp = setupBoardOne.getPiece(row, col);
                    setupBoardOne.setPiece(setupBoardOne.getPiece(3 - row, 9 - col), row, col);
                    setupBoardOne.setPiece(temp, 3 - row, 9 - col);
                    // Player Two
                    temp = setupBoardTwo.getPiece(row, col);
                    setupBoardTwo.setPiece(setupBoardTwo.getPiece(3 - row, 9 - col), row, col);
                    setupBoardTwo.setPiece(temp, 3 - row, 9 - col);
                }
            }

            GameStatus winCondition = checkWinCondition();

            toPlayerOne.writeObject(setupBoardTwo);
            toPlayerTwo.writeObject(setupBoardOne);
            toPlayerOne.writeObject(winCondition);
            toPlayerTwo.writeObject(winCondition);
        } catch (ClassNotFoundException | IOException e) {
        }
    }

    /**
     * Handles game abandonment with different status options
     * @param status the abandonment reason (RED_DISCONNECTED, BLUE_DISCONNECTED, or  DISCONNECTED)
     */
    public synchronized void abandonGame(GameStatus status) {
        if (gameAbandoned) {
            return;
        }
        gameAbandoned = true;

        try {
            // Update points based on abandonment reason
            updatePlayerPoints(status);

            // Send abandonment status to both players
            toPlayerOne.writeObject(status);
            toPlayerTwo.writeObject(status);

            toPlayerOne.flush();
            toPlayerTwo.flush();

        } catch (IOException e) {
        } finally {
            saveGameResult(status);
            resetServerBoard();
            closeConnections();
        }
    }

    /**
     * Updates player points based on game outcome
     * @param winCondition the game status that determines the winner
     */
    private void updatePlayerPoints(GameStatus winCondition) {
        PlayerService service = new PlayerService();
        try {
            // Determine the winning and losing color first
            PieceColor winnerColor;
            PieceColor loserColor;

            switch (winCondition) {
                case RED_NO_MOVES:
                case RED_CAPTURED:
                    winnerColor = PieceColor.BLUE;
                    loserColor = PieceColor.RED;
                    break;

                case BLUE_NO_MOVES:
                case BLUE_CAPTURED:
                    winnerColor = PieceColor.RED;
                    loserColor = PieceColor.BLUE;
                    break;

                case RED_DISCONNECTED:
                    winnerColor = PieceColor.BLUE;
                    loserColor = PieceColor.RED;
                    break;

                case BLUE_DISCONNECTED:
                    winnerColor = PieceColor.RED;
                    loserColor = PieceColor.BLUE;
                    break;

                default:
                    return;
            }

            // Now find out which player has the winning color
            models.Player winner = (playerOne.getColor() == winnerColor) ? service.findByEmail(playerOne.getEmail())
                    : service.findByEmail(playerTwo.getEmail());

            models.Player loser = (playerOne.getColor() == loserColor) ? service.findByEmail(playerOne.getEmail())
                    : service.findByEmail(playerTwo.getEmail());

            // Assign points
            int pointsToAdd = (winCondition == GameStatus.RED_DISCONNECTED ||
                    winCondition == GameStatus.BLUE_DISCONNECTED) ? 50 : 100;

            winner.setPoints(winner.getPoints() + pointsToAdd);
            service.savePlayer(winner);

        } catch (Exception e) {
        }
    }

    /**
     * Handles game abandonment when called without specific status (defaults to DISCONNECTED)
     */
    public synchronized void abandonGame() {
        abandonGame(GameStatus.DISCONNECTED);
    }

    /**
     * Main game loop. Receives moves from players in turn, processes them, checks for a win
     * condition, and sends the result back to both players.
     */
    private void playGame() {
        while (!gameAbandoned) {
            try {
                // Get the move from the player based on the current turn
                move = getMoveFromPlayer(turn);

                // Check if game was abandoned during move reception
                if (gameAbandoned || move == null) {
                    break;
                }

                // Initialize the moves that will be sent to each player
                Move moveToPlayerOne = new Move();
                Move moveToPlayerTwo = new Move();

                // Register move on the board
                gameRules.processMove(move, moveToPlayerOne, moveToPlayerTwo);

                // Check if someone has won the game
                GameStatus winCondition = checkWinCondition();

                // If game is over, update points and send final status
                if (winCondition != GameStatus.IN_PROGRESS) {
                    updatePlayerPoints(winCondition);
                    saveGameResult(winCondition);
                    sendMoveToPlayers(moveToPlayerOne, moveToPlayerTwo, winCondition);
                    break;
                }

                // Send updated moves and game status to both players
                sendMoveToPlayers(moveToPlayerOne, moveToPlayerTwo, winCondition);

                // Change turn color
                turn = (turn == PieceColor.RED) ? PieceColor.BLUE : PieceColor.RED;

            } catch (IOException | ClassNotFoundException e) {
                // If there's an IO error, treat it as abandonment
                abandonGame(GameStatus.DISCONNECTED);
                return;
            }
        }
        closeConnections();
    }

    /**
     * Evaluates the current game state to determine if there is a win condition.
     * Checks if either player has no available moves or if their flag has been
     * captured.
     * @return GameStatus representing the current status of the game.
     */
    private GameStatus checkWinCondition() {
        if (!hasAvailableMoves(PieceColor.RED))
            return GameStatus.RED_NO_MOVES;

        else if (isCaptured(PieceColor.RED))
            return GameStatus.RED_CAPTURED;

        if (!hasAvailableMoves(PieceColor.BLUE))
            return GameStatus.BLUE_NO_MOVES;

        else if (isCaptured(PieceColor.BLUE))
            return GameStatus.BLUE_CAPTURED;

        return GameStatus.IN_PROGRESS;
    }

    /**
     * Checks whether a player's flag has been captured.
     * @param inColor The color of the player to check.
     * @return true if the flag is no longer present, false otherwise.
     */
    private boolean isCaptured(PieceColor inColor) {
        if (playerOne.getColor() == inColor) {
            if (board.getSquare(playerOneFlag.x, playerOneFlag.y).getPiece().getPieceType() != PieceType.FLAG)
                return true;
        } if (playerTwo.getColor() == inColor) {
            if (board.getSquare(playerTwoFlag.x, playerTwoFlag.y).getPiece().getPieceType() != PieceType.FLAG)
                return true;
        }

        return false;
    }

    /**
     * Checks if the player has at least one valid move available.
     * @param inColor The color of the player to check.
     * @return true if at least one move exists, false otherwise.
     */
    private boolean hasAvailableMoves(PieceColor inColor) {
        for (int row = 0; row < 10; ++row) {
            for (int col = 0; col < 10; ++col) {
                if (board.getSquare(row, col).getPiece() != null
                        && board.getSquare(row, col).getPiece().getPieceColor() == inColor) {
                    if (gameRules.computeValidMoves(row, col, inColor).size() > 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Rotates the move coordinates by 180 degrees for Player One, while keeping
     * Player Two’s perspective intact. Sets the move details for both players accordingly.
     * @param move            The original move received.
     * @param moveToPlayerOne Move object populated for Player One (rotated).
     * @param moveToPlayerTwo Move object populated for Player Two (original).
     * @param startPiece      The piece that starts the move.
     * @param endPiece        The piece at the destination.
     * @param attackWin       Whether the attacking piece wins.
     * @param defendWin       Whether the defending piece wins.
     */
    public void rotateMove(Move move, Move moveToPlayerOne, Move moveToPlayerTwo, Piece startPiece, Piece endPiece,
            boolean attackWin, boolean defendWin) {
        moveToPlayerOne.setStart(CoordinateUtils.rotate180(move.getStart()));
        moveToPlayerOne.setEnd(CoordinateUtils.rotate180(move.getEnd()));
        moveToPlayerOne.setMoveColor(move.getMoveColor());
        moveToPlayerOne.setStartPiece(startPiece);
        moveToPlayerOne.setEndPiece(endPiece);
        moveToPlayerOne.setAttackWin(attackWin);
        moveToPlayerOne.setDefendWin(defendWin);

        moveToPlayerTwo.setStart(new Point(move.getStart().x, move.getStart().y));
        moveToPlayerTwo.setEnd(new Point(move.getEnd().x, move.getEnd().y));
        moveToPlayerTwo.setMoveColor(move.getMoveColor());
        moveToPlayerTwo.setStartPiece(startPiece);
        moveToPlayerTwo.setEndPiece(endPiece);
        moveToPlayerTwo.setAttackWin(attackWin);
        moveToPlayerTwo.setDefendWin(defendWin);
    }

    /**
     * Sends the turn color to both players and receives the move from the current
     * player. Rotates the move coordinates for Player One to match the internal board representation. 
     * @param turn The current player's color.
     * @return The move received from the appropriate player.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private Move getMoveFromPlayer(PieceColor turn) throws IOException, ClassNotFoundException {
        // Send player turn color to clients
        toPlayerOne.writeObject(turn);
        toPlayerTwo.writeObject(turn);

        // Get move from client
        Object received = (playerOne.getColor() == turn) ? fromPlayerOne.readObject() : fromPlayerTwo.readObject();

        // Check if it's an abandon signal
        if (received instanceof String && ((String) received).equals("ABANDON")) {
            // Determine which player is abandoning
            GameStatus abandonStatus = (playerOne.getColor() == turn)
                    ? (playerOne.getColor() == PieceColor.RED ? GameStatus.RED_DISCONNECTED
                            : GameStatus.BLUE_DISCONNECTED)
                    : (playerTwo.getColor() == PieceColor.RED ? GameStatus.RED_DISCONNECTED
                            : GameStatus.BLUE_DISCONNECTED);

            abandonGame(abandonStatus);
            return null;
        }

        // Process normal move
        move = (Move) received;
        if (playerOne.getColor() == turn) {
            move.setStart(CoordinateUtils.rotate180(move.getStart()));
            move.setEnd(CoordinateUtils.rotate180(move.getEnd()));
        }
        return move;
    }

    /**
     * Sends the processed move and current game status to both players.
     * @param moveToPlayerOne Move object to send to Player One.
     * @param moveToPlayerTwo Move object to send to Player Two.
     * @param winCondition    Current game status to send to both players.
     * @throws IOException
     */
    private void sendMoveToPlayers(Move moveToPlayerOne, Move moveToPlayerTwo, GameStatus winCondition)
            throws IOException {
        toPlayerOne.writeObject(moveToPlayerOne);
        toPlayerTwo.writeObject(moveToPlayerTwo);

        toPlayerOne.writeObject(winCondition);
        toPlayerTwo.writeObject(winCondition);
    }

    /** Save the game to the DB, for obtaining history information */
    private void saveGameResult(GameStatus status) {
        if (gameSaved)
            return;
        gameSaved = true;

        GameService gameService = new GameService();
        PlayerService playerService = new PlayerService();

        models.Player dbPlayerOne = playerService.findByEmail(playerOne.getEmail());
        models.Player dbPlayerTwo = playerService.findByEmail(playerTwo.getEmail());

        models.Game game = new models.Game();
        game.setStartTime(this.startTime);
        game.setEndTime(java.time.LocalDateTime.now());
        game.setFinished(true);
        game.setWasAbandoned(status == GameStatus.RED_DISCONNECTED || status == GameStatus.BLUE_DISCONNECTED);

        if (!game.isWasAbandoned()) {
            PieceColor winnerColor = switch (status) {
                case RED_NO_MOVES, RED_CAPTURED -> PieceColor.BLUE;
                case BLUE_NO_MOVES, BLUE_CAPTURED -> PieceColor.RED;
                default -> null;
            };
            if (winnerColor != null) {
                game.setWinner(playerOne.getColor() == winnerColor ? dbPlayerOne : dbPlayerTwo);
            }
        }

        models.GamePlayer gp1 = new models.GamePlayer();
        gp1.setPlayer(dbPlayerOne);
        gp1.setRedTeam(playerOne.getColor() == PieceColor.RED);
        gp1.setGame(game);

        models.GamePlayer gp2 = new models.GamePlayer();
        gp2.setPlayer(dbPlayerTwo);
        gp2.setRedTeam(playerTwo.getColor() == PieceColor.RED);
        gp2.setGame(game);

        game.getGamePlayers().add(gp1);
        game.getGamePlayers().add(gp2);

        gameService.saveGame(game);
    }
}
