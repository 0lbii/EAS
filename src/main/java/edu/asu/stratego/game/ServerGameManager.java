package edu.asu.stratego.game;

import java.awt.Point;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.*;

import edu.asu.stratego.game.board.ServerBoard;

/**
 * Task to manage a Stratego game between two clients.
 */
public class ServerGameManager implements Runnable {

    private static final Logger logger = Logger.getLogger(ServerGameManager.class.getName());
    
    private final String session;
    
    private ServerBoard board = new ServerBoard();
    
    private ObjectOutputStream toPlayerOne;
    private ObjectOutputStream toPlayerTwo;
    private ObjectInputStream  fromPlayerOne;
    private ObjectInputStream  fromPlayerTwo;
    
    private Player playerOne = new Player();
    private Player playerTwo = new Player();
    
    private Point playerOneFlag;
    private Point playerTwoFlag;
    
    private PieceColor turn;
    private Move move;
    
    private Socket socketOne;
    private Socket socketTwo;
    
    /**
     * Creates a new instance of ServerGameManager.
     * 
     * @param sockOne socket connected to Player 1's client
     * @param sockTwo socket connected to Player 2's client
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
            //       ObjectInputStreams so as to prevent a remote deadlock.
            toPlayerOne   = new ObjectOutputStream(socketOne.getOutputStream());
            fromPlayerOne = new ObjectInputStream(socketOne.getInputStream());
            toPlayerTwo   = new ObjectOutputStream(socketTwo.getOutputStream());
            fromPlayerTwo = new ObjectInputStream(socketTwo.getInputStream());
        }
        catch(IOException e) {
            // Log the error message
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
            if (toPlayerOne != null) toPlayerOne.close();
            if (fromPlayerOne != null) fromPlayerOne.close();
            if (toPlayerTwo != null) toPlayerTwo.close();
            if (fromPlayerTwo != null) fromPlayerTwo.close();
            if (socketOne != null) socketOne.close();
            if (socketTwo != null) socketTwo.close();
        } catch (IOException e) {
            // Log the error message
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
            }
            else {
                playerOne.setColor(PieceColor.BLUE);
                playerTwo.setColor(PieceColor.RED);
            }
            
            toPlayerOne.writeObject(playerTwo);
            toPlayerTwo.writeObject(playerOne);
        }
        catch (ClassNotFoundException e) {
            // Log the error message
            logger.log(Level.SEVERE, session + "Error receiving player information: Class not found.", e);
        }
        catch (IOException e) {
            // Log the error message
            logger.log(Level.SEVERE, session + "Error in I/O communication with players.", e);
        }
    }
    
    private void exchangeSetup() {
        try {
            SetupBoard setupBoardOne = (SetupBoard) fromPlayerOne.readObject();
            SetupBoard setupBoardTwo = (SetupBoard) fromPlayerTwo.readObject();
            
            // Register pieces on the server board.
            for (int row = 0; row < 4; ++row) {
                for (int col = 0; col < 10; ++col) {
                    board.getSquare(row, col).setPiece(setupBoardOne.getPiece(3 - row, 9 - col));
                    board.getSquare(row + 6, col).setPiece(setupBoardTwo.getPiece(row, col));
                    if(setupBoardOne.getPiece(3 - row, 9 - col).getPieceType() == PieceType.FLAG)
                    	playerOneFlag = new Point(row, col);
                    if(setupBoardTwo.getPiece(row, col).getPieceType() == PieceType.FLAG)
                    	playerTwoFlag = new Point(row + 6, col);
                }
            }
            
            // Rotate pieces by 180 degrees.
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
        }
        catch (ClassNotFoundException | IOException e) {
            // Log the error message
            logger.log(Level.SEVERE, session + "Error during setup exchange.", e);
        }
    }
    
    private void playGame() {       
        while (true) {
            try {
                // Get the move from the player based on the current turn.
                move = getMoveFromPlayer(turn);
                
                // Initialize the moves that will be sent to each player.           
                Move moveToPlayerOne = new Move(), moveToPlayerTwo = new Move();

                // Register move on the board.
                processMove(move, moveToPlayerOne, moveToPlayerTwo);

                // Check if someone has won the game.
                GameStatus winCondition = checkWinCondition();

                // Send updated moves and game status to both players.
                sendMoveToPlayers(moveToPlayerOne, moveToPlayerTwo, winCondition);

                // Change turn color.
                if (turn == PieceColor.RED)
                    turn = PieceColor.BLUE;
                else
                    turn = PieceColor.RED;
            }
            catch (IOException | ClassNotFoundException e) {
                // Log the error message
                logger.log(Level.SEVERE, session + " Error occurred during network I/O", e);
                return;
            }
        }
    }

    private Move getMoveFromPlayer(PieceColor turn) throws IOException, ClassNotFoundException {
        // Send player turn color to clients.
        toPlayerOne.writeObject(turn);
        toPlayerTwo.writeObject(turn);
        
        // Get turn from client.
        if (playerOne.getColor() == turn) {
            move = (Move) fromPlayerOne.readObject();
            move.setStart(9-move.getStart().x, 9-move.getStart().y);
            move.setEnd(9-move.getEnd().x, 9-move.getEnd().y);
        }
        else {
            move = (Move) fromPlayerTwo.readObject();
        }
        
        return move;
    }
    
    private void processMove(Move move, Move moveToPlayerOne, Move moveToPlayerTwo) {
        // If it's a normal move (no attack).
        if(board.getSquare(move.getEnd().x, move.getEnd().y).getPiece() == null) {
            Piece piece = board.getSquare(move.getStart().x, move.getStart().y).getPiece();   

            board.getSquare(move.getStart().x, move.getStart().y).setPiece(null);
            board.getSquare(move.getEnd().x, move.getEnd().y).setPiece(piece);

            // Rotate the move 180 degrees before sending.
            rotateMove(move, moveToPlayerOne, moveToPlayerTwo, null, piece, false, false);
        } 
        // If it's an attack move.
        else {
            Piece attackingPiece = board.getSquare(move.getStart().x, move.getStart().y).getPiece();
            Piece defendingPiece = board.getSquare(move.getEnd().x, move.getEnd().y).getPiece();

            BattleOutcome outcome = attackingPiece.getPieceType().attack(defendingPiece.getPieceType());
                	
            moveToPlayerOne.setAttackMove(true);
            moveToPlayerTwo.setAttackMove(true);

            handleAttackMove(move, attackingPiece, defendingPiece, outcome, moveToPlayerOne, moveToPlayerTwo);
        }
    }

    private void handleAttackMove(Move move, Piece attackingPiece, Piece defendingPiece, BattleOutcome outcome, 
                               Move moveToPlayerOne, Move moveToPlayerTwo) {
        switch (outcome) {
            case WIN:
                board.getSquare(move.getEnd().x, move.getEnd().y).setPiece(board.getSquare(move.getStart().x, move.getStart().y).getPiece());
                board.getSquare(move.getStart().x, move.getStart().y).setPiece(null);
                rotateMove(move, moveToPlayerOne, moveToPlayerTwo, null, attackingPiece, true, false);
                break;

            case LOSE:
                board.getSquare(move.getStart().x, move.getStart().y).setPiece(null);
                rotateMove(move, moveToPlayerOne, moveToPlayerTwo, null, defendingPiece, false, true);
                break;

            case DRAW:
                board.getSquare(move.getStart().x, move.getStart().y).setPiece(null);
                board.getSquare(move.getEnd().x, move.getEnd().y).setPiece(null);
                rotateMove(move, moveToPlayerOne, moveToPlayerTwo, null, null, false, false);
                break;    	
        }
    }

    private void rotateMove(Move move, Move moveToPlayerOne, Move moveToPlayerTwo, Piece startPiece, Piece endPiece, boolean attackWin, boolean defendWin) {
        moveToPlayerOne.setStart(new Point(9 - move.getStart().x, 9 - move.getStart().y));
        moveToPlayerOne.setEnd(new Point(9 - move.getEnd().x, 9 - move.getEnd().y));
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

    private void sendMoveToPlayers(Move moveToPlayerOne, Move moveToPlayerTwo, GameStatus winCondition) throws IOException {
        toPlayerOne.writeObject(moveToPlayerOne);
        toPlayerTwo.writeObject(moveToPlayerTwo);

        toPlayerOne.writeObject(winCondition);
        toPlayerTwo.writeObject(winCondition);
    }

    private GameStatus checkWinCondition() {
    	if(!hasAvailableMoves(PieceColor.RED))
    		return GameStatus.RED_NO_MOVES;
    		
    	else if(isCaptured(PieceColor.RED))
    		return GameStatus.RED_CAPTURED;
    	
    	if(!hasAvailableMoves(PieceColor.BLUE))
    		return GameStatus.BLUE_NO_MOVES;
    		
    	else if(isCaptured(PieceColor.BLUE))
    		return GameStatus.BLUE_CAPTURED;
    	
    	return GameStatus.IN_PROGRESS;
    }
    
    private boolean isCaptured(PieceColor inColor) {
    	if(playerOne.getColor() == inColor) {
    		if(board.getSquare(playerOneFlag.x, playerOneFlag.y).getPiece().getPieceType() != PieceType.FLAG) 
    			return true;
    	}
    	if(playerTwo.getColor() == inColor) {
    		if(board.getSquare(playerTwoFlag.x, playerTwoFlag.y).getPiece().getPieceType() != PieceType.FLAG)
    			return true;
    	}

    	return false;
    }
    
    private boolean hasAvailableMoves(PieceColor inColor) {
    	for(int row = 0; row < 10; ++row) {
    		for(int col = 0; col < 10; ++col) {
    			if(board.getSquare(row, col).getPiece() != null && board.getSquare(row, col).getPiece().getPieceColor() == inColor) {
	    			if(computeValidMoves(row, col, inColor).size() > 0) {
	    				return true;
	    			}
    			}
    		}
    	}

    	return false;
    }
    
    private ArrayList<Point> computeValidMoves(int row, int col, PieceColor inColor) {
        int max = (board.getSquare(row, col).getPiece().getPieceType() == PieceType.SCOUT) ? 8 : 1;

        ArrayList<Point> validMoves = new ArrayList<Point>();
    
        // Movement directions: (deltaRow, deltaCol)
        int[][] directions = {
            {-1, 0},    // Negative Row (UP)
            {1, 0},     // Positive Row (DOWN)
            {0, -1},    // Negative Col (LEFT)
            {0, 1}      // Positive Col (RIGHT)
        };
    
        // Loop through each address
        for (int[] direction : directions) {
            int dRow = direction[0];
            int dCol = direction[1];
            
            //Travel the squares in that direction
            for (int i = 1; i <= max; ++i) {
                int newRow = row + dRow * i;
                int newCol = col + dCol * i;
    
                if (!isInBounds(newRow, newCol) || isLake(newRow, newCol) || isOpponentPiece(newRow, newCol, inColor)) {
                    break;
                }    
                if (isNullPiece(newRow, newCol) || isOpponentPiece(newRow, newCol, inColor)) {
                    validMoves.add(new Point(newRow, newCol));
                    if (!isNullPiece(newRow, newCol) && isOpponentPiece(newRow, newCol, inColor))
                        break;                   
                } else {
                    break;
                }
            }
        }
    
        return validMoves;
    }
    
    private static boolean isLake(int row, int col) {
    	if (col == 2 || col == 3 || col == 6 || col == 7) {
            if (row == 4 || row == 5)
                return true;
        }
        
    	return false;
    }
    
    private static boolean isInBounds(int row, int col) {
    	if(row < 0 || row > 9)
    		return false;
    	if(col < 0 || col > 9)
    		return false;
    	
    	return true;
    }

    private boolean isOpponentPiece(int row, int col, PieceColor inColor) {
    	return board.getSquare(row, col).getPiece().getPieceColor() != inColor;
    }
    
    private boolean isNullPiece(int row, int col) {
    	return board.getSquare(row, col).getPiece() == null;
    }
}