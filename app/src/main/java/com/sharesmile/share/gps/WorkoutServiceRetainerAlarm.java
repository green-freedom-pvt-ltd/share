package com.sharesmile.share.gps;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.sharesmile.share.utils.Logger;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

/**
 * Created by ankitmaheshwari on 6/26/17.
 */

public class WorkoutServiceRetainerAlarm extends BroadcastReceiver {

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
            context.startService(serviceIntent);
        }

        wl.release();
    }

    public static void setRepeatingAlarm(Context context) {
        Intent i = new Intent(context, WorkoutServiceRetainerAlarm.class);
        i.setAction(ACTION);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, FLAG_UPDATE_CURRENT);
        // Start alarm after 3 mins and repeat after every 2 mins from then on
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, 1000*60*3, 2000*60*3, pi);
    }

    public static void cancelAlarm(Context context) {
        Intent intent = new Intent(context, WorkoutServiceRetainerAlarm.class);
        intent.setAction(ACTION);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

}
