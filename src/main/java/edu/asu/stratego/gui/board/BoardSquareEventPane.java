package edu.asu.stratego.gui.board;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.effect.Glow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

import edu.asu.stratego.game.ClientGameManager;
import edu.asu.stratego.game.Game;
import edu.asu.stratego.game.GameStatus;
import edu.asu.stratego.game.MoveStatus;
import edu.asu.stratego.game.pieces.OriginalPieceFactory;
import edu.asu.stratego.game.pieces.Piece;
import edu.asu.stratego.game.board.ClientSquare;
import edu.asu.stratego.game.pieces.PieceColor;
import edu.asu.stratego.game.pieces.PieceType;
import edu.asu.stratego.gui.board.setup.SetupPanel;
import edu.asu.stratego.media.ImageConstants;
import edu.asu.stratego.util.HashTables;
import edu.asu.stratego.game.pieces.PieceFactory;

/**
 * A single square within the BoardEventPane.
 */
public class BoardSquareEventPane extends GridPane {

    private static ArrayList<Point> validMoves;
    private ImageView hover;

    // Constants for board size and move limits
    private static final int BOARD_SIZE = 10;
    private static final int SCOUT_MOVE_RANGE = 8;
    private static final int REGULAR_MOVE_RANGE = 1;

    private SetupPanel setupPanel;

    /**
     * Creates a new instance of BoardSquareEventPane.
     */
    public BoardSquareEventPane() {
        hover = new ImageView(ImageConstants.HIGHLIGHT_NONE);

        // Event handlers for the square.
        hover.addEventHandler(MouseEvent.MOUSE_ENTERED_TARGET, new OnHover());
        hover.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, new OffHover());
        hover.addEventHandler(MouseEvent.MOUSE_CLICKED, new SelectSquare());

