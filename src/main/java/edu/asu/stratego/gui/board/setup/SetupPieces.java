package edu.asu.stratego.gui.board.setup;

import java.util.Arrays;
import java.util.EnumMap;

import edu.asu.stratego.game.Game;
import edu.asu.stratego.game.pieces.PieceType;
import edu.asu.stratego.gui.ClientStage;
import edu.asu.stratego.util.HashTables;
import edu.asu.stratego.util.MutableBoolean;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Glow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class SetupPieces {

    private static final int NUM_PIECE_TYPES = 12;
    private static final String LABEL_PREFIX = " x";

    private final EnumMap<PieceType, MutableBoolean> pieceSelected = new EnumMap<>(PieceType.class);
    private final EnumMap<PieceType, Integer> availability = new EnumMap<>(PieceType.class);
    private final EnumMap<PieceType, ImageView> pieceImages = new EnumMap<>(PieceType.class);
    private final EnumMap<PieceType, Label> pieceCount = new EnumMap<>(PieceType.class);

    private PieceType selectedPieceType;
    private final ColorAdjust zeroPieces = new ColorAdjust();
    private boolean allPiecesPlaced;

    private final SetupPanel setupPanel;

    // This method initializes the setup panel with all piece types and their counts
    public SetupPieces(SetupPanel setupPanel) {
        this.setupPanel = setupPanel;
        final double UNIT = ClientStage.getUnit();
        zeroPieces.setSaturation(-1.0);
        selectedPieceType = null;

        String playerColor = Game.getPlayer().getColor().toString();

        String[] pieceSuffix = { "02", "03", "04", "05", "06", "07", "08", "09", "10", "BOMB", "SPY", "FLAG" };
        int[] pieceTypeCount = { 8, 5, 4, 4, 4, 3, 2, 1, 1, 6, 1, 1 };

        PieceType[] pieceTypes = PieceType.values();

        for (int i = 0; i < NUM_PIECE_TYPES; i++) {
            initializePieceType(pieceTypes[i], pieceTypeCount[i], playerColor + "_" + pieceSuffix[i], UNIT, i);
        }
    }

    // Creates and stores the image and label for a given piece type
    private void initializePieceType(PieceType type, int count, String imageKey, double unit, int index) {
        availability.put(type, count);
        pieceSelected.put(type, new MutableBoolean(false));

        Label label = new Label(LABEL_PREFIX + count);
        label.setFont(Font.font("Century Gothic", unit * 0.4));
        label.setTextFill(Color.WHITE);
        pieceCount.put(type, label);

        ImageView image = new ImageView(HashTables.PIECE_MAP.get(imageKey));
        image.setFitHeight(unit * 0.8);
        image.setFitWidth(unit * 0.8);
        GridPane.setColumnIndex(image, index);
        image.addEventHandler(MouseEvent.MOUSE_PRESSED, new SelectPiece());

        pieceImages.put(type, image);
    }

    // Event handler for when a piece image is clicked
    private class SelectPiece implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent e) {
            ImageView clickedImage = (ImageView) e.getSource();

            for (PieceType type : PieceType.values()) {
                if (pieceImages.get(type) != clickedImage) {
                    if (availability.get(type) > 0)
                        pieceImages.get(type).setEffect(new Glow(0.0));
                    pieceSelected.get(type).setFalse();
                } else {
                    boolean isSelected = pieceSelected.get(type).getValue();
                    boolean hasPieces = availability.get(type) > 0;

                    if (!isSelected && hasPieces) {
                        selectedPieceType = type;
                        clickedImage.setEffect(new Glow(1.0));
                        pieceSelected.get(type).setTrue();
                    } else {
                        selectedPieceType = null;
                        if (hasPieces)
                            clickedImage.setEffect(new Glow(0.0));
                        pieceSelected.get(type).setFalse();
                    }
                }
            }
        }
    }

    public PieceType getSelectedPieceType() {
        return selectedPieceType;
    }

    public int getPieceCount(PieceType type) {
        return availability.getOrDefault(type, 0);
    }

    // Increases the available count of a piece type and updates its label
    public void incrementPieceCount(PieceType type) {
        availability.put(type, availability.get(type) + 1);
        pieceCount.get(type).setText(LABEL_PREFIX + availability.get(type));

        if (availability.get(type) == 1)
            pieceImages.get(type).setEffect(new Glow(0.0));

        allPiecesPlaced = false;
        notifyReadyStatus();
    }

    // Decreases the available count of a piece type and updates the GUI accordingly
    public void decrementPieceCount(PieceType type) {
        availability.put(type, availability.get(type) - 1);
        pieceCount.get(type).setText(LABEL_PREFIX + availability.get(type));

        if (availability.get(type) == 0) {
            pieceImages.get(type).setEffect(zeroPieces);
            pieceSelected.get(type).setFalse();
            selectedPieceType = null;
        }

        allPiecesPlaced = availability.values().stream().allMatch(count -> count == 0);
        notifyReadyStatus();
    }

    // Notifies the setup panel when all pieces have been placed or removed
    private void notifyReadyStatus() {
        Object updateReadyStatus = setupPanel.getUpdateReadyStatus();
        synchronized (updateReadyStatus) {
            updateReadyStatus.notify();
        }
    }

    public boolean getAllPiecesPlaced() {
        return allPiecesPlaced;
    }

    // Returns all piece images as an array for display or access
    public ImageView[] getPieceImages() {
        return Arrays.stream(PieceType.values())
                .map(pieceImages::get)
                .toArray(ImageView[]::new);
    }

    // Returns all piece count labels as an array for display or access
    public Label[] getPieceCountLabels() {
        return Arrays.stream(PieceType.values())
                .map(pieceCount::get)
                .toArray(Label[]::new);
    }

}
