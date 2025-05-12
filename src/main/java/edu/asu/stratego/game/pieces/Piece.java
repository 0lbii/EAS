package edu.asu.stratego.game.pieces;

import java.io.Serializable;

public abstract class Piece implements Serializable {

    private static final long serialVersionUID = 7193334048398155856L;

    protected PieceColor color;
    protected PieceType type;
    protected boolean isOpponentPiece;
    protected String spriteKey;

    /**
     * Creates a new instance of Piece.
     * 
     * @param type            PieceType of the piece.
     * @param color           color of the piece.
     * @param isOpponentPiece whether or not the piece belongs to the opponent.
     */
    public Piece(PieceType type, PieceColor color, boolean isOpponentPiece) {
        this.type = type;
        this.color = color;
        this.isOpponentPiece = isOpponentPiece;
        setPieceImage();
    }

    /**
     * Sets the Piece's image sprite according to the type of the piece, the
     * player's color, and whether or not the piece belongs to the opponent.
     */
    public abstract void setPieceImage();

    /**
     * @return the piece type of the piece.
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * @return the color of the piece.
     */
    public PieceColor getPieceColor() {
        return color;
    }

    /**
     * @return the sprite associated with the type of the piece.
     */
    public String getPieceSpriteKey() {
        return spriteKey;
    }

}
