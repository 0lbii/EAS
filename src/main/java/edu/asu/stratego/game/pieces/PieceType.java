package edu.asu.stratego.game.pieces;

import edu.asu.stratego.game.BattleOutcome;

public enum PieceType {

    SCOUT(2, 8),
    MINER(3, 5),
    SERGEANT(4, 4),
    LIEUTENANT(5, 4),
    CAPTAIN(6, 4),
    MAJOR(7, 3),
    COLONEL(8, 2),
    GENERAL(9, 1),
    MARSHAL(10, 1),
    BOMB(-1, 6),
    SPY(-1, 1),
    FLAG(-1, 1);

    private int value;
    private int count;

    /**
     * Creates a new instance of PieceType.
     * 
     * @param value the piece value
     * @param count number of pieces of this type a player has initially
     */
    PieceType(int value, int count) {
        this.value = value;
        this.count = count;
    }

    /**
     * @return initial count of piece type
     */
    public int getCount() {
        return count;
    }

    /**
     * Returns the current value (rank) of the piece type.
     *
     * @return the value of the piece
     */
    public int getValue() {
        return value;
    }

    /**
     * Sets a new value (rank) for the piece type.
     * This can be used to customize the strength of the piece in alternative game
     * modes.
     *
     * @param newValue the new value to assign to the piece
     */
    public void setValue(int newValue) {
        this.value = newValue;
    }

    /**
     * Returns the result of a battle when one piece type attacks another
     * piece type.
     * 
     * @param defender the defending piece type
     * @return the battle outcome
     */
    public BattleOutcome attack(PieceType defender) {
        // Defender is a flag.
        if (defender == FLAG)
            return BattleOutcome.WIN;

        // Defender is a spy.
        else if (defender == SPY)
            return BattleOutcome.WIN;

        // Defender is Marshal (10) and Attacker is Spy.
        else if (defender == MARSHAL && this == SPY)
            return BattleOutcome.WIN;

        // Defender is a bomb.
        else if (defender == BOMB)
            return (this == MINER) ? BattleOutcome.WIN : BattleOutcome.LOSE;

        // Attacking piece and defending piece are the same piece type.
        else if (this.value == defender.value)
            return BattleOutcome.DRAW;

        // Otherwise, compare piece values.
        return (this.value > defender.value) ? BattleOutcome.WIN : BattleOutcome.LOSE;
    }

}
