package com.shingrus.wpdaily;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import static android.support.v4.content.WakefulBroadcastReceiver.completeWakefulIntent;

/**
 * Created by shingrus on 12/01/16.
 * Update service should completely substitute jobservice
 */
public class WPUpdateService extends IntentService {

    private static final String _service_name = "WPUpdateService";
    private static final String _log_tag = "WPD/UpdateService";
    public static final String UPDATE_ACTIVITY_ACTION = "com.shingrus.wpadaily.action.update_activity";
    public static final String RESULT_EXTRA = "UpdateResult";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    private Context ctx = null;


    public WPUpdateService() {
        super(_service_name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ctx = getApplicationContext();
    }

    /**
     * @param periodic - time period in milliseconds
     */
    private static void scheduleAlarm(Context ctx, long periodic) {

        Log.d(_log_tag, "Schedule alarm with period: " + periodic);
        Intent intent = new Intent(ctx, WPAlarmReciever.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        PendingIntent pIntent = PendingIntent.getBroadcast(ctx, 0,
                intent, 0);


        AlarmManager alarm = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000,
                periodic, pIntent);
    }

    private static void restartJob(Context ctx, long seconds) {
//        if (BuildConfig.DEBUG)
//            seconds = 10;

        scheduleAlarm(ctx.getApplicationContext(), seconds * 1000);
    }
    public static void restartJobFromPreferences(Context ctx, SharedPreferences pref) {

        boolean autoUpdate = pref.getBoolean(ctx.getString(R.string.AutoUpdateEnabledKey), false);
        if (autoUpdate) {
            String freqKey = ctx.getString(R.string.update_freq_list);
            String freq = pref.getString(freqKey, "360");

            long seconds = Integer.parseInt(freq) * 60;

            restartJob(ctx, seconds);
        }
        else
            Log.d(_log_tag, "Autoupdate disabled. Job start refused");
    }


    private boolean checkNetworkState() {
        ConnectivityManager conMan = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isNetworkReady = false;
        NetworkInfo network = conMan.getActiveNetworkInfo();
        if (network != null && network.isConnected()) {
            isNetworkReady = true;

        }
        return isNetworkReady;
    }


    private void subscribeOnNetworkChanges(boolean subscribe) {

        Log.d(_log_tag, subscribe ? "Subscribe" : "Unsubscribe");
        ComponentName receiver = new ComponentName(ctx, NetworkBroadcastReceiver.class);

        PackageManager pm = ctx.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                subscribe ?
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    private SetWallPaper.UpdateResult doUpdate() {

        SetWallPaper.UpdateResult retVal = SetWallPaper.getSetWallPaper().updateWallPaperImage();
        Log.d(_log_tag, "Doing update from service");

        return retVal;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        Log.d(_log_tag, "Start service job");

        //Intent to notify activity about update result
        Intent intentForActivity = new Intent();
        intentForActivity.setAction(UPDATE_ACTIVITY_ACTION);
        if (checkNetworkState()) {
            SetWallPaper.UpdateResult updateResult = doUpdate();
            Log.d(_log_tag, "Update result: " + updateResult);

            if (updateResult != SetWallPaper.UpdateResult.NETWORK_FAIL)  {
                    //don't unsubscribe from network events till we have network error
                subscribeOnNetworkChanges(false);
            }
            intentForActivity.putExtra(RESULT_EXTRA, updateResult);

        } else {
            intentForActivity.putExtra(RESULT_EXTRA, SetWallPaper.UpdateResult.NETWORK_FAIL);
            subscribeOnNetworkChanges(true);
        }
        sendBroadcast(intentForActivity);
        completeWakefulIntent(intent);

    }
}
