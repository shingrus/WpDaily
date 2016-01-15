package com.shingrus.wpdaily;

import android.app.Application;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.net.URL;

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
        boolean isAutomaticUpdateEnable = pref.getBoolean(this.getString(R.string.AutomaticUpdateEnabledKey),true);
        if (isAutomaticUpdateEnable)
            WPUpdateService.restartJobfromPreferences(this, pref);

        //update on start
        Log.i(_log_tag, "Start application");


    }
}