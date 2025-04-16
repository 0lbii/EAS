package edu.asu.stratego.game;

import java.io.Serializable;

/**
 * Represents a single game piece.
 */
public class Piece implements Serializable {

    private static final long serialVersionUID = 7193334048398155856L;
    
    private PieceColor color;
    private PieceType  type;
    
    private boolean isOpponentPiece;
    private String   spriteKey;
    
    /**
     * Creates a new instance of Piece.
     * 
     * @param type PieceType of the piece
     * @param color color of the piece
     * @param isOpponentPiece whether or not the piece belongs to the opponent
     */
    public Piece(PieceType type, PieceColor color, boolean isOpponentPiece) {
        this.isOpponentPiece = isOpponentPiece;
        this.color = color;
        this.type  = type;
        setPieceImage();
    }
    
    /**
     * Sets the Piece's image sprite according to the type of the piece, the 
     * player's color, and whether or not the piece belongs to the opponent.
     */
    private void setPieceImage() {
        String colorPrefix = (this.color == PieceColor.RED) ? "RED" : "BLUE";
        if (this.isOpponentPiece) {
            this.spriteKey = colorPrefix + "_BACK";
        } else {
            switch (type) {
                case SCOUT:      this.spriteKey = colorPrefix + "_02";   break;
                case MINER:      this.spriteKey = colorPrefix + "_03";   break;
                case SERGEANT:   this.spriteKey = colorPrefix + "_04";   break;
                case LIEUTENANT: this.spriteKey = colorPrefix + "_05";   break;
                case CAPTAIN:    this.spriteKey = colorPrefix + "_06";   break;
                case MAJOR:      this.spriteKey = colorPrefix + "_07";   break;
                case COLONEL:    this.spriteKey = colorPrefix + "_08";   break;
                case GENERAL:    this.spriteKey = colorPrefix + "_09";   break;
                case MARSHAL:    this.spriteKey = colorPrefix + "_10";   break;
                case BOMB:       this.spriteKey = colorPrefix + "_BOMB"; break;
                case FLAG:       this.spriteKey = colorPrefix + "_FLAG"; break;
                case SPY:        this.spriteKey = colorPrefix + "_SPY";  break;
                default: break;
            }
        }
    }
    
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