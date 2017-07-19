package com.sharesmile.share.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.sharesmile.share.utils.Logger;

/**
 * Created by ankitmaheshwari on 7/17/17.
 */

public class BootReceiver extends BroadcastReceiver{

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.i(TAG, "onReceive");
        // Application just started
        SystemClock.elapsedRealtime();
    }

}
