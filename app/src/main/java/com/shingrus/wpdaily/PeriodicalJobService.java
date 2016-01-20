package com.shingrus.wpdaily;

import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by shingrus on 23/11/15.
 * JobService
 */
public class PeriodicalJobService /*extends JobService */{

//    private static String _log_tag = "WPD/WPJobService";
//    JobParameters params;
//    JobTask jobTask;
//
//    private static Integer frequency=0;
//
//    private static final int JOB_ID = 0x1000;
//
//    private class JobTask extends AsyncTask<Void, Void, Void> {
//
//
//        JobService jobService;
//        public JobTask(JobService jobService) {
//
//            this.jobService = jobService;
//        }
//
//        @Override
//        protected Void doInBackground(Void... params) {
//
//            Log.d(_log_tag, "Start bg job");
//            SetWallPaper.getSetWallPaper().updateWallPaperImage();
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void avoid) {
//
//            //good place to restart job
//            jobFinished(params, true);
//        }
//
//    }
//
//    @Override
//    public boolean onStartJob(JobParameters params) {
//        this.params= params;
//        jobTask = new JobTask(this);
//        jobTask.execute();
//        return true;
//    }
//
//    @Override
//    public boolean onStopJob(JobParameters params) {
//        Log.d(_log_tag, "Job finished");
//        return true;
//    }
//
//
//
//    private static void startJobfromPreferences(Context ctx, SharedPreferences pref) {
//        String freqKey = ctx.getString(R.string.update_freq_list);
//        String freq = pref.getString(freqKey, "360");
//        startJob(Integer.parseInt(freq), ctx);
//    }
//    /**
//     * @param freq Integer - minutes
//     */
//    private static  void startJob(Integer freq, Context ctx) {
//        JobScheduler scheduler = (JobScheduler) ctx.getSystemService(Context.JOB_SCHEDULER_SERVICE);
//        scheduler.cancel(JOB_ID);
//        if (freq > 0) {
//            JobInfo job = new JobInfo.Builder(JOB_ID, new ComponentName(ctx, PeriodicalJobService.class))
//                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
//                    .setMinimumLatency(freq * 60 * 1000)
//                    .setRequiresCharging(false)
//                    .build();
//            scheduler.schedule(job);
//            Log.d(_log_tag, "Put job with freq: " + freq);
//        }
//
//    }

}
