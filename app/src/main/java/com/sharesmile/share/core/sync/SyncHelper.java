package com.sharesmile.share.core.sync;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.google.gson.Gson;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.User;
import com.sharesmile.share.UserDao;
import com.sharesmile.share.core.config.ClientConfig;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.tracking.workout.data.model.FraudData;
import com.sharesmile.share.login.UserDetails;
import com.sharesmile.share.helpcenter.model.UserFeedback;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.SharedPrefsManager;

import java.util.List;

import static com.sharesmile.share.core.Constants.PREF_AUTH_TOKEN;
import static com.sharesmile.share.core.Constants.PREF_USER_EMAIL;
import static com.sharesmile.share.core.sync.TaskConstants.SYNC_DATA;

/**
 * Created by Shine on 20/07/16.
 */
public class SyncHelper {

    private static final String TAG = SyncHelper.class.getSimpleName();

    public static void scheduleDataSync(Context context) {

        PeriodicTask task = new PeriodicTask.Builder()
                .setService(SyncService.class)
                .setTag(SYNC_DATA)
                .setPeriod(ClientConfig.getInstance().DATA_SYNC_INTERVAL) // in secs , i.e. every 3 hours
                .setPersisted(true)
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                .setFlex(ClientConfig.getInstance().DATA_SYNC_INTERVAL_FLEX) // 1.5 hours
                .build();

        GcmNetworkManager mGcmNetworkManager = GcmNetworkManager.getInstance(context);
        mGcmNetworkManager.schedule(task);

    }

    /**
     * One time task to forcefully fetch all WorkoutData from server
     */
    public static void forceRefreshEntireWorkoutHistory() {
        if (MainApplication.getInstance().isLogin()){
            // User is logged in, can go for PULL of workout history
            SyncService.forceRefreshEntireWorkoutHistoryWithBackoff();
        }
    }

    /**
     *
     */
    public static void pushFraudData(FraudData data) {
        Gson gson = new Gson();
        Bundle bundle = new Bundle();
        bundle.putString(TaskConstants.FRAUD_DATA_JSON, gson.toJson(data));
        OneoffTask task = new OneoffTask.Builder()
                .setService(SyncService.class)
                .setTag(TaskConstants.PUSH_FRAUD_DATA)
                .setExtras(bundle)
                /*
                    Mandatory setter for creating a one-off task.
                    You specify the earliest point in time in the future from which your task might start executing,
                    as well as the latest point in time in the future at which your task must have executed.
                 */
                .setExecutionWindow(0L, 1L)
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                .build();

        GcmNetworkManager mGcmNetworkManager = GcmNetworkManager.getInstance(MainApplication.getContext().getApplicationContext());
        mGcmNetworkManager.schedule(task);
    }

    public static void pushUserFeedback(UserFeedback feedback) {
        Gson gson = new Gson();
        Bundle bundle = new Bundle();
        String feedbackJson = gson.toJson(feedback);
        bundle.putString(TaskConstants.FEEDBACK_DATA_JSON, feedbackJson);
        Logger.d(TAG, "Will push UserFeedback: " + feedbackJson);
        OneoffTask task = new OneoffTask.Builder()
                .setService(SyncService.class)
                .setTag(TaskConstants.PUSH_USER_FEEDBACK)
                .setExtras(bundle)
                /*
                    Mandatory setter for creating a one-off task.
                    You specify the earliest point in time in the future from which your task might start executing,
                    as well as the latest point in time in the future at which your task must have executed.
                 */
                .setExecutionWindow(0L, 5L)
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                .build();

        GcmNetworkManager mGcmNetworkManager = GcmNetworkManager.getInstance(MainApplication.getContext().getApplicationContext());
        mGcmNetworkManager.schedule(task);
    }

    public static void syncUserFromDB(){
        if (MainApplication.getInstance().getUserDetails() == null){
            int user_id = MainApplication.getInstance().getUserID();
            SharedPrefsManager prefsManager = SharedPrefsManager.getInstance();
            if (user_id != 0){
                UserDao mUserDao = MainApplication.getInstance().getDbWrapper().getDaoSession().getUserDao();
                User user;
                List<User> userList = mUserDao.queryBuilder().where(UserDao.Properties.Id.eq(user_id)).list();
                if (userList != null && !userList.isEmpty()) {
                    user = userList.get(0);
                    UserDetails details = new UserDetails();
                    details.setUserId(user_id);
                    details.setPhoneNumber(user.getMobileNO());
                    details.setBirthday(user.getBirthday());
                    details.setEmail(prefsManager.getString(PREF_USER_EMAIL));
                    details.setAuthToken(prefsManager.getString(PREF_AUTH_TOKEN));
                    details.setGenderUser(user.getGender());
                    details.setSignUp(prefsManager.getBoolean(Constants.PREF_IS_SIGN_UP_USER));
                    String prefTeamIdKey = "pref_league_team_id";
                    details.setTeamId(SharedPrefsManager.getInstance().getInt(prefTeamIdKey));
                    details.setSocialThumb(user.getProfileImageUrl());

                    MainApplication.getInstance().setUserDetails(details);
                }
            }
        }
    }

    public static void oneTimeUploadUserData() {
        OneoffTask task = new OneoffTask.Builder()
                .setService(SyncService.class)
                .setTag(TaskConstants.UPLOAD_USER_DATA)
                .setExecutionWindow(0L, 300) // Within 5 mins
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                .setPersisted(true)
                .build();

        GcmNetworkManager mGcmNetworkManager = GcmNetworkManager.getInstance(MainApplication.getContext());
        mGcmNetworkManager.schedule(task);
    }

    public static void uploadPendingWorkout() {
        OneoffTask task = new OneoffTask.Builder()
                .setService(SyncService.class)
                .setTag(TaskConstants.UPLOAD_PENDING_WORKOUT)
                .setExecutionWindow(0L, 300) // Within 5 mins
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                .setPersisted(true)
                .build();

        GcmNetworkManager mGcmNetworkManager = GcmNetworkManager.getInstance(MainApplication.getContext());
        mGcmNetworkManager.schedule(task);
    }

    public static void syncBadgesData() {
        OneoffTask task = new OneoffTask.Builder()
                .setService(SyncService.class)
                .setTag(TaskConstants.SYNC_BADGE_DATA)
                .setExecutionWindow(0L, 5)
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                .setPersisted(true)
                .build();

        GcmNetworkManager mGcmNetworkManager = GcmNetworkManager.getInstance(MainApplication.getContext());
        mGcmNetworkManager.schedule(task);
    }

    public static void syncMessageCenterData() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                SyncService.fetchMessage();
            }
        });
    }
}
