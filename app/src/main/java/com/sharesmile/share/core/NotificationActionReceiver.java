package com.sharesmile.share.core;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sharesmile.share.Events.PauseWorkoutEvent;
import com.sharesmile.share.Events.ResumeWorkoutEvent;
import com.sharesmile.share.R;
import com.sharesmile.share.gps.activityrecognition.ActivityDetector;
import com.sharesmile.share.utils.Logger;

import org.greenrobot.eventbus.EventBus;

import static android.content.Context.NOTIFICATION_SERVICE;

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

    public static final String NOTIFICATION_ID = "notification_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        int notifId = intent.getIntExtra(NOTIFICATION_ID, 0);
        Logger.d(TAG, "onReceive with action " + action + " and notifId: " + notifId);
        if(context.getString(R.string.notification_action_pause).equals(action)) {
            Logger.i(TAG, "Action Pause");
            EventBus.getDefault().post(new PauseWorkoutEvent());
        } else if(context.getString(R.string.notification_action_resume).equals(action)) {
            Logger.i(TAG, "Action Resume");
            EventBus.getDefault().post(new ResumeWorkoutEvent());
        } else if(context.getString(R.string.notification_action_stop).equals(action)) {
            Logger.i(TAG,"Action Stop");
        } else if(context.getString(R.string.notification_action_cancel).equals(action)) {
            Logger.i(TAG,"Action Cancel");
            if (notifId == WORKOUT_NOTIFICATION_WALK_ENGAGEMENT){
                ActivityDetector.getInstance().cancelWalkEngagementNotif();
            }else {
                NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                manager.cancel(notifId);
            }
        } else if (context.getString(R.string.notification_action_dismiss).equals(action)){
            Logger.i(TAG, "Action Dismiss");
            if (notifId == WORKOUT_NOTIFICATION_WALK_ENGAGEMENT){
                ActivityDetector.getInstance().userDismissedWalkEngagementNotif();
            }
        }
    }
}
