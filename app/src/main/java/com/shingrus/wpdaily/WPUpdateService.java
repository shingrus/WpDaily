package com.shingrus.wpdaily;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import static android.support.v4.content.WakefulBroadcastReceiver.completeWakefulIntent;

/**
 * Created by shingrus on 12/01/16.
 * Update service should completely substitute jobservice
 */
public class WPUpdateService extends IntentService {

    private static final String _service_name = "WPUpdateService";
    private static final String _log_tag = "WPD/UpdateService";
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
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
     *
     * @param periodic - time period in milliseconds
     */
    private static void scheduleAlarm(Context ctx, long periodic) {

        Intent intent = new Intent(ctx, WPAlarmReciever.class);
        // Create a PendingIntent to be triggered when the alarm goes off
         PendingIntent pIntent = PendingIntent.getBroadcast(ctx, 0,
                intent, 0);


        AlarmManager alarm = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000,
                periodic, pIntent);
    }

    public static void restartJobfromPreferences(Context ctx, SharedPreferences pref) {
        String freqKey = ctx.getString(R.string.update_freq_list);
        String freq = pref.getString(freqKey, "360");

        long seconds = Integer.parseInt(freq)*60;

        if (BuildConfig.DEBUG)
            seconds= 10;


        scheduleAlarm(ctx.getApplicationContext(), seconds*1000);

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action  = intent.getAction();
        Log.d(_log_tag, "Start service job");
        completeWakefulIntent(intent);

    }
}
