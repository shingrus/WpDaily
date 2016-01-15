package com.shingrus.wpdaily;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by shingrus on 16/01/16.
 * This receiver catches network state intents
 */
public class NetworkBroadcastReceiver extends BroadcastReceiver {

    private static final String _log_tag = "WPD/NetworkBroadcast";
    public static final String ACTION_NETWORK = "com.shingrus.wpdaily.action.network";
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(_log_tag, "Received network broadcast");
        Intent  serviceIntent = new Intent(ACTION_NETWORK, null, context, WPUpdateService.class);
        context.startService(serviceIntent);


    }
}
