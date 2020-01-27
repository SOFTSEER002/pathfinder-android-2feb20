package com.fox.app.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.avery.sampleapp.R;

import static com.fox.app.Activities.AccessPathfinderActivity.PREF_KEY_FIRST_START;

public class SplashActivity extends Activity {
    boolean firstStart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        firstStart = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(PREF_KEY_FIRST_START, true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (firstStart) {
                    startActivity(new Intent(SplashActivity.this, AccessPathfinderActivity.class));
                    finish();
                } else {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                }
            }
        }, 2000);
    }
}
