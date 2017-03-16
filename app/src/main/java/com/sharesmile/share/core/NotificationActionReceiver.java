package com.sharesmile.share.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sharesmile.share.utils.Logger;

/**
 * Created by ankitmaheshwari on 3/15/17.
 */


public class NotificationActionReceiver extends BroadcastReceiver {

    private static final String TAG = "NotificationActionReceiver";

    public static final String WORKOUT_PAUSE = "workout_pause";
    public static final String WORKOUT_RESUME = "workout_resume";
    public static final String WORKOUT_STOP = "workout_stop";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if(WORKOUT_PAUSE.equals(action)) {
            Logger.i(TAG, "Action Pause");
        } else if(WORKOUT_RESUME.equals(action)) {
            Logger.i(TAG, "Action Resume");
        } else if(WORKOUT_STOP.equals(action)) {
            Logger.i(TAG,"Action Stop");
        }
    }
}
