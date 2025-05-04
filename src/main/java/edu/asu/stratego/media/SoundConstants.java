package edu.asu.stratego.media;

import javafx.scene.media.AudioClip;

public class SoundConstants {
    public final static AudioClip MOVE_SOUND = new AudioClip(SoundConstants.class.getResource("/sound/move.mp3").toString());
    public final static AudioClip ATTACK_SOUND = new AudioClip(SoundConstants.class.getResource("/sound/attack.mp3").toString());
    public final static AudioClip WIN_SOUND = new AudioClip(SoundConstants.class.getResource("/sound/win.wav").toString());
    public final static AudioClip SELECT_SOUND = new AudioClip(SoundConstants.class.getResource("/sound/select.mp3").toString());
    public final static AudioClip CORNFIELD = new AudioClip(SoundConstants.class.getResource("/sound/cornfield.mp3").toString());
}