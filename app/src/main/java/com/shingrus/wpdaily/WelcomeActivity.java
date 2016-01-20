package com.shingrus.wpdaily;

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
        Button welcomeButton = (Button) findViewById(R.id.welcomeContinueButton);
        welcomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(WelcomeActivity.this);
                SharedPreferences.Editor editor = pref.edit();
                SwitchCompat sw = (SwitchCompat) findViewById(R.id.welcomeEnableAutoUpdate);
                editor.putBoolean(getString(R.string.WelcomeScreenShowedKey),true);
                editor.putBoolean(getString(R.string.AutomaticUpdateEnabledKey), sw.isChecked());
                editor.apply();

                Intent startMainActivity = new Intent(WelcomeActivity.this,WPDMainActivity.class);
                startMainActivity.setAction(WPDMainActivity.SKIP_WELCOME_CHECK_ACTION);
                startActivity(startMainActivity);
                WelcomeActivity.this.finish();
            }
        });
    }
}
