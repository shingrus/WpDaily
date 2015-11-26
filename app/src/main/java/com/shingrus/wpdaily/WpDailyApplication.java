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
    private static final int JOB_ID = 0x1000;
    private static final String _log_tag = "WP_APP";
    private static final int DEFAULT_UPDATE_FREQUENCY_H = 24*60;

    SetWallPaper setWallPaper;

    public WpDailyApplication() {
        super();

    }

    private String freqKey = "";

    private SharedPreferences.OnSharedPreferenceChangeListener listener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {

                // listener implementation
                public void onSharedPreferenceChanged(SharedPreferences pref, String key) {

                    if (key.equals(freqKey)) {
                        //our key changed
                        String freq = pref.getString(freqKey, "360");

                        //restart background job
                        startJob(Integer.parseInt(freq));
                    }

                }
            };


    /**
     *
     * @param freq Integer - minutes
     */
    private void startJob(Integer freq) {
        JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        scheduler.cancel(JOB_ID);
        if (freq > 0) {
            JobInfo job = new JobInfo.Builder(JOB_ID, new ComponentName(this, PeriodicalJobService.class))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setMinimumLatency(freq * 60 * 1000)
                    .setRequiresCharging(false)
                    .build();
            scheduler.schedule(job);
            Log.d(_log_tag, "Put job with freq: " + freq);
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();

        freqKey = getString(R.string.update_freq_list);
        setWallPaper = SetWallPaper.getSetWallPaper(this);

        //update on start

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String freq = pref.getString(freqKey, "360");

        //start background job
        startJob(Integer.parseInt(freq));
        pref.registerOnSharedPreferenceChangeListener(listener);


    }
}