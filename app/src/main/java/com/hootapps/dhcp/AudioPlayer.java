package com.hootapps.dhcp;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

/**
 * Created by baoleduc on 14/06/16.
 */
public class AudioPlayer {

    private static final String TAG = "Audio";
    private MediaPlayer mediaPlayer;

    public void stop() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
                mediaPlayer = null;
            } catch (Throwable e) {
            }
        }

        loopCount = 1;
    }

    int loopCount = 1;
    public void playSound(Context c, int rid, final int loop) {

        if (c == null) {
            return;
        }

        stop();

        try {
            
            Log.e(TAG, String.format("playSound: %s", loop));
            
            mediaPlayer = new MediaPlayer();// MediaPlayer.create(c, rid);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
            Uri uri = Uri.parse(String.format("android.resource://%s/", c.getPackageName()) + rid);
            mediaPlayer.setDataSource(c, uri);
            mediaPlayer.setLooping(loop > 1);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    if (loopCount >= loop) {
                        stop();
                    } else {
                        loopCount++;
                    }

                }
            });

            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Throwable e) {
            //ignored
            e.printStackTrace();
        }
    }


    public static void play(Context context, int rid, int loop) {
        new AudioPlayer().playSound(context, rid, loop);
    }

}