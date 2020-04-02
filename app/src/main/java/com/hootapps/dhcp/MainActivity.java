package com.hootapps.dhcp;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.text.format.DateUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;


public class MainActivity extends Activity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ImageView mActionView;
    private TextView mMessageView;
    private TextView mTickerView;

    private Handler mHandler = new Handler();
    private PowerManager.WakeLock mWakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTickerView = (TextView) findViewById(R.id.time);
        mMessageView = (TextView) findViewById(R.id.message);
        mActionView = (ImageView) findViewById(R.id.action);

        mActionView.setOnClickListener(this);

        findViewById(R.id.settings).setOnClickListener(this);
        BellApp.mBellManager.mCallback = new BellCallback() {
            @Override
            public void onStarted() {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
                mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        "MyApp::MyWakelockTag");
                mWakeLock.acquire();            }

            @Override
            public void onStopped() {
                if (mWakeLock != null) mWakeLock.release();
            }
        };

        updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.postDelayed(mTickerRunnable, DateUtils.SECOND_IN_MILLIS);
    }

    @Override
    protected void onPause() {
        mHandler.removeCallbacks(mTickerRunnable);
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.action) {
            BellApp.mBellManager.startBell();

        } else if (id == R.id.settings) {
            onSettings();
        }
        updateUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BellApp.mBellManager.onStop();
    }

    public void onSettings() {
        SettingFragment newFragment = new SettingFragment();
        newFragment.show(getFragmentManager(), "dia");
    }


    private void updateUI() {
        if (BellApp.mBellManager.isStarted()) {
            mMessageView.setText(R.string.started);
            mActionView.setImageResource(R.drawable.ic_stop_black_48dp);
            long startedTime = (System.currentTimeMillis() - BellApp.mBellManager.mStartedTime)/ 1000;
            mTickerView.setText(String.format("%s:%02d", startedTime / 60, startedTime % 60));

        } else {
            mActionView.setImageResource(R.drawable.ic_play_circle_filled_white_black_48dp);
            mMessageView.setText(R.string.stopped);
            mTickerView.setText("");
        }
    }


    Runnable mTickerRunnable = new Runnable() {
        @Override
        public void run() {
            mHandler.postDelayed(this, DateUtils.SECOND_IN_MILLIS);
            updateUI();
        }
    };
}
