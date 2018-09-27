package com.sharesmile.share.home.settings;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import com.sharesmile.share.Title;
import com.sharesmile.share.TitleDao;
import com.sharesmile.share.Workout;
import com.sharesmile.share.WorkoutDao;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.MainActivity;
import com.sharesmile.share.core.SharedPrefsManager;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.login.UserDetails;
import com.sharesmile.share.tracking.workout.WorkoutSingleton;
import com.sharesmile.share.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.sharesmile.share.core.Constants.PREF_TOTAL_IMPACT;
import static com.sharesmile.share.core.application.MainApplication.getContext;
import static com.sharesmile.share.core.notifications.NotificationActionReceiver.AUTO_NOTIFICATION_ID;

public class NotificationReceiver extends BroadcastReceiver {
    final String TAG = "NotificationReceiver";
    final int BADGE_ACHIEVE_ID = 1;
    final int TITLE_ACHIEVE_ID = 2;
    final int MAX_STREAK_ID = 3;
    final int DAILY_GOAL_ID = 4;
    final int STREAK_0_ID = 5;


    @Override
    public void onReceive(Context context, Intent intent) {
        Utils.setAutoNotification(getContext());
        if (WorkoutSingleton.getInstance().isWorkoutActive() ||
                !Utils.isAppForground(context) || !MainApplication.isLogin()) {
            return;
        }
        //todo comment auto notification
        if (true)
            return;

        Logger.d(TAG, "In " + TAG);
        NotificationSubClass notificationSubClass = checkForNotification();
        if (notificationSubClass != null) {
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            // This try catch bug is to avoid crash due to runtimeexception
            try {
                NotificationCompat.Builder mBuilder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mBuilder = Utils.createChannelForNotification(getContext(), getContext().getString(R.string.auto_notification_channel_description),
                            context.getString(R.string.channel_name_general), true);
                } else {
                    mBuilder = new NotificationCompat.Builder(context);
                }
                String title = "";
                String message = "";
                String buttonText = "";
                switch (notificationSubClass.id) {
                    case BADGE_ACHIEVE_ID:
                        BadgeDao badgeDao = MainApplication.getInstance().getDbWrapper().getBadgeDao();
                        title = context.getString(R.string.auto_notification_badge_title) + Utils.getEmoji(Constants.FLAG_EMOJI);
                        buttonText = context.getString(R.string.auto_notification_badge_button_text) + Utils.getEmoji(Constants.BADGE_EMOJI);
                        if (notificationSubClass.badgeId != -1) {
                            List<Badge> badges = badgeDao.queryBuilder().where(BadgeDao.Properties.BadgeId.eq(notificationSubClass.badgeId)).list();
                            if (badges.size() > 0) {
                                message = String.format(context.getString(R.string.auto_notification_badge_message), notificationSubClass.distance, badges.get(0).getName());
                            }
                        } else {
                            message = context.getString(R.string.auto_notification_multiple_badge_message);
                        }
                        break;
                    case TITLE_ACHIEVE_ID:
                        TitleDao titleDao = MainApplication.getInstance().getDbWrapper().getTitleDao();
                        title = context.getString(R.string.auto_notification_badge_title) + Utils.getEmoji(Constants.FLAG_EMOJI);
                        buttonText = context.getString(R.string.auto_notification_title_button_text) + Utils.getEmoji(Constants.CROWN_EMOJI);
                        if (notificationSubClass.badgeId != -1) {
                            List<Title> titles = titleDao.queryBuilder().where(TitleDao.Properties.TitleId.eq(notificationSubClass.badgeId)).list();
                            if (titles.size() > 0) {
                                message = String.format(context.getString(R.string.auto_notification_title_message), notificationSubClass.distance, titles.get(0).getTitle());
                            }
                        } else {
                            message = context.getString(R.string.auto_notification_multiple_title_message);
                        }
                        break;
                    case MAX_STREAK_ID:
                        title = context.getString(R.string.auto_notification_max_streak_title);
                        message = String.format(context.getString(R.string.auto_notification_max_streak_message), notificationSubClass.distance);
                        buttonText = context.getString(R.string.auto_notification_max_streak_button_text) + Utils.getEmoji(Constants.FIRE_EMOJI);
                        break;
                    case DAILY_GOAL_ID:
                        title = context.getString(R.string.auto_notification_daily_goal_title);
                        message = String.format(context.getString(R.string.auto_notification_daily_goal_message), notificationSubClass.distance) + Utils.getEmoji(Constants.RUN_EMOJI);
                        buttonText = "";
                        break;
                    case STREAK_0_ID:
                        title = context.getString(R.string.auto_notification_start_streak_title) + Utils.getEmoji(Constants.FIRE_EMOJI);
                        message = context.getString(R.string.auto_notification_start_streak_message) + Utils.getEmoji(Constants.RUN_EMOJI);
                        buttonText = "";
                        break;

                }

                mBuilder
                        .setContentTitle(title)
                        .setContentText(message)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setSmallIcon(getNotificationIcon())
                        .setColor(ContextCompat.getColor(getContext(), R.color.bright_sky_blue))
                        .setLargeIcon(getLargeIcon(context))
                        .setTicker(context.getResources().getString(R.string.app_name))
                        .setOngoing(false)
                        .setVisibility(1)
                        .setSound(Uri.parse("android.resource://"
                                + context.getPackageName() + "/" + R.raw.slow_spring_board))
                        .setVibrate(new long[]{0, 100, 200, 300});
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH && buttonText.length()!=0) {
                if(title.equals(context.getString(R.string.auto_notification_new_badge_title)) ||
                        title.equals(context.getString(R.string.auto_notification_new_badge_title)))
                {
                    mBuilder.addAction(/*R.drawable.ic_stop_black_24px*/0, buttonText, MainApplication.getInstance().createNotificationActionIntent(MainActivity.INTENT_BADGE, context.getString(R.string.notification_action_badge)));
                }else {
                    mBuilder.addAction(/*R.drawable.ic_stop_black_24px*/0, buttonText, MainApplication.getInstance().createNotificationActionIntent(MainActivity.INTENT_HOME, context.getString(R.string.notification_action_home)));
                }
            }

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

    private NotificationSubClass checkForNotification() {
        boolean checkCondition = false;
        NotificationSubClass notificationSubClass = null;
        int lifeTimeImpact = SharedPrefsManager.getInstance().getInt(PREF_TOTAL_IMPACT);
        //todo write condition
        if (lifeTimeImpact != 0) {
            /*****************************/
            AchievedBadgeDao achievedBadgeDao = MainApplication.getInstance().getDbWrapper().getAchievedBadgeDao();
            //less than 1km for title
            BadgeDao badgeDao = MainApplication.getInstance().getDbWrapper().getBadgeDao();
            TitleDao titleDao = MainApplication.getInstance().getDbWrapper().getTitleDao();
            SQLiteDatabase database = MainApplication.getInstance().getDbWrapper().getDaoSession().getDatabase();
            Cursor cursor = database.rawQuery("SELECT " + AchievedBadgeDao.Properties.Category.columnName
                    + ", SUM(" + AchievedBadgeDao.Properties.NoOfStarAchieved.columnName + ") AS no_of_stars"
                    + " FROM " + AchievedBadgeDao.TABLENAME +
                    " GROUP BY " + AchievedBadgeDao.Properties.Category.columnName + " having " +
                    AchievedBadgeDao.Properties.UserId.columnName + " is "
                    + MainApplication.getInstance().getUserID() +
                    " and " + AchievedBadgeDao.Properties.BadgeType.columnName + " is '" + Constants.BADGE_TYPE_CAUSE + "" + "'", new String[]{});
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    int category = cursor.getInt(0);
                    int no_of_stars = cursor.getInt(1);
                    int id = -1;
                    List<Title> titles = titleDao.queryBuilder()
                            .where(TitleDao.Properties.CategoryId.eq(category))
                            .orderAsc(TitleDao.Properties.GoalNStars).list();
                    double distancePending = -1;
                    for (int i = 0; i < titles.size(); i++) {
                        if (no_of_stars == titles.get(i).getGoalNStars() - 1) {
                            List<AchievedBadge> achievedBadgesForTitle = achievedBadgeDao.queryBuilder()
                                    .where(AchievedBadgeDao.Properties.Category.eq(category),
                                            AchievedBadgeDao.Properties.CategoryStatus.eq(Constants.BADGE_IN_PROGRESS)).list();
                            distancePending = -1;
                            for (AchievedBadge achievedBadge :
                                    achievedBadgesForTitle) {
                                if (achievedBadge.getBadgeIdInProgress() != achievedBadge.getBadgeIdAchieved()) {
                                    List<Badge> badges = badgeDao.queryBuilder()
                                            .where(BadgeDao.Properties.BadgeId.eq(achievedBadge.getBadgeIdInProgress()))
                                            .list();
                                    if (badges.size() > 0 &&
                                            achievedBadge.getParamDone() >= badges.get(0).getBadgeParameter() - 1 &&
                                            achievedBadge.getParamDone() < badges.get(0).getBadgeParameter()) {
                                        double distanceDiff = badges.get(0).getBadgeParameter() - achievedBadge.getParamDone();
                                        if (distancePending == -1 ||
                                                distanceDiff < distancePending) {
                                            distancePending = distanceDiff;
                                            id = titles.get(i).getTitleId();
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (distancePending != -1) {
                        if (notificationSubClass == null)
                            notificationSubClass = new NotificationSubClass(id, TITLE_ACHIEVE_ID, distancePending);
                        else {
                            notificationSubClass.badgeId = -1;
                        }
                        checkCondition = true;
                    }
                    cursor.moveToNext();
                }
            }
            if (checkCondition)
                return notificationSubClass;
            // if any badge contains less than 1 km left for
            List<AchievedBadge> achievedBadges = achievedBadgeDao.queryBuilder()
                    .where(AchievedBadgeDao.Properties.UserId.eq(MainApplication.getInstance().getUserID()),
                            AchievedBadgeDao.Properties.CategoryStatus.eq(Constants.BADGE_IN_PROGRESS)).list();
            if (achievedBadges.size() > 0) {
                for (AchievedBadge achievedBadge :
                        achievedBadges) {
                    List<Badge> badges = badgeDao.queryBuilder()
                            .where(BadgeDao.Properties.BadgeId.eq(achievedBadge.getBadgeIdInProgress())).list();
                    if (badges.size() > 0) {
                        if (achievedBadge.getParamDone() >= badges.get(0).getBadgeParameter() - 1 &&
                                achievedBadge.getParamDone() < badges.get(0).getBadgeParameter()) {
                            if (notificationSubClass == null) {
                                notificationSubClass = new NotificationSubClass(BADGE_ACHIEVE_ID,
                                        achievedBadge.getBadgeIdInProgress(),
                                        badges.get(0).getBadgeParameter() - achievedBadge.getParamDone());
                            } else {
                                notificationSubClass.badgeId = -1;
                            }
                            checkCondition = true;
                        }
                    }
                }
            }

            if (checkCondition)
                return notificationSubClass;

            /*****************************/
            //less than 1km from achieving max streak
            //less than 1km from achieving daily goal
            UserDetails userDetails = MainApplication.getInstance().getUserDetails();
            if (!userDetails.isStreakAdded()) {
                if (userDetails.getStreakRunProgress() >= userDetails.getStreakGoalDistance() - 1 &&
                        userDetails.getStreakRunProgress() < userDetails.getStreakGoalDistance()) {
                    double kmsLeft = userDetails.getStreakGoalDistance() - userDetails.getStreakRunProgress();
                    if (!SharedPrefsManager.getInstance().getBoolean(Constants.PREF_NOTIFICATION_MAX_STREAK_SHOWN, false) &&
                            userDetails.getStreakCount() == userDetails.getStreakMaxCount()) {
                        notificationSubClass = new NotificationSubClass(MAX_STREAK_ID, 0, kmsLeft);
                    } else {
                        notificationSubClass = new NotificationSubClass(DAILY_GOAL_ID, 0, kmsLeft);
                    }
                    checkCondition = true;
                }
            }
            if (checkCondition)
                return notificationSubClass;
        }
        /********************/
        //if streak is 0 for past 3 days
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        try {
            Date streadAddedDate = simpleDateFormat.parse(SharedPrefsManager.getInstance().getString(Constants.PREF_NOTIFICATION_STREAK_ADDED_DATE, Utils.getCurrentDateDDMMYYYY()));
            Date currentDate = simpleDateFormat.parse(Utils.getCurrentDateDDMMYYYY());
            long diff = currentDate.getTime() - streadAddedDate.getTime();
            float dayCount = (float) diff / (24 * 60 * 60 * 1000);
            if (dayCount % 3 == 0) {
                if (!SharedPrefsManager.getInstance().getBoolean(Constants.PREF_NOTIFICATION_0_STREAK_SHOWN, false)) {
                    notificationSubClass = new NotificationSubClass(STREAK_0_ID, 0, 0.0);
                    checkCondition = true;
                } else {
                    WorkoutDao workoutDao = MainApplication.getInstance().getDbWrapper().getWorkoutDao();
                    Calendar today = Calendar.getInstance();
                    today.setTimeInMillis(currentDate.getTime());
                    today.add(Calendar.DAY_OF_MONTH, -3);
                    long begin = Utils.getEpochForBeginningOfDay(today);
                    List<Workout> workouts = workoutDao.queryBuilder().where(WorkoutDao.Properties.BeginTimeStamp.ge(begin)).list();
                    if (workouts.size() == 0) {
                        notificationSubClass = new NotificationSubClass(STREAK_0_ID, 0, 0.0);
                        checkCondition = true;
                    }
                }
            }
            if (checkCondition)
                return notificationSubClass;
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

    class NotificationSubClass {
        private int id;
        private long badgeId;
        private double distance;

        public NotificationSubClass(int id, long badgeId, double distance) {
            this.id = id;
            this.badgeId = badgeId;
            this.distance = distance;
        }
    }
}
