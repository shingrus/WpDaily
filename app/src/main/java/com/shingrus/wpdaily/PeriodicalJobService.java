package com.shingrus.wpdaily;

import android.app.WallpaperManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by shingrus on 23/11/15.
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

            SetWallPaper setWallPaper = SetWallPaper.getSetWallPaper();
            URL imageUrl = setWallPaper.GetLastWallpaperLink();
            if (imageUrl != null) {
                //i have new url,

                Bitmap image = SetWallPaper.getSetWallPaper().getImage(imageUrl);



                if (image != null) {
                    WallpaperManager wp = WallpaperManager.getInstance(jobService);
                    try {
                        wp.setBitmap(image);
                    } catch (IOException e) {
                        Log.e(_log_tag, "set image error" + e);
                    }
                }
            }

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
