package com.hootapps.dhcp;

import android.app.Application;

public class BellApp extends Application {
    public static BellManager mBellManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mBellManager = new BellManager(this);
    }
}
