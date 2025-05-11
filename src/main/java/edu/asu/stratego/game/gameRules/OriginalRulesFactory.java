package edu.asu.stratego.game.gameRules;

import edu.asu.stratego.game.board.ServerBoard;
import edu.asu.stratego.game.ServerGameManager;

public class OriginalRulesFactory implements RulesFactory {

    @Override
    public OriginalRules createOriginalRules(ServerBoard board, ServerGameManager manager) {
        return new OriginalRules(board, manager);
    }

}
