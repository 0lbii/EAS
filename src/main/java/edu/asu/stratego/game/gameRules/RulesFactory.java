package edu.asu.stratego.game.gameRules;

import edu.asu.stratego.game.board.ServerBoard;
import edu.asu.stratego.game.ServerGameManager;

public interface RulesFactory {

    OriginalRules createOriginalRules(ServerBoard board, ServerGameManager manager);

}
