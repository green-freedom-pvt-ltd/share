package com.sharesmile.share.tracking.activityrecognition;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.SharedPrefsManager;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static com.sharesmile.share.core.Constants.PREF_DISABLE_ALERTS;

/**
 * Created by ankitmaheshwari on 9/13/17.
 */

public class InvokeActivityDetectionAlarm extends BroadcastReceiver {

    private static final String TAG = "InvokeActivityDetectionAlarm";
    public static final String ACTION = "com.sharesmile.share.tracking.activityrecognition.START_ACTIVITY_DETECTION_ALARM";

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.d(TAG, "onReceive");
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        wl.acquire();

        // Start Activity detection in ActivityDetector only if user has allowed to show alerts
        // Background activity detection is being used for WALK_ENGAGEMENT notification only
        if (!SharedPrefsManager.getInstance().getBoolean(PREF_DISABLE_ALERTS)){
            ActivityDetector.getInstance().startActivityDetection();
        }

        wl.release();
    }

    /**
     * Sets the InvokeActivityDetectionAlarm to go off after specified time interval
     * @param context
     * @param scheduleAfterMillis time interval in millisecs after which the alarm should go off
     */
    public static void setAlarm(Context context, long scheduleAfterMillis) {
        Logger.d(TAG, "setAlarm");
        Intent intent = new Intent(context, InvokeActivityDetectionAlarm.class);
        intent.setAction(ACTION);
        PendingIntent pi = PendingIntent.getBroadcast(context, 10, intent, FLAG_UPDATE_CURRENT);
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        // Set the alarm to go off after given time (scheduleAfterMillis)
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + scheduleAfterMillis, pi);
    }

    public static void cancelAlarm(Context context) {
        Logger.d(TAG, "cancelAlarm");
        Intent intent = new Intent(context, InvokeActivityDetectionAlarm.class);
        intent.setAction(ACTION);
        PendingIntent sender = PendingIntent.getBroadcast(context, 10, intent, FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}
