package edu.asu.stratego.game;

import java.awt.Point;
import java.io.Serializable;

import edu.asu.stratego.game.pieces.PieceColor;
import edu.asu.stratego.game.pieces.Piece;

public class Move implements Serializable {

	private static final long serialVersionUID = -8315478849105334331L;

	private Point start = new Point(-1, -1);
	private Point end = new Point(-1, -1);

	private PieceColor moveColor = null;

	private Piece startPiece;
	private Piece endPiece;

	private boolean isAttack;
	private boolean attackWin;
	private boolean defendWin;

	/**
	 * Checks whether the current move is an attack move
	 *
	 * @return true if it's an attack move, false otherwise
	 */
	public boolean isAttackMove() {
		return isAttack;
	}

	/**
	 * Sets whether the move is an attack move
	 *
	 * @param bool true if it's an attack move, false otherwise
	 */
	public void setAttackMove(boolean bool) {
		isAttack = bool;
	}

	/**
	 * Checks if a piece has been selected
	 *
	 * @return true if a piece is selected, false otherwise
	 */
	public boolean isPieceSelected() {
		return (start.x != -1 && start.y != -1);
	}

	/**
	 * Gets the row index of the start position
	 *
	 * @return the starting row index
	 */
	public int getRowStart() {
		return start.x;
	}

	/**
	 * Gets the column index of the start position
	 *
	 * @return the starting column index
	 */
	public int getColStart() {
		return start.y;
	}

	/**
	 * Gets the starting point of the move
	 *
	 * @return the start point as a Point object
	 */
	public Point getStart() {
		return start;
	}

	/**
	 * Gets the ending point of the move
	 *
	 * @return the end point as a Point object
	 */
	public Point getEnd() {
		return end;
	}

	/**
	 * Sets the start position of the move
	 * 
	 * @param rowStart the row index of the start position
	 * @param colStart the column index of the start position
	 */
	public void setStart(int rowStart, int colStart) {
		start = new Point(rowStart, colStart);
	}

	/**
	 * Sets the end position of the move
	 * 
	 * @param rowEnd the row index of the end position
	 * @param colEnd the column index of the end position
	 */
	public void setEnd(int rowEnd, int colEnd) {
		end = new Point(rowEnd, colEnd);
	}

	/**
	 * Gets the start position of the move
	 * 
	 * @return the start position of the move
	 */
	public void setStart(Point Start) {
		start = Start;
	}

	/**
	 * Sets the end position of the move
	 * 
	 * @param End the end position of the move
	 */
	public void setEnd(Point End) {
		end = End;
	}

	/**
	 * Gets the color of the piece making the move
	 * 
	 * @return the color of the piece making the move
	 */
	public PieceColor getMoveColor() {
		return moveColor;
	}

	/**
	 * Sets the color of the piece making the move
	 * 
	 * @param moveColor the color of the piece making the move
	 */
	public void setMoveColor(PieceColor moveColor) {
		this.moveColor = moveColor;
	}

	/**
	 * Gets the piece at the start position of the move
	 * 
	 * @return the piece at the start position
	 */
	public Piece getStartPiece() {
		return startPiece;
	}

	/**
	 * Sets the start piece of the move
	 * 
	 * @param startPiece the piece at the start position
	 */
	public void setStartPiece(Piece startPiece) {
		this.startPiece = startPiece;
	}

	/**
	 * Gets the piece at the end position of the move
	 * 
	 * @return the piece at the end position
	 */
	public Piece getEndPiece() {
		return endPiece;
	}

	/**
	 * Sets the end piece of the move
	 * 
	 * @param endPiece the piece at the end position
	 */
	public void setEndPiece(Piece endPiece) {
		this.endPiece = endPiece;
	}

	/**
	 * Checks if the move is an attack move
	 * 
	 * @return true if the move is an attack, false otherwise
	 */
	public boolean isAttackWin() {
		return attackWin;
	}

	/**
	 * Sets the attackWin status
	 * 
	 * @param attackWin true if the attacking piece wins, false otherwise
	 */
	public void setAttackWin(boolean attackWin) {
		this.attackWin = attackWin;
	}

	/**
	 * Checks if the defending piece wins
	 * 
	 * @return true if the defending piece wins, false otherwise
	 */
	public boolean isDefendWin() {
		return defendWin;
	}

	/**
	 * Sets the defendWin status
	 * 
	 * @param defendWin true if the defending piece wins, false otherwise
	 */
	public void setDefendWin(boolean defendWin) {
		this.defendWin = defendWin;
	}

}
