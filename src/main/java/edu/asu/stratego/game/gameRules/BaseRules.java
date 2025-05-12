package edu.asu.stratego.game.gameRules;

import java.awt.Point;

import edu.asu.stratego.game.BattleOutcome;
import edu.asu.stratego.game.Move;
import edu.asu.stratego.game.pieces.Piece;
import edu.asu.stratego.game.ServerGameManager;
import edu.asu.stratego.game.board.ServerBoard;
import edu.asu.stratego.game.pieces.PieceColor;

public abstract class BaseRules implements GameRules {

    protected ServerBoard board;

    protected ServerGameManager gameManager;

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
    protected abstract void handleAttackMove(Move move, Piece attacker, Piece defender, BattleOutcome outcome,
            Move moveToPlayerOne, Move moveToPlayerTwo);

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
     * Indicates whether the box corresponds to a lake (non-trafficable area).
     */
    protected boolean isLake(int row, int col) {
        return (col == 2 || col == 3 || col == 6 || col == 7) && (row == 4 || row == 5);
    }

    /**
     * Indicates whether the coordinates are within the board.
     */
    protected boolean isInBounds(int row, int col) {
        return row >= 0 && row <= 9 && col >= 0 && col <= 9;
    }

    /**
     * Checks if there is an enemy piece on the specified square.
     */
    protected boolean isOpponentPiece(int row, int col, PieceColor inColor) {
        Piece piece = board.getSquare(row, col).getPiece();
        return piece != null && piece.getPieceColor() != inColor;
    }

    /**
     * Checks if the specified checkbox is empty.
     */
    protected boolean isNullPiece(int row, int col) {
        return board.getSquare(row, col).getPiece() == null;
    }

    /**
     * Retrieves the piece located at the given point on the board.
     *
     * @param point the coordinates (row, column) on the board.
     * @return the piece at the specified location, or null if the square is empty.
     */
    protected Piece getPieceAt(Point point) {
        return board.getSquare(point.x, point.y).getPiece();
    }

    /**
     * Places a piece at the specified point on the board.
     *
     * @param point the coordinates (row, column) where the piece will be placed.
     * @param piece the piece to place at the given location.
     */
    protected void setPieceAt(Point point, Piece piece) {
        board.getSquare(point.x, point.y).setPiece(piece);
    }

}
