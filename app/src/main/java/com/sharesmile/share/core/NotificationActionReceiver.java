package com.sharesmile.share.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sharesmile.share.Events.PauseWorkoutEvent;
import com.sharesmile.share.Events.ResumeWorkoutEvent;
import com.sharesmile.share.R;
import com.sharesmile.share.utils.Logger;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by ankitmaheshwari on 3/15/17.
 */


public class NotificationActionReceiver extends BroadcastReceiver {

    private static final String TAG = "NotificationActionReceiver";

    public static final int WORKOUT_NOTIFICATION_GPS_INACTIVE_ID = 103;
    public static final int WORKOUT_NOTIFICATION_DISABLE_MOCK_ID = 104;
    public static final int WORKOUT_NOTIFICATION_USAIN_BOLT_ID = 105;
    public static final int WORKOUT_NOTIFICATION_USAIN_BOLT_FORCE_EXIT_ID = 106;
    public static final int WORKOUT_NOTIFICATION_STILL_ID = 107;
    public static final int WORKOUT_NOTIFICATION_WALK_ENGAGEMENT = 108;


    public static final int WORKOUT_NOTIFICATION_ID = 101;
    public static final int WORKOUT_TRACK_NOTIFICATION_ID = 102;

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.d(TAG, "onReceive with action " + intent.getAction());
        String action = intent.getAction();
        if(context.getString(R.string.notification_action_pause).equals(action)) {
            Logger.i(TAG, "Action Pause");
            EventBus.getDefault().post(new PauseWorkoutEvent());
        } else if(context.getString(R.string.notification_action_resume).equals(action)) {
            Logger.i(TAG, "Action Resume");
            EventBus.getDefault().post(new ResumeWorkoutEvent());
        } else if(context.getString(R.string.notification_action_stop).equals(action)) {
            Logger.i(TAG,"Action Stop");
        }
    }
}
