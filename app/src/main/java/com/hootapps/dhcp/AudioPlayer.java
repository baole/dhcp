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
    private MediaPlayer mMediaPlayer;
    private int mLoopCount = 1;

    public void stop() {
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.release();
                mMediaPlayer = null;
            } catch (Throwable e) {
            }
        }

        mLoopCount = 1;
    }

    void playSound(Context c, int rid, final int loop) {

        if (c == null) {
            return;
        }

        stop();

        try {
            
            Log.e(TAG, String.format("playSound: %s", loop));
            
            mMediaPlayer = new MediaPlayer();// MediaPlayer.create(c, rid);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
            Uri uri = Uri.parse(String.format("android.resource://%s/", c.getPackageName()) + rid);
            mMediaPlayer.setDataSource(c, uri);
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
//                    Log.e(TAG, String.format("  loop: %s %s", loopCount, loop));
                    if (mLoopCount >= loop) {
                        stop();
                    } else {
                        mediaPlayer.seekTo(0);
                        mediaPlayer.start();
                        mLoopCount++;
                    }

                }
            });

            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (Throwable e) {
            //ignored
            e.printStackTrace();
        }
    }


    static void play(Context context, int rid, int loop) {
        new AudioPlayer().playSound(context, rid, loop);
    }

}