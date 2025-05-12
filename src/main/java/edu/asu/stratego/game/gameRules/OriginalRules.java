package edu.asu.stratego.game.gameRules;

import java.util.ArrayList;
import java.awt.Point;

import edu.asu.stratego.game.BattleOutcome;
import edu.asu.stratego.game.pieces.Piece;
import edu.asu.stratego.game.Move;
import edu.asu.stratego.game.ServerGameManager;
import edu.asu.stratego.game.board.ServerBoard;
import edu.asu.stratego.game.pieces.PieceColor;
import edu.asu.stratego.game.pieces.PieceType;

public class OriginalRules extends BaseRules {

    /**
     * OriginalRules constructor.
     * 
     * @param board       The game board.
     * @param gameManager The game manager.
     */
    protected OriginalRules(ServerBoard board, ServerGameManager manager) {
        this.board = board;
        this.gameManager = manager;
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
    protected void handleAttackMove(Move move, Piece attacker, Piece defender, BattleOutcome outcome,
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

}
