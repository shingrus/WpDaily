package com.shingrus.wpdaily;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.Button;

/**
 * Created by shingrus on 19/01/16.
 * Is Showed at first start.
 * Asks about automatic change of the wallpaper
 */
public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        Button welcomeYesButton = (Button) findViewById(R.id.welcomeYesButton);
        Button welcomeNoButton = (Button) findViewById(R.id.welcomeNoButton);
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(WelcomeActivity.this);
                SharedPreferences.Editor editor = pref.edit();

                editor.putBoolean(getString(R.string.WelcomeScreenShowedKey),true);
                editor.putBoolean(getString(R.string.AutoUpdateEnabledKey),
                        (v.getId() == R.id.welcomeYesButton));
                editor.apply();
                Intent startMainActivity = new Intent(WelcomeActivity.this,WPDMainActivity.class);
                startMainActivity.setAction(WPDMainActivity.SKIP_WELCOME_CHECK_ACTION);
                startActivity(startMainActivity);
                WelcomeActivity.this.finish();
            }
        };
        welcomeNoButton.setOnClickListener(clickListener);
        welcomeYesButton.setOnClickListener(clickListener);
    }
}
