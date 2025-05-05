package edu.asu.stratego.game.board;

import edu.asu.stratego.gui.board.BoardEventPane;
import edu.asu.stratego.gui.board.BoardPane;
import edu.asu.stratego.gui.board.BoardSquareType;

/**
 * Representation of a Stratego board.
 */
public class ClientBoard {

    private final BoardPane piecePane;
    private final BoardEventPane eventPane;
    private static final int size = 10;
    private final ClientSquare[][] squares;

    /**
     * Creates a new instance of Board.
     */
    public ClientBoard() {
        // Initialize the board GUI.
        this.squares = new ClientSquare[size][size];
        initializeSquares();
        // Initialize board layers.
        this.piecePane = new BoardPane(this);
        this.eventPane = new BoardEventPane(this);
    }

    private void initializeSquares() {
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                BoardSquareType type = ((row + col) % 2 == 0) ? BoardSquareType.DARK : BoardSquareType.LIGHT;
                squares[row][col] = new ClientSquare(type);
            }
        }
    }

    /**
     * Returns the board square located at (row, col).
     * 
     * @param row board square row
     * @param col board square column
     * @return the square located at (row, col)
     */
    public ClientSquare getSquare(int row, int col) {
        return squares[row][col];
    }

    /**
     * @return the BoardPane.
     */
    public BoardPane getPiecePane() {
        return piecePane;
    }

    /**
     * @return the BoardEventPane.
     */
    public BoardEventPane getEventPane() {
        return eventPane;
    }
}