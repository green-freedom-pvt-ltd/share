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
import com.sharesmile.share.Badge;
import com.sharesmile.share.BadgeDao;
import com.sharesmile.share.R;
import com.sharesmile.share.Title;
import com.sharesmile.share.TitleDao;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.SharedPrefsManager;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.tracking.workout.WorkoutSingleton;
import com.sharesmile.share.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

import static com.sharesmile.share.core.application.MainApplication.getContext;
import static com.sharesmile.share.core.notifications.NotificationActionReceiver.AUTO_NOTIFICATION_ID;

public class AfterBadgeWonNotificationReceiver extends BroadcastReceiver {
    final String TAG = "AfterBadgeWonNotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (WorkoutSingleton.getInstance().isWorkoutActive() || !MainApplication.isLogin()) {
            return;
        }
        //todo comment auto notification
        if (true)
            return;
        Logger.d(TAG, "In " + TAG);
        NotificationTextSubClass notificationTextSubClass = checkForNotification();
        SharedPrefsManager.getInstance().setString(Constants.PREF_NOTIFICATION_BADGE_NOT_SEEN,new JSONObject().toString());
        if (notificationTextSubClass != null) {
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            // This try catch bug is to avoid crash due to runtimeexception
            try {
                NotificationCompat.Builder mBuilder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mBuilder = Utils.createChannelForNotification(getContext(),
                            getContext().getString(R.string.channel_name_general)
                            , getContext().getString(R.string.auto_notification_channel_description), true);
                } else {
                    mBuilder = new NotificationCompat.Builder(context);
                }

                mBuilder
                        .setContentTitle(notificationTextSubClass.title)
                        .setContentText(notificationTextSubClass.message)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationTextSubClass.message))
                        .setSmallIcon(getNotificationIcon())
                        .setColor(ContextCompat.getColor(getContext(), R.color.bright_sky_blue))
                        .setLargeIcon(getLargeIcon(context))
                        .setTicker(context.getResources().getString(R.string.app_name))
                        .setOngoing(false)
                        .setVisibility(1)
                        .setSound(Uri.parse("android.resource://"
                                + context.getPackageName() + "/" + R.raw.slow_spring_board))
                        .setVibrate(new long[]{0, 100, 200, 300});
//            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
//                mBuilder.addAction(pauseResumeDrawable, pauseResumeLabel, MainApplication.getInstance().createNotificationActionIntent(pauseResumeIntent, pauseResumeAction))
//                        .addAction(R.drawable.ic_stop_black_24px, "Stop", MainApplication.getInstance().createNotificationActionIntent(MainActivity.INTENT_STOP_RUN, getString(R.string.notification_action_stop)));
//            }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
                }

                mBuilder.setContentIntent(MainApplication.getInstance().createAppIntent());

                mNotificationManager.notify(AUTO_NOTIFICATION_ID, mBuilder.build());

            } catch (RuntimeException rte) {
                rte.printStackTrace();
                Crashlytics.logException(rte);
                Logger.e(TAG, "RuntimeException while updating sticky notification of automated notification");
            }
        }

    }

    private NotificationTextSubClass checkForNotification() {
        NotificationTextSubClass notificationTextSubClass = null;
        String message = "";
        String title = "";
        String buttonText = "";
        try {
            JSONObject jsonObject = new JSONObject(SharedPrefsManager.getInstance().getString(Constants.PREF_NOTIFICATION_BADGE_NOT_SEEN, "{}"));
            int onlyOne = 0;
            String keyUsed = "";
            Iterator<String> stringIterator = jsonObject.keys();
            while (stringIterator.hasNext()) {
                String key = stringIterator.next();
                if (jsonObject.getInt(key) > 0) {
                    if (keyUsed.length() == 0)
                        keyUsed = key;
                    onlyOne++;
                }
            }
            if (keyUsed.equals(Constants.TITLE_TYPE_CAUSE)) {
                int id = jsonObject.getInt(keyUsed);
                TitleDao titleDao = MainApplication.getInstance().getDbWrapper().getTitleDao();
                List<Title> titles = titleDao.queryBuilder().where(TitleDao.Properties.TitleId.eq(id)).list();
                if (titles.size() > 0) {
                    message = getContext().getResources().getString(R.string.auto_notification_new_title_title) + Utils.getEmoji(Constants.CROWN_EMOJI);
                    title = String.format(getContext().getResources().getString(R.string.auto_notification_new_title_message), titles.get(0).getTitle());
                    buttonText = getContext().getResources().getString(R.string.auto_notification_new_title_button_text) + Utils.getEmoji(Constants.SMILE_WITH_OPEN_MOUTH_EMOJI);
                }
            } else {
                int id = jsonObject.getInt(keyUsed);
                BadgeDao badgeDao = MainApplication.getInstance().getDbWrapper().getBadgeDao();
                List<Badge> badges = badgeDao.queryBuilder().where(BadgeDao.Properties.BadgeId.eq(id)).list();
                if (badges.size() > 0) {
                    message = getContext().getResources().getString(R.string.auto_notification_new_badge_title) + Utils.getEmoji(Constants.BADGE_EMOJI);
                    title = String.format(getContext().getResources().getString(R.string.auto_notification_new_badge_message), badges.get(0).getName());
                    buttonText = getContext().getResources().getString(R.string.auto_notification_new_badge_button_text) + Utils.getEmoji(Constants.SMILE_WITH_SUNGLASSES_EMOJI);
                }
            }


            if (message.length() > 0) {
                notificationTextSubClass = new NotificationTextSubClass(message, title, buttonText);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return notificationTextSubClass;
    }

    private Bitmap getLargeIcon(Context context) {
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        return largeIcon;
    }

    private int getNotificationIcon() {
        boolean useWhiteIcon = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.mipmap.ic_notification_small : R.mipmap.ic_launcher;
    }

    class NotificationTextSubClass {
        private String title;
        private String message;
        private String buttonText;

        public NotificationTextSubClass(String title, String message, String buttonText) {
            this.title = title;
            this.message = message;
            this.buttonText = buttonText;
        }
    }
}
