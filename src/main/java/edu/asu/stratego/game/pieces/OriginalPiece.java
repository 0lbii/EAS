package edu.asu.stratego.game.pieces;

public class OriginalPiece extends Piece {

    /**
     * Constructor for the OriginalPiece class.
     *
     * @param type            The type of the piece.
     * @param color           The color of the piece.
     * @param isOpponentPiece A boolean indicating if the piece belongs to the
     *                        opponent.
     */
    public OriginalPiece(PieceType type, PieceColor color, boolean isOpponentPiece) {
        super(type, color, isOpponentPiece);
    }

    /**
     * Sets the Piece's image sprite according to the type of the piece, the
     * player's color, and whether or not the piece belongs to the opponent.
     */
    public void setPieceImage() {
        String colorPrefix = (this.color == PieceColor.RED) ? "RED" : "BLUE";
        if (this.isOpponentPiece) {
            this.spriteKey = colorPrefix + "_BACK";
        } else {
            switch (type) {
                case SCOUT:
                    this.spriteKey = colorPrefix + "_02";
                    break;
                case MINER:
                    this.spriteKey = colorPrefix + "_03";
                    break;
                case SERGEANT:
                    this.spriteKey = colorPrefix + "_04";
                    break;
                case LIEUTENANT:
                    this.spriteKey = colorPrefix + "_05";
                    break;
                case CAPTAIN:
                    this.spriteKey = colorPrefix + "_06";
                    break;
                case MAJOR:
                    this.spriteKey = colorPrefix + "_07";
                    break;
                case COLONEL:
                    this.spriteKey = colorPrefix + "_08";
                    break;
                case GENERAL:
                    this.spriteKey = colorPrefix + "_09";
                    break;
                case MARSHAL:
                    this.spriteKey = colorPrefix + "_10";
                    break;
                case BOMB:
                    this.spriteKey = colorPrefix + "_BOMB";
                    break;
                case FLAG:
                    this.spriteKey = colorPrefix + "_FLAG";
                    break;
                case SPY:
                    this.spriteKey = colorPrefix + "_SPY";
                    break;
                default:
                    break;
            }
        }
    }

    /*
     * Static block executed once when the class is first loaded.
     */
    /* static {
        applyCustomValues();
    } */

    /**
     * Applies custom values to PieceType entries.
     */
    /* private static void applyCustomValues() {
        PieceType.SCOUT.setValue(11);
        PieceType.MINER.setValue(11);
        PieceType.SERGEANT.setValue(11);
        PieceType.LIEUTENANT.setValue(11);
        PieceType.CAPTAIN.setValue(11);
        PieceType.MAJOR.setValue(11);
        PieceType.COLONEL.setValue(11);
        PieceType.GENERAL.setValue(11);
        PieceType.MARSHAL.setValue(11);
        PieceType.BOMB.setValue(11);
        PieceType.SPY.setValue(11);
        PieceType.FLAG.setValue(11);
    } */
   
}
