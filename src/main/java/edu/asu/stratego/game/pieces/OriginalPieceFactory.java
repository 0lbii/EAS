package edu.asu.stratego.game.pieces;

public class OriginalPieceFactory implements PieceFactory {

    @Override
    public OriginalPiece createOriginalPiece(PieceType type, PieceColor color, boolean isOpponent) {
        return new OriginalPiece(type, color, isOpponent);
    }

}
