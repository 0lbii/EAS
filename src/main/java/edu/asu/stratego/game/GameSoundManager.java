package edu.asu.stratego.game;

import edu.asu.stratego.media.PlaySound;
import edu.asu.stratego.util.HashTables.SoundType;

public class GameSoundManager {
    public static void playMoveSound() {
        PlaySound.playEffect(SoundType.MOVE, 100);
    }

    public static void playAttackSound() {
        PlaySound.playEffect(SoundType.ATTACK, 100);
    }

    public static void playWinSound() {
        PlaySound.playEffect(SoundType.WIN, 100);
    }
}