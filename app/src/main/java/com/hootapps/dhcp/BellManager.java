package com.hootapps.dhcp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Log;

interface BellCallback {
    void onStarted();
    void onStopped();
}

public class BellManager {
    static int STATE_STARTED = 0;
    static int STATE_STOPPED = 1;
    private static final String PREF_SOUND = "sound";
    private static final String TAG = BellManager.class.getSimpleName();

    int mState = STATE_STOPPED;
    long mStartedTime = 0;
    int mIncreasement = 0;

    BellCallback mCallback = null;
    Context mContext;

    BellManager(Context context) {
        this.mContext = context;
    }

    public void startBell() {
        if (mState == STATE_STOPPED) {
            onStart();
        } else {
            onStop();
        }
    }

    void onStop() {
        mState = STATE_STOPPED;
        mStartedTime = 0;
        mIncreasement = 0;
        if (mCallback != null) mCallback.onStopped();
    }

    private void onStart() {
        mState = STATE_STARTED;
        mStartedTime = System.currentTimeMillis();
        mIncreasement = 0;
        if (mCallback != null) mCallback.onStarted();
        onLap();
    }

    public boolean isStarted() {
        return mState == STATE_STARTED;
    }


    public static int sounds[] = new int[]{
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
    };

    public void onLap() {
        if (!isStarted()) return;

        int sound = R.raw.beep2;
        int selectedSound = getSelectedSoundIndex(mContext);

        if (selectedSound >= 0 && selectedSound < sounds.length) {
            sound = sounds[selectedSound];
        }

        Log.e(TAG, String.format("onLap %s, %s", mIncreasement, (System.currentTimeMillis() - BellApp.mBellManager.mStartedTime) / 1000));

        if (mIncreasement == 0 || mIncreasement == 1
                || mIncreasement == 4 || mIncreasement == 5
                || mIncreasement == 8 || mIncreasement == 9) {
            AudioPlayer.play(mContext, sound, 1);
        } else if (mIncreasement == 2 || mIncreasement == 6 || mIncreasement == 10) {
            AudioPlayer.play(mContext, sound, 2);
        } else if (mIncreasement == 3 || mIncreasement == 7 || mIncreasement == 11) {
            AudioPlayer.play(mContext, sound, 3);
        }

        mIncreasement++;

        if (mIncreasement > 11) {
            onStop();
        } else {
            onScheduleLap();
        }
    }

    private void onScheduleLap() {
        Intent alarmIntent = new Intent(mContext, LapBroadcastReceiver.class);
        PendingIntent pendingAlarmIntent = PendingIntent.getBroadcast(mContext, 0, alarmIntent, 0);
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        if (Build.VERSION.SDK_INT >=23) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + DateUtils.MINUTE_IN_MILLIS * 5, pendingAlarmIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + DateUtils.MINUTE_IN_MILLIS * 5, pendingAlarmIntent);
        }

        Log.e(TAG, String.format("onScheduleLap %s, %s", mIncreasement, (System.currentTimeMillis() - BellApp.mBellManager.mStartedTime) / 1000));
    }

    public int getSelectedSoundIndex(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getInt(PREF_SOUND, 0);
    }


    public void setSelectedSoundIndex(Context context, int selected) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().putInt(PREF_SOUND, selected).apply();
    }

}
