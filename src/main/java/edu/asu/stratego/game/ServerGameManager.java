package edu.asu.stratego.game;

import java.awt.Point;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.*;

import edu.asu.stratego.game.board.ServerBoard;
import edu.asu.stratego.game.gameRules.OriginalRulesFactory;
import edu.asu.stratego.game.gameRules.RulesFactory;
import edu.asu.stratego.game.pieces.PieceColor;
import edu.asu.stratego.game.pieces.PieceType;
import edu.asu.stratego.game.gameRules.GameRules;
import edu.asu.stratego.util.CoordinateUtils;
import services.PlayerService;
import edu.asu.stratego.game.pieces.Piece;

/**
 * Task to manage a Stratego game between two clients.
 */
public class ServerGameManager implements Runnable {

    private static final Logger logger = Logger.getLogger(ServerGameManager.class.getName());

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

    private Socket socketOne;
    private Socket socketTwo;

    RulesFactory rulesFactory = new OriginalRulesFactory();
    GameRules gameRules;

    /**
     * Creates a new instance of ServerGameManager.
     * 
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
     * 
     * @see edu.asu.stratego.game.ClientGameManager
     */
    @Override
    public void run() {
        createIOStreams();
        exchangePlayers();
        exchangeSetup();

        playGame();
    }

    /**
     * Establish IO object streams to facilitate communication between the
     * client and server.
     */
    private void createIOStreams() {
        try {
            // NOTE: ObjectOutputStreams must be constructed before the
            // ObjectInputStreams so as to prevent a remote deadlock
            toPlayerOne = new ObjectOutputStream(socketOne.getOutputStream());
            fromPlayerOne = new ObjectInputStream(socketOne.getInputStream());
            toPlayerTwo = new ObjectOutputStream(socketTwo.getOutputStream());
            fromPlayerTwo = new ObjectInputStream(socketTwo.getInputStream());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error establishing communication streams.", e);
            // Clean up resources if streams or sockets were created before the exception
            closeConnections();
            // Finish the thread execution
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Closes the socket connections and I/O streams safely.
     */
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
            logger.log(Level.SEVERE, session + "Error while closing connections.", e);
        }
    }

    /**
     * Receive player information from the clients. Determines the players'
     * colors, and sends the player information of the opponents back to the
     * clients.
     */
    private void exchangePlayers() {
        try {
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
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, session + "Error receiving player information: Class not found.", e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, session + "Error in I/O communication with players.", e);
        }
    }

    /**
     * Handles the initial exchange of the setup boards between the two players.
     * Pieces are registered on the server board and rotated 180 degrees for correct
     * orientation.
     */
    private void exchangeSetup() {
        try {
            SetupBoard setupBoardOne = (SetupBoard) fromPlayerOne.readObject();
            SetupBoard setupBoardTwo = (SetupBoard) fromPlayerTwo.readObject();

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
            logger.log(Level.SEVERE, session + "Error during setup exchange.", e);
        }
    }

    /**
     * Main game loop.
     * Receives moves from players in turn, processes them, checks for a win
     * condition,
     * and sends the result back to both players.
     */
    private void playGame() {
        while (true) {
            try {
                // Get the move from the player based on the current turn
                move = getMoveFromPlayer(turn);

                // Initialize the moves that will be sent to each player
                Move moveToPlayerOne = new Move(), moveToPlayerTwo = new Move();

                // Register move on the board
                gameRules.processMove(move, moveToPlayerOne, moveToPlayerTwo);

                // Check if someone has won the game
                GameStatus winCondition = checkWinCondition();

                // Determine winner and award points accordingly
                PlayerService service = new PlayerService();
                try {
                    if (winCondition == GameStatus.RED_NO_MOVES || winCondition == GameStatus.RED_CAPTURED) {
                        models.Player p = service.findByEmail(playerTwo.getEmail());
                        p.setPoints(p.getPoints() + 100);
                        service.savePlayer(p);
                    } else if (winCondition == GameStatus.BLUE_NO_MOVES || winCondition == GameStatus.BLUE_CAPTURED) {
                        models.Player p = service.findByEmail(playerOne.getEmail());
                        p.setPoints(p.getPoints() + 100);
                        service.savePlayer(p);
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error updating player points", e);
                }

                // Send updated moves and game status to both players
                sendMoveToPlayers(moveToPlayerOne, moveToPlayerTwo, winCondition);

                // Change turn color
                if (turn == PieceColor.RED)
                    turn = PieceColor.BLUE;
                else
                    turn = PieceColor.RED;
            } catch (IOException | ClassNotFoundException e) {
                logger.log(Level.SEVERE, session + " Error occurred during network I/O", e);
                return;
            }
        }

    }

    /**
     * Evaluates the current game state to determine if there is a win condition.
     * Checks if either player has no available moves or if their flag has been
     * captured.
     * 
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
     *
     * @param inColor The color of the player to check.
     * @return true if the flag is no longer present, false otherwise.
     */
    private boolean isCaptured(PieceColor inColor) {
        if (playerOne.getColor() == inColor) {
            if (board.getSquare(playerOneFlag.x, playerOneFlag.y).getPiece().getPieceType() != PieceType.FLAG)
                return true;
        }
        if (playerTwo.getColor() == inColor) {
            if (board.getSquare(playerTwoFlag.x, playerTwoFlag.y).getPiece().getPieceType() != PieceType.FLAG)
                return true;
        }

        return false;
    }

    /**
     * Checks if the player has at least one valid move available.
     *
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
     * Player Two’s perspective intact.
     * Sets the move details for both players accordingly.
     *
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
     * player.
     * Rotates the move coordinates for Player One to match the internal board
     * representation.
     *
     * @param turn The current player's color.
     * @return The move received from the appropriate player.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private Move getMoveFromPlayer(PieceColor turn) throws IOException, ClassNotFoundException {
        // Send player turn color to clients
        toPlayerOne.writeObject(turn);
        toPlayerTwo.writeObject(turn);

        // Get turn from client
        if (playerOne.getColor() == turn) {
            move = (Move) fromPlayerOne.readObject();
            move.setStart(CoordinateUtils.rotate180(move.getStart()));
            move.setEnd(CoordinateUtils.rotate180(move.getEnd()));
        } else {
            move = (Move) fromPlayerTwo.readObject();
        }

        return move;
    }

    /**
     * Sends the processed move and current game status to both players.
     *
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

}
