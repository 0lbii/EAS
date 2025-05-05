package edu.asu.stratego.media;

import javafx.scene.media.AudioClip;

public class SoundConstants {

    private static AudioClip load(String path) {
        return new AudioClip(SoundConstants.class.getResource(path).toString());
    } 

    public final static AudioClip MOVE_SOUND = load("/sound/move.mp3");
    public final static AudioClip ATTACK_SOUND = load("/sound/attack.mp3");
    public final static AudioClip WIN_SOUND = load("/sound/win.wav");
    public final static AudioClip SELECT_SOUND = load("/sound/select.mp3");
    public final static AudioClip CORNFIELD = load("/sound/cornfield.mp3");
}