package com.sharesmile.share.home.settings;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.crashlytics.android.Crashlytics;
import com.sharesmile.share.R;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.tracking.workout.WorkoutSingleton;
import com.sharesmile.share.utils.Utils;

import static com.sharesmile.share.core.application.MainApplication.getContext;
import static com.sharesmile.share.core.notifications.NotificationActionReceiver.REMINDER_NOTIFICATION_ID;

public class AlarmReceiver extends BroadcastReceiver {
    final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(WorkoutSingleton.getInstance().isWorkoutActive())
        {
            return;
        }
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // This try catch bug is to avoid crash due to runtimeexception
        try {


            NotificationCompat.Builder mBuilder =null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mBuilder = Utils.createChannelForNotification(getContext(), getContext().getString(R.string.channel_description_general),
                        context.getString(R.string.channel_name_general), true);
            }else
            {
                mBuilder = new NotificationCompat.Builder(getContext());
            }

            mBuilder
                            .setContentTitle("Let\'s Go \uD83C\uDFC3")
                            .setContentText("Time to create some Impact.")
                            .setSmallIcon(getNotificationIcon())
                            .setColor(ContextCompat.getColor(getContext(), R.color.bright_sky_blue))
                            .setLargeIcon(getLargeIcon(context))
                            .setTicker(context.getResources().getString(R.string.app_name))
                            .setOngoing(false)
                            .setVisibility(1)
                            .setSound( Uri.parse("android.resource://"
                                    + context.getPackageName() + "/" + R.raw.slow_spring_board))
                    .setVibrate(new long[]{0,100,200,300})
                            ;


//            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
//                mBuilder.addAction(pauseResumeDrawable, pauseResumeLabel, MainApplication.getInstance().createNotificationActionIntent(pauseResumeIntent, pauseResumeAction))
//                        .addAction(R.drawable.ic_stop_black_24px, "Stop", MainApplication.getInstance().createNotificationActionIntent(MainActivity.INTENT_STOP_RUN, getString(R.string.notification_action_stop)));
//            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
            }

            mBuilder.setContentIntent(MainApplication.getInstance().createAppIntent());

            mNotificationManager.notify(REMINDER_NOTIFICATION_ID, mBuilder.build());
        } catch (RuntimeException rte) {
            rte.printStackTrace();
            Crashlytics.logException(rte);
            Logger.e(TAG, "RuntimeException while updating sticky notification of reminder");
        }

    }

    private Bitmap getLargeIcon(Context context) {
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        return largeIcon;
    }
    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.mipmap.ic_notification_small : R.mipmap.ic_launcher;
    }
}
