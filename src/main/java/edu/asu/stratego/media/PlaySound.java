package edu.asu.stratego.media;

import edu.asu.stratego.util.HashTables;
import edu.asu.stratego.util.HashTables.SoundType;
import javafx.scene.media.AudioClip;

public class PlaySound {
    private static AudioClip currentMusic = null;

    public static void playMusic(SoundType soundType, int volume) {
        stopMusic();
        currentMusic = HashTables.SOUND_MAP.get(soundType);
        currentMusic.setVolume(volume);
        currentMusic.setCycleCount(AudioClip.INDEFINITE);
        currentMusic.play();
    }

    public static void playEffect(SoundType soundType, int Volume) {
        AudioClip effect = HashTables.SOUND_MAP.get(soundType);
        effect.setVolume(Volume);
        effect.play();
    }

    public static void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic = null;
        }
    }
}