package edu.asu.stratego.game;

import java.io.Serializable;

import edu.asu.stratego.game.pieces.PieceColor;

/**
 * Contains information about a player.
 */
public class Player implements Serializable {

    private static final long serialVersionUID = 649459794036226272L;
    private String nickname;
    private PieceColor color;
    private String email;
    private Integer points = 0;

    /**
     * Creates a new instance of Player
     */
    public Player() {
    }

    /**
     * @param nickname the player's nickname
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * @return a String containing the player's name
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * @param color the player's color
     */
    public void setColor(PieceColor color) {
        this.color = color;
    }

    /**
     * @return the player's color
     */
    public PieceColor getColor() {
        return color;
    }

    /**
     * @param email the player's email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the player's email
     */
    public String getEmail() {
        return email;
    }
    
    /**
     * @return the player's points
     */
    public Integer getPoints() {
        return points;
    }

    /**
     * @param points the player's points
     */
    public void setPoints(Integer points) {
        this.points = points;
    }

}
