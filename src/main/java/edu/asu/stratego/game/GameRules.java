package edu.asu.stratego.game;

import java.awt.Point;
import java.util.ArrayList;

import edu.asu.stratego.game.board.ServerBoard;

public class GameRules {

    private ServerBoard board;

    private ServerGameManager gameManager;

    /**
     * GameRules constructor.
     * 
     * @param board       The game board.
     * @param gameManager The game manager.
     */
    public GameRules(ServerBoard board, ServerGameManager gameManager) {
        this.board = board;
        this.gameManager = gameManager;
    }

    /**
     * Processes a move made by a player.
     * Determines whether it is a normal move or an attack and acts accordingly.
     * 
     * @param move            The player's original move.
     * @param moveToPlayerOne The transformed move for player 1.
     * @param moveToPlayerTwo The transformed move for player 2.
     */
    public void processMove(Move move, Move moveToPlayerOne, Move moveToPlayerTwo) {
        Piece destinationPiece = getPieceAt(move.getEnd());
        Piece movingPiece = getPieceAt(move.getStart());

        // If it's a normal move (no attack)
        if (destinationPiece == null) {
            setPieceAt(move.getStart(), null);
            setPieceAt(move.getEnd(), movingPiece);
            // Rotate the move 180 degrees before sending
            gameManager.rotateMove(move, moveToPlayerOne, moveToPlayerTwo, null, movingPiece, false, false);
        }
        // If it's an attack move
        else {
            BattleOutcome outcome = movingPiece.getPieceType().attack(destinationPiece.getPieceType());
            moveToPlayerOne.setAttackMove(true);
            moveToPlayerTwo.setAttackMove(true);
            handleAttackMove(move, movingPiece, destinationPiece, outcome, moveToPlayerOne, moveToPlayerTwo);
        }
    }

    /**
     * Handles the consequences of an attack.
     * 
     * @param move            The attacking move made.
     * @param attackingPiece  The attacking piece.
     * @param defendingPiece  The defending piece.
     * @param outcome         The result of the attack (WIN, LOSE, DRAW).
     * @param moveToPlayerOne The adapted move for player 1.
     * @param moveToPlayerTwo The adapted move for player 2.
     */
    private void handleAttackMove(Move move, Piece attacker, Piece defender, BattleOutcome outcome,
            Move moveToPlayerOne, Move moveToPlayerTwo) {
        switch (outcome) {
            case WIN -> {
                setPieceAt(move.getEnd(), attacker);
                setPieceAt(move.getStart(), null);
                gameManager.rotateMove(move, moveToPlayerOne, moveToPlayerTwo, null, attacker, true, false);
            }
            case LOSE -> {
                setPieceAt(move.getStart(), null);
                gameManager.rotateMove(move, moveToPlayerOne, moveToPlayerTwo, null, defender, false, true);
            }
            case DRAW -> {
                setPieceAt(move.getStart(), null);
                setPieceAt(move.getEnd(), null);
                gameManager.rotateMove(move, moveToPlayerOne, moveToPlayerTwo, null, null, false, false);
            }
        }
    }

    /**
     * Calculates the valid moves for a piece according to the game rules.
     * 
     * @param row     The piece's current row.
     * @param col     The piece's current column.
     * @param inColor The piece's color (to distinguish between ally and enemy).
     * @return List of valid coordinates to which the piece can move.
     */
    public ArrayList<Point> computeValidMoves(int row, int col, PieceColor inColor) {
        // Determines the maximum range of the movement
        int max = (board.getSquare(row, col).getPiece().getPieceType() == PieceType.SCOUT) ? 8 : 1;

        // Initialize the list that will store all valid destination squares
        ArrayList<Point> validMoves = new ArrayList<Point>();

        // Movement directions: (deltaRow, deltaCol)
        int[][] directions = {
                { -1, 0 }, // Negative Row (UP)
                { 1, 0 }, // Positive Row (DOWN)
                { 0, -1 }, // Negative Col (LEFT)
                { 0, 1 } // Positive Col (RIGHT)
        };

        // Loop through each address
        for (int[] direction : directions) {
            int dRow = direction[0];
            int dCol = direction[1];

            // Travel the squares in that direction
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

    /**
     * Indicates whether the box corresponds to a lake (non-trafficable area).
     */
    private static boolean isLake(int row, int col) {
        return (col == 2 || col == 3 || col == 6 || col == 7) && (row == 4 || row == 5);
    }

    /**
     * Indicates whether the coordinates are within the board.
     */
    private static boolean isInBounds(int row, int col) {
        return row >= 0 && row <= 9 && col >= 0 && col <= 9;
    }

    /**
     * Checks if there is an enemy piece on the specified square.
     */
    private boolean isOpponentPiece(int row, int col, PieceColor inColor) {
        Piece piece = board.getSquare(row, col).getPiece();
        return piece != null && piece.getPieceColor() != inColor;
    }

    /**
     * Checks if the specified checkbox is empty.
     */
    private boolean isNullPiece(int row, int col) {
        return board.getSquare(row, col).getPiece() == null;
    }

    /**
     * Retrieves the piece located at the given point on the board.
     *
     * @param point the coordinates (row, column) on the board.
     * @return the piece at the specified location, or null if the square is empty.
     */
    private Piece getPieceAt(Point point) {
        return board.getSquare(point.x, point.y).getPiece();
    }

    /**
     * Places a piece at the specified point on the board.
     *
     * @param point the coordinates (row, column) where the piece will be placed.
     * @param piece the piece to place at the given location.
     */
    private void setPieceAt(Point point, Piece piece) {
        board.getSquare(point.x, point.y).setPiece(piece);
    }

}
