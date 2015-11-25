package com.shingrus.wpdaily;

import android.app.WallpaperManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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
public class PeriodicalJobService extends JobService {

    private static String _log_tag = "WPJobService";
    JobParameters params;
    JobTask jobTask;
    private class JobTask extends AsyncTask<Void, Void, Void> {


        JobService jobService;
        public JobTask(JobService jobService) {

            this.jobService = jobService;
        }

        @Override
        protected Void doInBackground(Void... params) {

            Log.d(_log_tag, "Start bg job");
            SetWallPaper.getSetWallPaper().updateWallPaperImage();
            return null;
        }

        @Override
        protected void onPostExecute(Void avoid) {

            jobFinished(params, true);
        }

    }

    //JobTask jobTask = new JobTask(this);

    @Override
    public boolean onStartJob(JobParameters params) {
        this.params= params;
        jobTask = new JobTask(this);
        jobTask.execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

}
