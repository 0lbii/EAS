package edu.asu.stratego.game.pieces;

public interface PieceFactory {

    OriginalPiece createOriginalPiece(PieceType type, PieceColor color, boolean isOpponent);

}
