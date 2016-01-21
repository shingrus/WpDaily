package com.shingrus.wpdaily;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * Created by shingrus on 27/11/15.
 * Broadcast receiver for BootCompleted
 */
public class BootCompletedReceiver extends WakefulBroadcastReceiver {
    private final static String _log_tag = "WPD/BootReciever";


    @Override
    public void onReceive(Context ctx, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            //Do nothing, looks like job started

            Log.d(_log_tag, "Recieve reboot broadcast");
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
            Boolean onBootEnabled = pref.getBoolean(ctx.getString(R.string.onBootEnabledKey), true);

            if (onBootEnabled) {
                WPUpdateService.restartJobFromPreferences(ctx, pref);

            }

        }
    }
}
