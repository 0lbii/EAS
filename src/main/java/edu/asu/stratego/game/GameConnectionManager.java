package edu.asu.stratego.game;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class GameConnectionManager {

    private ObjectOutputStream toServer;
    private ObjectInputStream fromServer;

    public GameConnectionManager() {
        // Constructor vacío
    }

    public void initializeConnection() throws IOException {
        if (ClientSocket.getInstance() != null && !ClientSocket.getInstance().isClosed()) {
            ClientSocket.getInstance().close();
            ClientSocket.setInstance(null);
        }
        ClientSocket.connect(Game.getPlayer().getServerIP(), 4212);

        // I/O Streams
        toServer = new ObjectOutputStream(ClientSocket.getInstance().getOutputStream());
        fromServer = new ObjectInputStream(ClientSocket.getInstance().getInputStream());
    }

    public void closeConnection() {
        try {
            if (fromServer != null) {
                fromServer.close();
                fromServer = null;
            }
            if (toServer != null) {
                toServer.close();
                toServer = null;
            }
            ClientSocket.getInstance().close();
        } catch (IOException e) {
        }
    }

    public void sendObject(Object object) throws IOException {
        if (toServer != null) {
            toServer.writeObject(object);
            toServer.flush();
        }
    }

    public Object receiveObject() throws IOException, ClassNotFoundException {
        return fromServer.readObject();
    }

    public void sendPlayerInfo() throws IOException {
        toServer.writeObject(Game.getPlayer());
    }

    public void sendMove() throws IOException {
        if (toServer != null) {
            toServer.writeObject(Game.getMove());
        }
    }

    public void sendAbandonSignal() throws IOException {
        if (toServer != null) {
            toServer.writeObject("ABANDON");
            toServer.flush();
        }
    }

    public void sendSetupBoard(SetupBoard setupBoard) throws IOException {
        toServer.writeObject(setupBoard);
    }

    public ObjectInputStream getInputStream() {
        return fromServer;
    }

    public ObjectOutputStream getOutputStream() {
        return toServer;
    }
}