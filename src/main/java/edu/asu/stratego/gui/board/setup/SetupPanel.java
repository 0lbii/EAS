package edu.asu.stratego.gui.board.setup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.asu.stratego.game.ClientGameManager;
import edu.asu.stratego.game.Game;
import edu.asu.stratego.game.ResourceBundleManager;
import edu.asu.stratego.game.pieces.OriginalPiece;
import edu.asu.stratego.game.pieces.Piece;
import edu.asu.stratego.game.pieces.PieceColor;
import edu.asu.stratego.game.pieces.PieceType;
import edu.asu.stratego.gui.ClientStage;
import edu.asu.stratego.gui.board.BoardSquareEventPane;
import edu.asu.stratego.languages.LanguageObserver;
import edu.asu.stratego.media.ImageConstants;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class SetupPanel implements LanguageObserver{

    private final GridPane setupPanel = new GridPane();
    private final GridPane piecePane = new GridPane();
    private final Object updateReadyStatus = new Object();
    private final StackPane instructionPane = new StackPane();
    private final Label instructions = new Label();
    private final Label readyLabel = new Label();
    private final ImageView readyButton = new ImageView();
    private final Button randomButton = new Button();
    private final SetupPieces setupPieces;
    private final BoardSquareEventPane boardPane;

    /**
     * Creates a new instance of SetupPanel.
     */
    public SetupPanel(BoardSquareEventPane boardPane) {
        this.boardPane = boardPane;
        final double UNIT = ClientStage.getUnit();

        setupPanel.setMaxHeight(UNIT * 4);
        setupPanel.setMaxWidth(UNIT * 10);

        // Panel background.
        String backgroundURL = "edu/asu/stratego/media/images/board/setup_panel.png";
        setupPanel.setStyle("-fx-background-image: url(" + backgroundURL + "); " +
                "-fx-background-size: " + UNIT * 10 + " " + UNIT * 5 + ";" +
                "-fx-background-repeat: stretch;");

        /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
         *                                                                               *
         *                        C R E A T E   U I :   H E A D E R                      *
         *                                                                               *
         * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

        // Stratego logo.
        ImageView logo = new ImageView(ImageConstants.stratego_logo);
        GridPane.setMargin(logo, new Insets(UNIT * 0.15, 0.0, 0.0, UNIT * 0.3));
        logo.setFitHeight(UNIT * 1.25);
        logo.setFitWidth(UNIT * 4.4);

        // Header line.
        Rectangle headerLine = new Rectangle(UNIT * 0.04, UNIT * 1.25);
        headerLine.setFill(new Color(0.4, 0.1, 0.0, 1.0));

        // Nickname Display.
        GridPane headerText = new GridPane();
        headerText.getRowConstraints().add(new RowConstraints(UNIT * 0.6));
        GridPane.setMargin(headerText, new Insets(UNIT * 0.2, 0, 0, UNIT * 0.2));

        services.PlayerService service = new services.PlayerService();
        models.Player localPlayer = service.findByEmail(Game.getPlayer().getEmail());
        models.Player remotePlayer = service.findByEmail(Game.getOpponent().getEmail());

        String localName = (localPlayer != null) ? localPlayer.getNickname() : Game.getPlayer().getNickname();
        String remoteName = (remotePlayer != null) ? remotePlayer.getNickname() : Game.getOpponent().getNickname();

        String titleContent = localName + " vs. " + remoteName;
        
        double fontScale = 1.0 / ((titleContent.length() - 7) / 8 + 2);

        Label nameDisplay = new Label(titleContent);
        nameDisplay.setFont(Font.font("Century Gothic", FontWeight.BOLD, UNIT * fontScale));
        nameDisplay.setTextFill(new Color(1.0, 0.7, 0.0, 1.0));
        nameDisplay.setAlignment(Pos.BOTTOM_LEFT);
        headerText.add(nameDisplay, 0, 0);

        // Setup Timer.
        Label setupTimer = new Label(ResourceBundleManager.get("setup.timer.label"));
        setupTimer.setFont(Font.font("Century Gothic", UNIT / 3));
        setupTimer.setTextFill(new Color(0.9, 0.5, 0.0, 1.0));
        setupTimer.setAlignment(Pos.TOP_LEFT);

        SetupTimer timer = new SetupTimer(boardPane);
        timer.startTimer();

        GridPane timerPane = new GridPane();
        timerPane.add(setupTimer, 0, 1);
        timerPane.add(timer.getLabel(), 1, 1);

        headerText.add(timerPane, 0, 1);

        GridPane headerPane = new GridPane();
        headerPane.getColumnConstraints().add(new ColumnConstraints(UNIT * 5));
        headerPane.add(logo, 0, 0);
        headerPane.add(headerLine, 1, 0);
        headerPane.add(headerText, 2, 0);

        setupPanel.add(headerPane, 0, 0);

        /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
         *                                                                               *
         *                          C R E A T E   U I :   B O D Y                        *
         *                                                                               *
         * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

        setupPieces = new SetupPieces(this);
        ImageView[] pieceImages = setupPieces.getPieceImages();
        Label[] pieceCount = setupPieces.getPieceCountLabels();

        GridPane.setMargin(piecePane, new Insets(UNIT * 0.15, 0.0, 0.0, UNIT * 0.15));

        for (int i = 0; i < 12; ++i) {
            piecePane.add(pieceImages[i], i, 0);
            piecePane.add(pieceCount[i], i, 1);
        }

        setupPanel.add(piecePane, 0, 1);

        /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
         *                                                                               *
         *                       C R E A T E   U I :   F O O T E R                       *
         *                                                                               *
         * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

        GridPane.setMargin(instructionPane, new Insets(UNIT * 0.15, 0.0, 0.0, 0.0));

        // Add instructions.
        instructions.setText(ResourceBundleManager.get("setup.instructions"));

        // Ready button + event handlers.
        readyButton.setImage(ImageConstants.getReadyIdle());
        readyButton.setFitHeight(UNIT * 0.75);
        readyButton.setFitWidth(UNIT * 2.25);

        readyButton.addEventHandler(MouseEvent.MOUSE_ENTERED_TARGET, e -> {
            readyButton.setImage(ImageConstants.getReadyHover());
        });

        readyButton.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, e -> {
            readyButton.setImage(ImageConstants.getReadyIdle());
        });

        readyButton.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            Platform.runLater(this::finishSetup);
        });

        // Configuración del botón aleatorio estándar
        randomButton.setText(ResourceBundleManager.get("random.button"));
        randomButton.setPrefSize(UNIT * 2.25, UNIT * 0.75);
        randomButton.setFont(Font.font("Century Gothic", UNIT * 0.3));
        randomButton.setStyle(
            "-fx-background-color: rgba(70, 30, 0, 0.8); " +
            "-fx-text-fill: rgb(255, 180, 0); " +
            "-fx-border-color: rgb(255, 180, 0); " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 5; " +
            "-fx-background-radius: 5;"
        );
        
        // Estilos para hover
        randomButton.setOnMouseEntered(e -> {
            randomButton.setStyle(
                "-fx-background-color: rgba(100, 50, 0, 0.8); " +
                "-fx-text-fill: rgb(255, 200, 50); " +
                "-fx-border-color: rgb(255, 200, 50); " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 5; " +
                "-fx-background-radius: 5;"
            );
        });
        
        // Restaurar estilo normal al salir con el ratón
        randomButton.setOnMouseExited(e -> {
            randomButton.setStyle(
                "-fx-background-color: rgba(70, 30, 0, 0.8); " +
                "-fx-text-fill: rgb(255, 180, 0); " +
                "-fx-border-color: rgb(255, 180, 0); " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 5; " +
                "-fx-background-radius: 5;"
            );
        });
        
        // Acción del botón
        randomButton.setOnAction(e -> Platform.runLater(this::randomizeSetup));

        // Text properties - Reduciendo el tamaño de la fuente para que sea más pequeña
        instructions.setFont(Font.font("Century Gothic", UNIT * 0.22)); // Tamaño reducido
        instructions.setTextFill(new Color(1.0, 0.7, 0.0, 1.0));

        // Crear un contenedor para las instrucciones y el botón aleatorio
        HBox initialBox = new HBox(UNIT * 0.5);
        initialBox.setAlignment(Pos.CENTER);
        initialBox.getChildren().addAll(instructions, randomButton);

        // Añadir el contenedor al panel de instrucciones
        instructionPane.getChildren().clear(); // Limpiar primero para evitar duplicados
        instructionPane.getChildren().add(initialBox);
        instructionPane.setAlignment(Pos.CENTER);

        // Worker thread to update the ready button when all of the pieces 
        // have been placed.
        Thread updateButton = new Thread(new UpdateReadyButton());
        updateButton.setDaemon(true);
        updateButton.start();

        setupPanel.add(instructionPane, 0, 2);

        /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
         *                                                                               *
         *                        C R E A T E   U I :   R E A D Y                        *
         *                                                                               *
         * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

        GridPane.setMargin(readyLabel, new Insets(UNIT * 0.8, 0.0, 0.0, UNIT * 1.5));
        readyLabel.setText(ResourceBundleManager.get("waiting.message"));
        readyLabel.setFont(Font.font("Century Gothic", UNIT * 0.6));
        readyLabel.setTextFill(new Color(1.0, 0.7, 0.0, 1.0));
    }

    /**
     * @return the object to communicate the status of the setup pieces between 
     * SetupPieces and the SetupPanel so that UpdateReadyButton can decide 
     * whether or not to display the ready button.
     * 
     * @see edu.asu.stratego.gui.board.setup.SetupPanel
     * @see edu.asu.stratego.gui.board.setup.SetupPieces
     * @see edu.asu.stratego.gui.board.setup.SetupPanel.UpdateReadyButton
     */
    public Object getUpdateReadyStatus() {
        return updateReadyStatus;
    }

    /**
     * @return the SetupPanel (JavaFX GridPane)
     */
    public GridPane getSetupPanel() {
        return setupPanel;
    }

    /**
     * When the player has all of their pieces placed on the board and is ready 
     * to start playing, this method is called. Notifies the ClientGameManager 
     * to send the initial piece positions to the server and receive the 
     * opponent's initial piece positions.
     */
    public void finishSetup() {
        Object setupPieces = ClientGameManager.getSetupPieces();

        synchronized (setupPieces) {
            setupPanel.getChildren().remove(instructionPane);
            setupPanel.getChildren().remove(piecePane);
            setupPanel.add(readyLabel, 0, 1);
            setupPieces.notify();
        }
    }

    /**
     * Coloca aleatoriamente todas las piezas disponibles en el tablero.
     */
    private void randomizeSetup() {
        // Primero, limpiamos el tablero (quitamos todas las piezas ya colocadas)
        clearBoard();

        // Crear lista de todas las piezas disponibles
        List<PieceType> availablePieces = new ArrayList<>();
        
        // Añadir cada tipo de pieza según su disponibilidad
        for (PieceType type : PieceType.values()) {
            int pieceCount = setupPieces.getPieceCount(type);
            for (int i = 0; i < pieceCount; i++) {
                availablePieces.add(type);
            }
        }

        // Crear lista de todas las posiciones válidas (filas 6-9)
        List<int[]> availablePositions = new ArrayList<>();
        for (int row = 6; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                availablePositions.add(new int[]{row, col});
            }
        }

        // Mezclar ambas listas para aleatorizar
        Collections.shuffle(availablePieces);
        Collections.shuffle(availablePositions);

        // Colocar cada pieza en una posición aleatoria
        PieceColor color = Game.getPlayer().getColor();
        for (int i = 0; i < availablePieces.size(); i++) {
            PieceType pieceType = availablePieces.get(i);
            int[] position = availablePositions.get(i);
            int row = position[0];
            int col = position[1];

            // Verificar si hay piezas disponibles de este tipo
            if (setupPieces.getPieceCount(pieceType) > 0) {
                try {
                    // Crear una pieza usando OriginalPiece en lugar de Piece
                    OriginalPiece piece = new OriginalPiece(pieceType, color, false);
                    
                    // Actualizar el modelo lógico del tablero
                    Game.getBoard().getSquare(row, col).setPiece(piece);
                    
                    // Obtener la clave para la imagen de la pieza
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
                    
                    // Actualizar la representación visual de la pieza en el tablero
                    Game.getBoard().getSquare(row, col).getPiecePane().setPiece(
                        edu.asu.stratego.util.HashTables.PIECE_MAP.get(pieceKey)
                    );
                    
                    // Decrementar el contador de piezas disponibles
                    setupPieces.decrementPieceCount(pieceType);
                } catch (Exception e) {
                    System.err.println("Error al colocar pieza: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        // Notificar que todas las piezas han sido colocadas
        synchronized (updateReadyStatus) {
            updateReadyStatus.notify();
        }
    }

    /**
     * Limpia el tablero removiendo todas las piezas colocadas.
     */
    private void clearBoard() {
        // Recorrer el área del jugador (filas 6-9)
        for (int row = 6; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                // Si hay una pieza en esta posición
                Piece piece = Game.getBoard().getSquare(row, col).getPiece();
                if (piece != null) {
                    // Incrementar el contador para este tipo de pieza
                    setupPieces.incrementPieceCount(piece.getPieceType());
                    
                    // Remover la pieza del tablero
                    Game.getBoard().getSquare(row, col).setPiece(null);
                    
                    // Remover la pieza de la vista del tablero
                    Game.getBoard().getSquare(row, col).getPiecePane().setPiece(null);
                }
            }
        }
    }

    /**
     * Worker task that waits for a Setup Piece to be incremented or 
     * decremented. If all of the pieces have been placed, this task removes 
     * the instructions from the panel and adds the ready button to the panel. 
     */
    private class UpdateReadyButton implements Runnable {
        @Override
        public void run() {
            // instructionPane should update only when the state is changed.
            boolean readyState = false;

            synchronized (updateReadyStatus) {
                while (true) {
                    try {
                        // Wait for piece type to increment / decrement.
                        updateReadyStatus.wait();

                        // Remove instructions, add ready button.
                        if (setupPieces.getAllPiecesPlaced() && !readyState) {
                            Platform.runLater(() -> {
                                instructionPane.getChildren().clear();
                                
                                // Crear HBox para ambos botones
                                HBox buttonBox = new HBox(ClientStage.getUnit() * 0.5);
                                buttonBox.setAlignment(Pos.CENTER);
                                buttonBox.getChildren().addAll(randomButton, readyButton);
                                
                                instructionPane.getChildren().add(buttonBox);
                            });
                            readyState = true;
                        }
                        // Remove ready button, add instructions with random button
                        else if (!setupPieces.getAllPiecesPlaced() && readyState) {
                            Platform.runLater(() -> {
                                instructionPane.getChildren().clear();
                                
                                // Recrear el contenedor inicial
                                HBox initialBox = new HBox(ClientStage.getUnit() * 0.5);
                                initialBox.setAlignment(Pos.CENTER);
                                initialBox.getChildren().addAll(instructions, randomButton);
                                
                                instructionPane.getChildren().add(initialBox);
                            });
                            readyState = false;
                        }
                    } catch (InterruptedException e) {
                        System.err.println("UpdateReadyButton thread was interrupted: " + e.getMessage());
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }
    }

    public SetupPieces getSetupPieces() {
        return setupPieces;
    }

    public GridPane getPanel() {
        return setupPanel;
    }

    @Override
    public void onLanguageChanged() {
        instructions.setText(ResourceBundleManager.get("setup.instructions"));
        readyLabel.setText(ResourceBundleManager.get("waiting.message"));
    }
}
