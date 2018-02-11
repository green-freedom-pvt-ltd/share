package com.sharesmile.share.core.timekeeping;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sharesmile.share.core.Logger;

/**
 * Created by ankitmaheshwari on 6/27/17.
 */

public class TimeChangedReceiver extends BroadcastReceiver {
    private static final String TAG = "TimeChangedReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.d(TAG, "onReceive");
        // Someone changed system clock time, setTimerOutOfSync
        ServerTimeKeeper.getInstance().checkIfTimerIsOutOfSync();
    }
}
