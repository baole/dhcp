package com.hootapps.dhcp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LapBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(LapBroadcastReceiver.class.getSimpleName(), String.format("onReceive %s", (System.currentTimeMillis() - BellApp.mBellManager.mStartedTime) / 1000));
        BellApp.mBellManager.onLap();
    }
}