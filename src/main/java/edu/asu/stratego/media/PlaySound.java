package edu.asu.stratego.media;

import edu.asu.stratego.util.HashTables;
import edu.asu.stratego.util.HashTables.SoundType;
import javafx.scene.media.AudioClip;

public class PlaySound {
    public static void playMusic(SoundType soundType, int Volume) {
        AudioClip music = HashTables.SOUND_MAP.get(soundType);
        music.setVolume(Volume);
		music.setCycleCount(AudioClip.INDEFINITE);
        music.play();
        System.out.println("Played music (" + soundType + ", " + Volume * 100 + "% volume)");
    }

    public static void playEffect(SoundType soundType, int Volume) {
        AudioClip effect = HashTables.SOUND_MAP.get(soundType);
        effect.setVolume(Volume);
        effect.play();
    }
}