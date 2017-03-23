package com.sharesmile.share.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.sharesmile.share.utils.Logger;

/**
 * Created by ankitmaheshwari on 3/15/17.
 */


public class NotificationActionReceiver extends BroadcastReceiver {

    private static final String TAG = "NotificationActionReceiver";

    public static final int WORKOUT_PAUSE = 1;
    public static final int WORKOUT_RESUME = 2;
    public static final int WORKOUT_STOP = 3;

    public static final String NOTIFICATION_ACTION = "com.sharesmile.share.notification.ACTION";
    public static final String NOTIFICATION_ACTION_BUTTON = "action_button";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (NOTIFICATION_ACTION.equals(action)){
            Bundle bundle = intent.getExtras();
            int button = bundle.getInt(NOTIFICATION_ACTION_BUTTON);

            if(WORKOUT_PAUSE == button) {
                Logger.i(TAG, "Action Pause");
            } else if(WORKOUT_RESUME == button) {
                Logger.i(TAG, "Action Resume");
            } else if(WORKOUT_STOP == button) {
                Logger.i(TAG,"Action Stop");
            }
        }


    }
}
