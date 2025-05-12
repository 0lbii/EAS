package edu.asu.stratego.game.gameRules;

import java.awt.Point;
import java.util.ArrayList;

import edu.asu.stratego.game.Move;
import edu.asu.stratego.game.pieces.PieceColor;

public interface GameRules {

    void processMove(Move move, Move moveToPlayerOne, Move moveToPlayerTwo);

    ArrayList<Point> computeValidMoves(int row, int col, PieceColor inColor);

}
