package edu.asu.stratego.gui.board.setup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.asu.stratego.game.Game;
import edu.asu.stratego.game.pieces.OriginalPiece;
import edu.asu.stratego.game.pieces.PieceColor;
import edu.asu.stratego.game.pieces.PieceType;

public class RandomPlacementStrategy implements PlacementStrategy {
    
    @Override
    public void placePieces(SetupPanel setupPanel) {
        setupPanel.clearBoard();
        
        SetupPieces setupPieces = setupPanel.getSetupPieces();
        
        List<PieceType> availablePieces = new ArrayList<>();
        
        for (PieceType type : PieceType.values()) {
            int pieceCount = setupPieces.getPieceCount(type);
            for (int i = 0; i < pieceCount; i++) {
                availablePieces.add(type);
            }
        }

        List<int[]> availablePositions = new ArrayList<>();
        for (int row = 6; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                availablePositions.add(new int[]{row, col});
            }
        }

        Collections.shuffle(availablePieces);
        Collections.shuffle(availablePositions);

        PieceColor color = Game.getPlayer().getColor();
        for (int i = 0; i < availablePieces.size(); i++) {
            PieceType pieceType = availablePieces.get(i);
            int[] position = availablePositions.get(i);
            int row = position[0];
            int col = position[1];

            if (setupPieces.getPieceCount(pieceType) > 0) {
                try {
                    OriginalPiece piece = new OriginalPiece(pieceType, color, false);
                    
                    Game.getBoard().getSquare(row, col).setPiece(piece);
                    
                    String pieceKey = color.toString() + "_";
                    switch (pieceType) {
                        case SCOUT: pieceKey += "02"; break;
                        case MINER: pieceKey += "03"; break;
                        case SERGEANT: pieceKey += "04"; break;
                        case LIEUTENANT: pieceKey += "05"; break;
                        case CAPTAIN: pieceKey += "06"; break;
                        case MAJOR: pieceKey += "07"; break;
                        case COLONEL: pieceKey += "08"; break;
                        case GENERAL: pieceKey += "09"; break;
                        case MARSHAL: pieceKey += "10"; break;
                        case BOMB: pieceKey += "BOMB"; break;
                        case SPY: pieceKey += "SPY"; break;
                        case FLAG: pieceKey += "FLAG"; break;
                    }
                    
                    Game.getBoard().getSquare(row, col).getPiecePane().setPiece(
                        edu.asu.stratego.util.HashTables.PIECE_MAP.get(pieceKey)
                    );
                    
                    setupPieces.decrementPieceCount(pieceType);
                } catch (Exception e) {
                    System.err.println("Error al colocar pieza: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        synchronized (setupPanel.getUpdateReadyStatus()) {
            setupPanel.getUpdateReadyStatus().notify();
        }
    }
}