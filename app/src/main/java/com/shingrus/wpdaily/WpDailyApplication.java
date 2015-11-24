package com.shingrus.wpdaily;

import android.app.Application;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by shingrus on 22/11/15.
 */
public class WpDailyApplication extends Application {
    //Intent myIntentService;
    private static final int JOB_ID = 0x1000;
    private static final int DEFAULT_UPDATE_FREQUENCY_H = 24;

    SetWallPaper setWallPaper;

    public WpDailyApplication() {
        super();

    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Intent myWPService = new Intent(this, SetWpService.class);
        //this.startService(myWPService);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        Integer freq = pref.getInt("sync_frequency", DEFAULT_UPDATE_FREQUENCY_H);

        setWallPaper = SetWallPaper.getSetWallPaper(this);

        JobInfo job = new JobInfo.Builder(JOB_ID, new ComponentName(this, PeriodicalJobService.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setMinimumLatency(24 * 60*60*1000)
                .setRequiresCharging(true)
                .build();
        JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        scheduler.schedule(job);
    }
}