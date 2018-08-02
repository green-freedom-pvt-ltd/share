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
import com.sharesmile.share.AchievedBadge;
import com.sharesmile.share.AchievedBadgeDao;
import com.sharesmile.share.Badge;
import com.sharesmile.share.BadgeDao;
import com.sharesmile.share.R;
import com.sharesmile.share.Workout;
import com.sharesmile.share.WorkoutDao;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.SharedPrefsManager;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.login.UserDetails;
import com.sharesmile.share.tracking.workout.WorkoutSingleton;
import com.sharesmile.share.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.sharesmile.share.core.application.MainApplication.getContext;
import static com.sharesmile.share.core.notifications.NotificationActionReceiver.REMINDER_NOTIFICATION_ID;

public class NotificationReceiver extends BroadcastReceiver {
    final String TAG = "NotificationReceiver";
    final long MAX_STREAK_ID = -1;
    final long DAILY_GOAL_ID = -2;
    final long STREAK_0_ID = -3;
    @Override
    public void onReceive(Context context, Intent intent) {
        if(WorkoutSingleton.getInstance().isWorkoutActive())
        {
            return;
        }
        HashMap<Long,Double> badgeIdKmsLeftHashMap = checkForNotification();
        if(badgeIdKmsLeftHashMap!=null) {
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            // This try catch bug is to avoid crash due to runtimeexception
            try {
                NotificationCompat.Builder mBuilder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mBuilder = Utils.createChannelForNotification(getContext(),getContext().getString(R.string.auto_notification_channel_description));
                } else {
                    mBuilder = new NotificationCompat.Builder(context);
                }
                String title ="";
                String message = "";
                String buttonText = "";
                Iterator it = badgeIdKmsLeftHashMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<Long,Double> pair = (Map.Entry<Long, Double>) it.next();
                    if(pair.getKey() == MAX_STREAK_ID)
                    {

                    }else if(pair.getKey() == DAILY_GOAL_ID)
                    {

                    }else if(pair.getKey() == STREAK_0_ID)
                    {

                    }else
                    {

                    }
                    it.remove(); // avoids a ConcurrentModificationException
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
                        .setSound(Uri.parse("android.resource://"
                                + context.getPackageName() + "/" + R.raw.slow_spring_board))
                        .setVibrate(new long[]{0, 100, 200, 300})
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
                Logger.e(TAG, "RuntimeException while updating sticky notification of automated notification");
            }
        }

    }

    private HashMap<Long,Double> checkForNotification() {
        HashMap<Long,Double> badgeIdKmsLeftHashMap = new HashMap<>();
        boolean checkCondition = false;

        // if any badge contains less than 1 km left for
        AchievedBadgeDao achievedBadgeDao = MainApplication.getInstance().getDbWrapper().getAchievedBadgeDao();
        List<AchievedBadge> achievedBadges = achievedBadgeDao.queryBuilder()
                .where(AchievedBadgeDao.Properties.UserId.eq(MainApplication.getInstance().getUserID()),
                        AchievedBadgeDao.Properties.CategoryStatus.eq(Constants.BADGE_IN_PROGRESS)).list();
        if(achievedBadges.size()>0)
        {
            BadgeDao badgeDao = MainApplication.getInstance().getDbWrapper().getBadgeDao();
            for (AchievedBadge achievedBadge :
                    achievedBadges) {
                List<Badge> badges = badgeDao.queryBuilder()
                        .where(BadgeDao.Properties.BadgeId.eq(achievedBadge.getBadgeIdInProgress())).list();
                if(badges.size()>0)
                {
                    if(achievedBadge.getParamDone()>=badges.get(0).getBadgeParameter()-1 &&
                            achievedBadge.getParamDone()<badges.get(0).getBadgeParameter())
                    {
                        badgeIdKmsLeftHashMap.put(achievedBadge.getBadgeIdInProgress(),badges.get(0).getBadgeParameter()-achievedBadge.getParamDone());
                        checkCondition = true;
                    }
                }
            }
        }

        if(checkCondition)
            return badgeIdKmsLeftHashMap;
        /*****************************/
        //less than 1km from achieving max streak
        //less than 1km from achieving daily goal
        UserDetails userDetails = MainApplication.getInstance().getUserDetails();
        if(!userDetails.isStreakAdded())
        {
            if(userDetails.getStreakRunProgress()>=userDetails.getStreakGoalDistance()-1 &&
                            userDetails.getStreakRunProgress()<userDetails.getStreakGoalDistance())
            {
                double kmsLeft = userDetails.getStreakGoalDistance() - userDetails.getStreakRunProgress();
                if(!SharedPrefsManager.getInstance().getBoolean(Constants.PREF_NOTIFICATION_MAX_STREAK_SHOWN,false) &&
                        userDetails.getStreakCount()==userDetails.getStreakMaxCount()) {
                    badgeIdKmsLeftHashMap.put(MAX_STREAK_ID, kmsLeft);
                }else
                {
                    badgeIdKmsLeftHashMap.put(DAILY_GOAL_ID, kmsLeft);
                }
                checkCondition = true;
            }
        }
        if(checkCondition)
            return badgeIdKmsLeftHashMap;
        /********************/
        //if streak is 0 for past 3 days
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        try {
            Date streadAddedDate = simpleDateFormat.parse(SharedPrefsManager.getInstance().getString(Constants.PREF_NOTIFICATION_STREAK_ADDED_DATE,Utils.getCurrentDateDDMMYYYY()));
            Date currentDate = simpleDateFormat.parse(Utils.getCurrentDateDDMMYYYY());
            long diff = currentDate.getTime() - streadAddedDate.getTime();
            float dayCount = (float) diff / (24 * 60 * 60 * 1000);
            if(dayCount%3==0)
            {
                if(!SharedPrefsManager.getInstance().getBoolean(Constants.PREF_NOTIFICATION_0_STREAK_SHOWN,false)) {
                    badgeIdKmsLeftHashMap.put(STREAK_0_ID, 0.0);
                    checkCondition = true;
                }else
                {
                    WorkoutDao workoutDao = MainApplication.getInstance().getDbWrapper().getWorkoutDao();
                    Calendar today = Calendar.getInstance();
                    today.setTimeInMillis(currentDate.getTime());
                    today.add(Calendar.DAY_OF_MONTH,-3);
                    long begin = Utils.getEpochForBeginningOfDay(today);
                    List<Workout> workouts = workoutDao.queryBuilder().where(WorkoutDao.Properties.BeginTimeStamp.ge(begin)).list();
                    if(workouts.size()==0)
                    {
                        badgeIdKmsLeftHashMap.put(STREAK_0_ID, 0.0);
                        checkCondition = true;
                    }
                }
            }
            if(checkCondition)
                return badgeIdKmsLeftHashMap;
            /********************/
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Bitmap getLargeIcon(Context context) {
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        return largeIcon;
    }
    private int getNotificationIcon() {
        boolean useWhiteIcon = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.mipmap.ic_notification_small : R.mipmap.ic_launcher;
    }
}
