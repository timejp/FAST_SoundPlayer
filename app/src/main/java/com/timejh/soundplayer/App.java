package com.timejh.soundplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

/**
 * Created by tokijh on 2017. 2. 28..
 */

public class App {

    // 서비스 플레이 액션 정의
    public static final String ACTION_PLAY = "com.timejh.soundplayer.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.timejh.soundplayer.ACTION_PAUSE";
    public static final String ACTION_RESTART = "com.timejh.soundplayer.ACTION_RESTART";
    public static final String ACTION_STOP = "com.timejh.soundplayer.ACTION_STOP";

    public static MediaPlayer soundPlayer;

    public static void initSound(Context context) {
        Uri soundUri = null; // TODO uri 설정
        soundPlayer = MediaPlayer.create(context, soundUri);
    }

    public static void playSound() {
        soundPlayer.start();
    }

    public static void pauseSound() {
        soundPlayer.pause();
    }

    public static void restartSound() {
        soundPlayer.start();
    }

    public static void stop() {
        if (soundPlayer != null) {
            soundPlayer.release();
            soundPlayer = null;
        }
    }
}
