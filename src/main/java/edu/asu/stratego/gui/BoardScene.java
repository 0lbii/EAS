package edu.asu.stratego.gui;

import edu.asu.stratego.game.Game;
import edu.asu.stratego.gui.board.BoardSquareEventPane;
import edu.asu.stratego.gui.board.BoardTurnIndicator;
import edu.asu.stratego.gui.board.setup.SetupPanel;
import edu.asu.stratego.media.ImageConstants;
import edu.asu.stratego.media.PlaySound;
import edu.asu.stratego.util.HashTables.SoundType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

/**
 * Wrapper class for a JavaFX scene. Contains a scene UI and its associated 
 * event handlers for playing a game of Stratego.
 */
public class BoardScene {
    
    private final double UNIT = ClientStage.getUnit();
    private final int    SIDE = ClientStage.getSide();
    
    private static StackPane root       = new StackPane();
    private static GridPane  setupPanel = new GridPane();
    
    Scene scene;
    
    /**
     * Creates a new instance of BoardScene.
     */
    public BoardScene() {
                
        /* ================ Board Design ================
         * 
         * The scene is divided into a 12 x 12 grid.
         * Each unit represents a 1 x 1 area.
         * 
         * The scene should be about roughly 85% of the 
         * square of the height of the player's screen 
         * resolution.
         * 
         *          = = = = = = = = = = = =
         *          = + + + + + + + + + + =
         *          = + + + + + + + + + + =
         *          = + + + + + + + + + + =
         *          = + + + + + + + + + + =
         *          = + + + + + + + + + + =
         *          = + + + + + + + + + + =
         *          = + + + + + + + + + + =
         *          = + + + + + + + + + + =
         *          = + + + + + + + + + + =
         *          = + + + + + + + + + + =
         *          = = = = = = = = = = = =
         * 
         * Each '=' indicates part of the board border.
         * Each '+' indicates an individual board square.
         * 
         * Part of the border image is semi-transparent so
         * that the scene background color can come through 
         * to indicate which player's turn it is.
         */
        
        // Set the background color (turn indicator).
        BoardTurnIndicator indicator = new BoardTurnIndicator(); // do not eliminate, it makes background not null
        Rectangle background = BoardTurnIndicator.getTurnIndicator();
        
        
        PlaySound.playMusic(SoundType.CORNFIELD, 1);
        
        // Resize the board.
        final int size = 10;
        for (int row = 0; row < size; ++row) {
            for (int col = 0; col < size; ++col) {
                Game.getBoard().getSquare(row, col).getPiecePane().getPiece().setFitHeight(UNIT);
                Game.getBoard().getSquare(row, col).getPiecePane().getPiece().setFitWidth(UNIT);
                Game.getBoard().getSquare(row, col).getEventPane().getHover().setFitHeight(UNIT);
                Game.getBoard().getSquare(row, col).getEventPane().getHover().setFitWidth(UNIT);
            }
        }
        
        // Create the setup panel.
        BoardSquareEventPane boardEventPane = new BoardSquareEventPane();
        SetupPanel setupPanelWrapper = new SetupPanel(boardEventPane);
        boardEventPane.setSetupPanel(setupPanelWrapper);

        for (int row = 0; row < 10; ++row) {
            for (int col = 0; col < 10; ++col) {
                BoardSquareEventPane pane = Game.getBoard().getSquare(row, col).getEventPane();
                pane.setSetupPanel(setupPanelWrapper);
            }
        }

        setupPanel = setupPanelWrapper.getPanel();
        StackPane.setMargin(setupPanel, new Insets(UNIT, 0, 0, 0));
        StackPane.setAlignment(setupPanel, Pos.TOP_CENTER);
        
        // Create the border.
        ImageView border = new ImageView(ImageConstants.BORDER);
        StackPane.setAlignment(border, Pos.CENTER);
        border.setFitHeight(SIDE);
        border.setFitWidth(SIDE);
        
        // Show Board GUI.
        root = new StackPane(background, Game.getBoard().getPiecePane(), 
                             Game.getBoard().getEventPane(), setupPanel, border);
        root.setMaxSize(SIDE, SIDE);
        Game.getBoard().getPiecePane().setAlignment(Pos.CENTER);
        Game.getBoard().getEventPane().setAlignment(Pos.CENTER);
        
        
        scene = new Scene(root, SIDE, SIDE);
    }
    
    public static StackPane getRootPane() {
        return root;
    }
    
    public static GridPane getSetupPanel() {
        return setupPanel;
    }
}