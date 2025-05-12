package edu.asu.stratego.game.pieces;

import java.util.ArrayList;

import edu.asu.stratego.game.Game;

/**
 * Represents the set of pieces for a player in the Stratego game.
 * The pieces are created according to the player's color and the number of
 * pieces per type.
 */
public class PieceSet {

    private ArrayList<Piece> pieces = new ArrayList<>();

    /**
     * Constructs a PieceSet for a given color.
     * 
     * @param color the color of the player (either RED or BLUE)
     */
    public PieceSet(PieceColor color) {
        // Determine whether the pieces belong to the player or the opponent
        boolean isOpponentPiece = !color.equals(Game.getPlayer().getColor());

        // Initialize the pieces according to each PieceType
        for (PieceType type : PieceType.values()) {
            for (int i = 0; i < type.getCount(); ++i) {
                pieces.add(new OriginalPiece(type, color, isOpponentPiece)); // Usando OriginalPiece
            }
        }
    }

    /**
     * @return the list of pieces in the set
     */
    public ArrayList<Piece> getPieces() {
        return pieces;
    }

    /**
     * Adds a new piece to the set.
     * 
     * @param piece the piece to add
     */
    public void addPiece(Piece piece) {
        pieces.add(piece);
    }

    /**
     * Removes a piece from the set.
     * 
     * @param piece the piece to remove
     */
    public void removePiece(Piece piece) {
        pieces.remove(piece);
    }

}
