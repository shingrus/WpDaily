package com.shingrus.wpdaily;

import android.app.Application;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by shingrus on 22/11/15.
 * Main application
 */
public class WpDailyApplication extends Application {
    //Intent myIntentService;
    private static final String _log_tag = "WPD/WP_APP";
    private static final int DEFAULT_UPDATE_FREQUENCY_H = 6 * 60;

    SetWallPaper setWallPaper;
    ImageStorage storage;

    public WpDailyApplication() {
        super();

    }


    @Override
    public void onCreate() {
        super.onCreate();

        setWallPaper = SetWallPaper.getSetWallPaper(this);
        storage = ImageStorage.getInstance(this);

        //
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isAutomaticUpdateEnable = pref.getBoolean(this.getString(R.string.AutoUpdateEnabledKey),true);
        if (isAutomaticUpdateEnable)
            WPUpdateService.restartJobFromPreferences(this, pref);

        //update on start
        Log.d(_log_tag, "Start application:"+BuildConfig.VERSION_CODE);


    }
}