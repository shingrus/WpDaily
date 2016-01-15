package com.shingrus.wpdaily;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * Created by shingrus on 13/01/16.
 * Alarm receiver for periodic alarm
 */
public class WPAlarmReciever extends WakefulBroadcastReceiver {

    private static final String _log_tag = "WPD/AlarmReceiver";
    public static final String ACTION_ALARM = "com.shingrus.wpdaily.action.alarm";
    public static final int ALARM_CODE = 0x17;


    @Override
    public void onReceive(Context ctx, Intent intent) {
        Log.d(_log_tag, "Got intent:" + intent.getAction());

        Log.d(_log_tag, "Recieved alarm broadcast");
        Intent serviceIntent = new Intent(ACTION_ALARM, null, ctx, WPUpdateService.class);

        startWakefulService(ctx, serviceIntent);


    }
}