        this.getChildren().add(hover);
    }

    /**
     * This event is triggered when the player's cursor is hovering over the
     * BoardSquareEventPane. It changes the hover image to indicate to the user
     * whether or not a square is valid.
     */
    private class OnHover implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent e) {
            ImageView hover = (ImageView) e.getSource();
            int row = GridPane.getRowIndex(hover);
            int col = GridPane.getColumnIndex(hover);

            GameStatus gameStatus = Game.getStatus();
            MoveStatus moveStatus = Game.getMoveStatus();

            // Setting up
            if (gameStatus == GameStatus.SETTING_UP) {
                checkMove(row, col, hover);
            }
            // Waiting for opponent
            else if (gameStatus == GameStatus.WAITING_OPP) {
                invalidMove(hover);
            }
            // In progress
            else if (gameStatus == GameStatus.IN_PROGRESS) {
                if (moveStatus == MoveStatus.OPP_TURN)
                    invalidMove(hover);
                else if (moveStatus == MoveStatus.NONE_SELECTED)
                    checkMove(row, col, hover);
                // START_SELECTED case is handled elsewhere
            }
        }
    }

    /**
     * This event is fired when the cursor leaves the square. It changes the
     * hover image back to its default image: a blank image with a 1% fill.
     */
    private class OffHover implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent e) {
            ImageView hover = (ImageView) e.getSource();
            // Reset hover image to none regardless of game state
            hover.setImage(ImageConstants.HIGHLIGHT_NONE);
        }
    }

    // Set the image to a red highlight indicating an invalid move
    private void invalidMove(ImageView inImage) {
        inImage.setImage(ImageConstants.HIGHLIGHT_INVALID);
    }

    // Set the image to a green highlight indicating a valid move
    private void validMove(ImageView inImage) {
        inImage.setImage(ImageConstants.HIGHLIGHT_VALID);
    }

    // Check if the move is valid and set the hover accordingly
    private void checkMove(int row, int col, ImageView inImage) {
        if (isHoverValid(row, col))
            validMove(inImage);
        else
            invalidMove(inImage);
    }

    /**
     * This event is fired when the player clicks on the event square.
     */
    private class SelectSquare implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent e) {
            // Square position.
            ImageView source = (ImageView) e.getSource();

            int row = GridPane.getRowIndex((Node) source);
            int col = GridPane.getColumnIndex((Node) source);

            // The square and the piece at this position.
            BoardSquarePane squarePane = Game.getBoard()
                    .getSquare(row, col)
                    .getPiecePane();

            ClientSquare square = Game.getBoard().getSquare(row, col);
            Piece squarePiece = square.getPiece();

            // Player color.
            PieceColor playerColor = Game.getPlayer().getColor();

            /* Game Setup Handler */
            if (Game.getStatus() == GameStatus.SETTING_UP && isHoverValid(row, col)) {
                handleSetupPhase(row, col, square, squarePane, squarePiece, playerColor);
            } else if (Game.getStatus() == GameStatus.IN_PROGRESS && Game.getTurn() == playerColor) {
                handleGamePhase(row, col, playerColor);
            }
        }
    }

    /**
     * Handles piece placement during the setup phase
     */
    private void handleSetupPhase(int row, int col, ClientSquare square, BoardSquarePane squarePane,
            Piece squarePiece, PieceColor playerColor) {
        // Get the selected piece (piece type and count) from the SetupPanel.
        PieceType selectedPiece = setupPanel.getSetupPieces().getSelectedPieceType();
        int selectedPieceCount = 0;
        if (selectedPiece != null)
            selectedPieceCount = setupPanel.getSetupPieces().getPieceCount(selectedPiece);

        // If the square contains a piece...
        if (squarePiece != null) {

            // Remove the existing piece if it is the same piece on board as the
            // selected piece (in SetupPanel) or if no piece is selected (in SetupPanel).
            if (squarePiece.getPieceType() == selectedPiece || selectedPiece == null) {
                if (squarePiece.getPieceType() != null)
                    setupPanel.getSetupPieces().incrementPieceCount(squarePiece.getPieceType());
                squarePane.setPiece(null);
                square.setPiece(null);
            }

            // Replace the existing piece with the selected piece (in SetupPanel).
            else if (squarePiece.getPieceType() != selectedPiece && selectedPieceCount > 0) {
                setupPanel.getSetupPieces().decrementPieceCount(selectedPiece);
                setupPanel.getSetupPieces().incrementPieceCount(squarePiece.getPieceType());
                PieceFactory pieceFactory = new OriginalPieceFactory();
                square.setPiece(pieceFactory.createOriginalPiece(selectedPiece, playerColor, false));
                squarePane.setPiece(HashTables.PIECE_MAP.get(square.getPiece().getPieceSpriteKey()));
            }
        }

        // Otherwise, if the square does not contain a piece...
        else {
            // Place a new piece on the square.
            if (selectedPiece != null && selectedPieceCount > 0) {
                PieceFactory pieceFactory = new OriginalPieceFactory();
                square.setPiece(pieceFactory.createOriginalPiece(selectedPiece, playerColor, false));
                squarePane.setPiece(HashTables.PIECE_MAP.get(square.getPiece().getPieceSpriteKey()));
                setupPanel.getSetupPieces().decrementPieceCount(selectedPiece);
            }
        }
    }

    /**
     * Handles piece movement during the game phase
     */
    private void handleGamePhase(int row, int col, PieceColor playerColor) {
        MoveStatus moveStatus = Game.getMoveStatus();

        // If it is the first piece being selected to move (start)
        if (moveStatus == MoveStatus.NONE_SELECTED && isHoverValid(row, col)) {
            Game.getMove().setStart(row, col);

            // Backup opacity check to fix rare race condition
            Game.getBoard().getSquare(row, col).getPiecePane().getPiece().setOpacity(1.0);

            // Update the movestatus to reflect a start has been selected
            Game.setMoveStatus(MoveStatus.START_SELECTED);

            // Calculate and display the valid moves upon selecting the piece
            validMoves = computeValidMoves(row, col);
            displayValidMoves(row, col);
        }
        // If a start piece has already been selected, but user is changing start piece
        else if (moveStatus == MoveStatus.START_SELECTED && !isNullPiece(row, col)) {
            Piece highlightPiece = Game.getBoard().getSquare(row, col).getPiece();
            if (highlightPiece.getPieceColor() == playerColor) {
                Game.getMove().setStart(row, col);

                // Backup opacity check to fix rare race condition
                Game.getBoard().getSquare(row, col).getPiecePane().getPiece().setOpacity(1.0);

                // Calculate and display the valid moves upon selecting the piece
                validMoves = computeValidMoves(row, col);
                displayValidMoves(row, col);
            }
        }

        // If a valid move is selected as the endpoint
        if (moveStatus == MoveStatus.START_SELECTED && isValidMove(row, col)) {
            clearAllHighlights();

            // Set the end location and color in the move
            Game.getMove().setEnd(row, col);
            Game.getMove().setMoveColor(Game.getPlayer().getColor());

            // Change the movestatus to reflect that the end point has been selected
            Game.setMoveStatus(MoveStatus.END_SELECTED);

            synchronized (ClientGameManager.getSendMove()) {
                ClientGameManager.getSendMove().notify();
            }
        }
    }

    /**
     * Clear all highlights from the board
     */
    private void clearAllHighlights() {
        for (int rowClear = 0; rowClear < BOARD_SIZE; ++rowClear) {
            for (int colClear = 0; colClear < BOARD_SIZE; ++colClear) {
                Game.getBoard().getSquare(rowClear, colClear).getEventPane().getHover()
                        .setImage(ImageConstants.HIGHLIGHT_NONE);
                Game.getBoard().getSquare(rowClear, colClear).getEventPane().getHover().setOpacity(1.0);
            }
        }
    }

    /**
     * Checks if a square is a valid move destination
     */
    public boolean isValidMove(int row, int col) {
        // Iterate through validMoves arraylist and check if a square is a valid move
        if (validMoves != null && !validMoves.isEmpty()) {
            for (Point validPoint : validMoves) {
                if (row == validPoint.getX() && col == validPoint.getY())
                    return true;
            }
        }
        return false;
    }

    /**
     * Highlights the selected piece and its valid moves
     */
    private void displayValidMoves(int pieceRow, int pieceCol) {
        // Iterate through and unhighlight/unglow all squares/pieces
        clearAllHighlightsAndEffects();

        // Glow and set a white highlight around the selected piece
        Game.getBoard().getSquare(pieceRow, pieceCol).getPiecePane().getPiece().setEffect(new Glow(0.75));
        Game.getBoard().getSquare(pieceRow, pieceCol).getEventPane().getHover()
                .setImage(ImageConstants.HIGHLIGHT_WHITE);

        // Iterate through all valid moves and highlight respective squares
        for (Point validMove : validMoves) {
            int x = (int) validMove.getX();
            int y = (int) validMove.getY();
            Game.getBoard().getSquare(x, y).getEventPane().getHover().setImage(ImageConstants.HIGHLIGHT_VALID);
            Game.getBoard().getSquare(x, y).getEventPane().getHover().setOpacity(0.5);
        }
    }

    /**
     * Clears all highlights and glow effects from the board
     */
    private void clearAllHighlightsAndEffects() {
        for (int row = 0; row < BOARD_SIZE; ++row) {
            for (int col = 0; col < BOARD_SIZE; ++col) {
                Game.getBoard().getSquare(row, col).getEventPane().getHover().setImage(ImageConstants.HIGHLIGHT_NONE);
                Game.getBoard().getSquare(row, col).getEventPane().getHover().setOpacity(1.0);
                Game.getBoard().getSquare(row, col).getPiecePane().getPiece().setEffect(new Glow(0.0));
            }
        }
    }

    /**
     * Computes all valid moves for a piece at the given position
     */
    private ArrayList<Point> computeValidMoves(int row, int col) {
        Piece piece = Game.getBoard().getSquare(row, col).getPiece();
        PieceType pieceType = piece.getPieceType();

        // Return empty list for immobile pieces
        if (pieceType == PieceType.BOMB || pieceType == PieceType.FLAG) {
            return new ArrayList<>();
        }

        // Set the max distance based on piece type
        int max = (pieceType == PieceType.SCOUT) ? SCOUT_MOVE_RANGE : REGULAR_MOVE_RANGE;

        ArrayList<Point> validMoves = new ArrayList<>();

        // Check in all four directions
        checkDirection(row, col, -1, 0, max, validMoves); // UP
        checkDirection(row, col, 0, 1, max, validMoves); // RIGHT
        checkDirection(row, col, 1, 0, max, validMoves); // DOWN
        checkDirection(row, col, 0, -1, max, validMoves); // LEFT

        return validMoves;
    }

    /**
     * Helper method to check for valid moves in a specific direction
     */
    private void checkDirection(int startRow, int startCol, int rowDelta, int colDelta, int maxDistance,
            ArrayList<Point> validMoves) {
        for (int i = 1; i <= maxDistance; i++) {
            int newRow = startRow + (i * rowDelta);
            int newCol = startCol + (i * colDelta);

            // Check if position is in bounds
            if (!isInBounds(newRow, newCol)) {
                break;
            }

            // Check if the square is a lake
            if (isLake(newRow, newCol)) {
                break;
            }

            // Check if square has a friendly piece (blocking movement)
            if (!isNullPiece(newRow, newCol) && !isOpponentPiece(newRow, newCol)) {
                break;
            }

            // Valid empty square or opponent piece
            validMoves.add(new Point(newRow, newCol));

            // Stop after capturing a piece
            if (!isNullPiece(newRow, newCol) && isOpponentPiece(newRow, newCol)) {
                break;
            }
        }
    }

    /**
     * Returns true if the given square is a lake
     */
    private static boolean isLake(int row, int col) {
        if ((col == 2 || col == 3 || col == 6 || col == 7) && (row == 4 || row == 5)) {
            return true;
        }
        return false;
    }

    /**
     * Returns false if the given square is outside of the board
     */
    private static boolean isInBounds(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    /**
     * Returns true if the piece is the opponent (from the client's perspective)
     */
    private static boolean isOpponentPiece(int row, int col) {
        return Game.getBoard().getSquare(row, col).getPiece().getPieceColor() != Game.getPlayer().getColor();
    }

    /**
     * Returns true if the piece is null
     */
    private static boolean isNullPiece(int row, int col) {
        return Game.getBoard().getSquare(row, col).getPiece() == null;
    }

    /**
     * During the Setup phase of the game, this method randomly places the
     * pieces that have not yet been placed when the Setup Timer hits 0.
     */
    public void randomSetup() {
        PieceColor playerColor = Game.getPlayer().getColor();

        // Iterate through each square in the player's setup area
        for (int col = 0; col < BOARD_SIZE; ++col) {
            for (int row = 6; row < BOARD_SIZE; ++row) {
                BoardSquarePane squarePane = Game.getBoard().getSquare(row, col).getPiecePane();
                ClientSquare square = Game.getBoard().getSquare(row, col);
                Piece squarePiece = square.getPiece();

                // Create an arraylist of all the available values
                ArrayList<PieceType> availTypes = new ArrayList<PieceType>(Arrays.asList(PieceType.values()));

                // If the square is empty, place a random piece
                if (squarePiece == null) {
                    placeRandomPiece(square, squarePane, availTypes, playerColor);
                }
            }
        }

        // Trigger finishSetup so the game will begin
        setupPanel.finishSetup();
    }

    /**
     * Helper method to place a random piece on a square
     */
    private void placeRandomPiece(ClientSquare square, BoardSquarePane squarePane,
            ArrayList<PieceType> availTypes, PieceColor playerColor) {
        PieceType pieceType = null;

        // Find a random piece type with available pieces
        while (pieceType == null && !availTypes.isEmpty()) {
            int randInt = (int) (Math.random() * availTypes.size());
            if (setupPanel.getSetupPieces().getPieceCount(availTypes.get(randInt)) > 0) {
                pieceType = availTypes.get(randInt);
            } else {
                // Remove unavailable piece types
                availTypes.remove(randInt);
            }
        }

        // If we found an available piece type, place it
        if (pieceType != null) {
            PieceFactory pieceFactory = new OriginalPieceFactory();
            square.setPiece(pieceFactory.createOriginalPiece(pieceType, playerColor, false));
            squarePane.setPiece(HashTables.PIECE_MAP.get(square.getPiece().getPieceSpriteKey()));
            setupPanel.getSetupPieces().decrementPieceCount(pieceType);
        }
    }

    /**
     * Indicates whether or not a square is a valid square to click.
     * 
     * @param row row index of the square
     * @param col column index of the square
     * @return true if the square is valid, false otherwise
     */
    private boolean isHoverValid(int row, int col) {
        PieceColor playerColor = Game.getPlayer().getColor();
        GameStatus gameStatus = Game.getStatus();
        MoveStatus moveStatus = Game.getMoveStatus();

        // Lakes are always invalid.
        if (isLake(row, col)) {
            return false;
        }

        // Game state specific validation
        if (gameStatus == GameStatus.SETTING_UP) {
            // The game is setting up and the square is outside of the setup area.
            return row > 5;
        } else if (gameStatus == GameStatus.WAITING_OPP) {
            // The player has finished setting up and is waiting for the opponent.
            return false;
        } else if (gameStatus == GameStatus.IN_PROGRESS) {
            // It's the opponent's turn
            if (moveStatus == MoveStatus.OPP_TURN) {
                return false;
            }

            // Player is selecting first piece to move
            if (moveStatus == MoveStatus.NONE_SELECTED) {
                // Must select a non-null piece of player's color
                Piece piece = Game.getBoard().getSquare(row, col).getPiece();
                return piece != null && piece.getPieceColor() == playerColor;
            }
        }

        return true;
    }

    /**
     * @return the ImageView object that displays the hover image.
     */
    public ImageView getHover() {
        return hover;
    }

    public void setSetupPanel(SetupPanel setupPanel) {
        this.setupPanel = setupPanel;
    }
}