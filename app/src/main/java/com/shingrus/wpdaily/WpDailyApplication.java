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
    private static final int DEFAULT_UPDATE_FREQUENCY_H = 24;

    SetWallPaper setWallPaper;

    public WpDailyApplication() {
        super();

    }

    private class UpdateTask extends AsyncTask<Void, Void, Void> {



        @Override
        protected Void doInBackground(Void... params) {


            setWallPaper.updateWallPaperImage();
            return null;
        }

        @Override
        protected void onPostExecute(Void avoid) {
            Toast t = Toast.makeText(getApplicationContext(), "WallPaper updated", Toast.LENGTH_LONG);
            t.show();

        }

    }

    private void startJob(Integer freq) {
        JobInfo job = new JobInfo.Builder(JOB_ID, new ComponentName(this, PeriodicalJobService.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setMinimumLatency(freq*60*60*1000)
                //.setMinimumLatency(6 * 1000)
                .setRequiresCharging(false)
                .build();
        JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        scheduler.schedule(job);
        Log.d(_log_tag, "Put job with freq: " + freq);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        Integer freq = pref.getInt("sync_frequency", DEFAULT_UPDATE_FREQUENCY_H);

        setWallPaper = SetWallPaper.getSetWallPaper(this);

        //update on start

        new UpdateTask().execute();
        //start background job
        startJob(freq);

    }
}