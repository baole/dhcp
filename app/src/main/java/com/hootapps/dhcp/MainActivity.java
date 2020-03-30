package com.hootapps.dhcp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


public class MainActivity extends Activity implements View.OnClickListener {
    int increasement = 0;
    final long fiveMin = 60 * 1000 * 5;
    final long oneSec = 1000;

    private static final String TAG = "Main";
    private static final String PREF_SOUND = "sound";
    private ImageView actionView;
    private TextView messageView;
    private TextView timeView;

    static int STATE_STARTED = 0;
    static int STATE_STOPPED = 1;

    int state = STATE_STOPPED;
    private long timer = 0;

    Handler handler = new Handler();
    private PowerManager.WakeLock wl;
    private int selectedSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectedSound = getSelectedSoundIndex(this);

        timeView = (TextView) findViewById(R.id.time);
        messageView = (TextView) findViewById(R.id.message);
        actionView = (ImageView) findViewById(R.id.action);

        actionView.setOnClickListener(this);

        findViewById(R.id.settings).setOnClickListener(this);

        updateUI();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.action) {
            if (state == STATE_STOPPED) {
                state = STATE_STARTED;
                startTimer();
            } else {
                state = STATE_STOPPED;
                stop();
                updateUI();
            }
        } else if (id == R.id.settings) {
            onSettings();
        }
        updateUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stop();
    }

    public void onSettings() {
        SettingFragment newFragment = new SettingFragment();
        newFragment.setCallback(new SettingFragment.Callback() {
            @Override
            public void onDone() {
                selectedSound = getSelectedSoundIndex(getApplicationContext());
            }
        });
        newFragment.show(getFragmentManager(), "dia");
    }


    public static int getSelectedSoundIndex(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getInt(PREF_SOUND, 0);
    }

    public static void setSelectedSoundIndex(Context context, int selected) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().putInt(PREF_SOUND, selected).apply();
    }

    private void updateUI() {
        if (state == STATE_STOPPED) {
            actionView.setImageResource(R.drawable.ic_play_circle_filled_white_black_48dp);
            messageView.setText(R.string.stopped);
        } else {
            messageView.setText(R.string.started);
            actionView.setImageResource(R.drawable.ic_stop_black_48dp);
        }

        updateTicker();

    }


    Runnable lapRunnable = new Runnable() {
        @Override
        public void run() {
            onLap();
            if (increasement <= 11) {
                handler.postDelayed(this, fiveMin);
            } else {
                stop();
                updateUI();
            }
        }
    };

    Runnable tickerRunnable = new Runnable() {
        @Override
        public void run() {
            timer++;
            handler.postDelayed(this, oneSec);
            updateTicker();
        }
    };

    private void updateTicker() {
        timeView.setText(String.format("%s:%02d", timer / 60, timer % 60));
    }

    private void startTimer() {
        timer = 0;
        onLap();
        handler.postDelayed(tickerRunnable, oneSec);
        handler.postDelayed(lapRunnable, fiveMin);// delay 5 min

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "com.hootapps.dhcp:");
        wl.acquire();

//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setSmallIcon(R.drawable.notification_icon)
//                .setContentTitle(textTitle)
//                .setContentText(textContent)
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
    }

    private void stop() {
        handler.removeCallbacks(tickerRunnable);
        handler.removeCallbacks(lapRunnable);

        timer = 0;
        increasement = 0;
        state = STATE_STOPPED;

        try {
            wl.release();
        } catch (Throwable e) {

        }

    }

    public static int sounds[] = new int[] {
            R.raw.beep,
            R.raw.beep2,
            R.raw.beep3,
            R.raw.beep4,
            R.raw.beep5,
            R.raw.button1,
            R.raw.button2,
            R.raw.button3,
            R.raw.button4,
            R.raw.click1,
            R.raw.click2,
            R.raw.click3
    } ;

    private void onLap() {

        int sound = R.raw.beep2;

        if (selectedSound >=0 && selectedSound < sounds.length) {
            sound = sounds[selectedSound];
        }

        Log.e(TAG, String.format("onLap %s, sound=%s, diff=%s", increasement, selectedSound, (R.raw.beep2 == sounds[1])));

        if (increasement == 0 || increasement == 1
                || increasement == 4 || increasement == 5
                || increasement == 8 || increasement == 9) {
            AudioPlayer.play(this, sound, 1);
        } else if (increasement == 2 || increasement == 6 || increasement == 10) {
            AudioPlayer.play(this, sound, 2);
        } else if (increasement == 3 || increasement == 7 || increasement == 11) {
            AudioPlayer.play(this, sound, 3);
        }

        increasement++;
    }
}
