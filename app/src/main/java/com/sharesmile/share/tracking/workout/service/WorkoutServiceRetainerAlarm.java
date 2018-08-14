package com.sharesmile.share.tracking.workout.service;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.sharesmile.share.core.Logger;
import com.sharesmile.share.tracking.workout.WorkoutSingleton;

/**
 * Created by ankitmaheshwari on 6/26/17.
 */

public class WorkoutServiceRetainerAlarm extends WakefulBroadcastReceiver {

    private static final String TAG = "WorkoutServiceRetainerAlarm";
    public static final String ACTION = "com.sharesmile.share.gps.START_ALARM";

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.d(TAG, "onReceive");
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        wl.acquire();

        // Start WorkoutService if required
        if (WorkoutSingleton.getInstance().isWorkoutActive()){
            Intent serviceIntent = new Intent(context, WorkoutService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }

        }

        wl.release();
    }

    public static void setRepeatingAlarm(Context context) {
       /* Intent i = new Intent(context, WorkoutServiceRetainerAlarm.class);
        i.setAction(ACTION);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, FLAG_UPDATE_CURRENT);
        // Start alarm after 3 mins and repeat after every 2 mins from then on
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000*60*3, 1000*60*2, pi);*/
    }

    public static void cancelAlarm(Context context) {
        /*Intent intent = new Intent(context, WorkoutServiceRetainerAlarm.class);
        intent.setAction(ACTION);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);*/
    }

}
