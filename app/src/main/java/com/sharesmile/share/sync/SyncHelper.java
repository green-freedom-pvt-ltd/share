package com.sharesmile.share.sync;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.google.gson.Gson;
import com.sharesmile.share.Events.DBEvent;
import com.sharesmile.share.LeaderBoardDataStore;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.MessageDao;
import com.sharesmile.share.User;
import com.sharesmile.share.UserDao;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.gcm.SyncService;
import com.sharesmile.share.gcm.TaskConstants;
import com.sharesmile.share.network.NetworkDataProvider;
import com.sharesmile.share.network.NetworkException;
import com.sharesmile.share.rfac.models.FraudData;
import com.sharesmile.share.rfac.models.UserDetails;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.SharedPrefsManager;
import com.sharesmile.share.utils.Urls;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import Models.CampaignList;
import Models.MessageList;

import static com.sharesmile.share.core.Constants.PREF_AUTH_TOKEN;
import static com.sharesmile.share.core.Constants.PREF_USER_EMAIL;
import static com.sharesmile.share.core.Constants.PREF_USER_NAME;
import static com.sharesmile.share.gcm.TaskConstants.SYNC_CAUSE_DATA;
import static com.sharesmile.share.gcm.TaskConstants.SYNC_LEADERBOARD_DATA;
import static com.sharesmile.share.gcm.TaskConstants.SYNC_SERVER_TIME;
import static com.sharesmile.share.gcm.TaskConstants.SYNC_WORKOUT_DATA;
import static com.sharesmile.share.gcm.TaskConstants.UPLOAD_USER_DATA;

/**
 * Created by Shine on 20/07/16.
 */
public class SyncHelper {

    private static final String TAG = SyncHelper.class.getSimpleName();

    public static void scheduleRunDataSync(Context context) {

        PeriodicTask task = new PeriodicTask.Builder()
                .setService(SyncService.class)
                .setTag(SYNC_WORKOUT_DATA)
                .setPeriod(7200L) // in secs , i.e. every two hour
                .setPersisted(true)
                .setFlex(2400)
                .build();

        GcmNetworkManager mGcmNetworkManager = GcmNetworkManager.getInstance(context);
        mGcmNetworkManager.schedule(task);

    }

    /**
     * One time task to forcefully fetch all WorkoutData from server
     */
    public static void forceRefreshEntireWorkoutHistory() {
        OneoffTask task = new OneoffTask.Builder()
                .setService(SyncService.class)
                .setTag(TaskConstants.FORCE_REFRESH_ENTIRE_WORKOUT_HISTORY)
                .setExecutionWindow(0L, 5L)
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                .setPersisted(true)
                .build();

        GcmNetworkManager mGcmNetworkManager = GcmNetworkManager
                .getInstance(MainApplication.getContext().getApplicationContext());
        mGcmNetworkManager.schedule(task);
    }

    /**
     * One time task to push all the dirty runs (is_sync false) from client to server
     * It will retry if the upload fails for some reason
     */
    public static void pushRunData() {
        OneoffTask task = new OneoffTask.Builder()
                .setService(SyncService.class)
                .setTag(TaskConstants.PUSH_WORKOUT_DATA)
                /*
                    Mandatory setter for creating a one-off task.
                    You specify the earliest point in time in the future from which your task might start executing,
                    as well as the latest point in time in the future at which your task must have executed.
                 */
                .setExecutionWindow(0L, 300L)
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                .setPersisted(true)
                .build();

        GcmNetworkManager mGcmNetworkManager = GcmNetworkManager.getInstance(MainApplication.getContext().getApplicationContext());
        mGcmNetworkManager.schedule(task);
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

    public static void syncMessageCenterData(Context context) {
        SyncTaskManger.fetchMessageData(context);
    }

    public static boolean fetchMessage() {
        MessageDao messageDao = MainApplication.getInstance().getDbWrapper().getDaoSession().getMessageDao();
        long messageCount = messageDao.queryBuilder().count();
        String url = Urls.getMessageUrl();
        return fetchMessages(url, messageCount);
    }

    private static boolean fetchMessages(String url, long messageCount) {

        try {
            MessageList messageList = NetworkDataProvider.doGetCall(url, MessageList.class);
            if (messageCount >= messageList.getTotalMessageCount()) {
                Logger.d(TAG, "update success" + messageList + " : " + messageList.getTotalMessageCount());
                EventBus.getDefault().post(new DBEvent.MessageDataUpdated());
                return true;
            } else {
                MessageDao messageDao = MainApplication.getInstance().getDbWrapper().getDaoSession().getMessageDao();
                messageDao.insertOrReplaceInTx(messageList);
                SharedPrefsManager.getInstance().setBoolean(Constants.PREF_UNREAD_MESSAGE, true);
                Logger.d(TAG, "Message fetch success" + messageList.toString());
                if (!TextUtils.isEmpty(messageList.getNextUrl())) {
                    return fetchMessages(messageList.getNextUrl(), messageCount);
                }
            }
        } catch (NetworkException e) {
            e.printStackTrace();
            Logger.d(TAG, "NetworkException" + e.getMessageFromServer() + e.getMessage());
            return false;
        }
        EventBus.getDefault().post(new DBEvent.MessageDataUpdated());

        return true;
    }

    public static void syncLeaderBoardData(Context context) {
        PeriodicTask task = new PeriodicTask.Builder()
                .setService(SyncService.class)
                .setTag(SYNC_LEADERBOARD_DATA)
                .setPeriod(7200) // in secs , i.e. every two hours
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                .setPersisted(true)
                .setFlex(2400)
                .build();

        GcmNetworkManager mGcmNetworkManager = GcmNetworkManager.getInstance(context);
        mGcmNetworkManager.schedule(task);

    }

    public static void scheduleCauseDataSync(Context context) {
        PeriodicTask task = new PeriodicTask.Builder()
                .setService(SyncService.class)
                .setTag(SYNC_CAUSE_DATA)
                .setPeriod(7200L) // in secs , i.e. every two hours
                .setPersisted(true)
                .setFlex(2400)
                .build();

        GcmNetworkManager mGcmNetworkManager = GcmNetworkManager.getInstance(context);
        mGcmNetworkManager.schedule(task);
    }

    public static void scheduleUserDataSync(Context context) {
        Logger.d(TAG, "scheduleUserDataSync");
        PeriodicTask task = new PeriodicTask.Builder()
                .setService(SyncService.class)
                .setTag(UPLOAD_USER_DATA)
                .setPeriod(21600L) // in secs, i.e. sync every 6 hours
                .setPersisted(true)
                .setFlex(2400)
                .build();

        GcmNetworkManager mGcmNetworkManager = GcmNetworkManager.getInstance(context);
        mGcmNetworkManager.schedule(task);
    }

    public static void scheduleServerTimeSync(Context context) {
        Logger.d(TAG, "scheduleServerTimeSync");
        PeriodicTask task = new PeriodicTask.Builder()
                .setService(SyncService.class)
                .setTag(SYNC_SERVER_TIME)
                .setPeriod(21600L) // in secs, i.e. sync every 6 hours
                .setPersisted(true)
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                .setFlex(2400)
                .build();

        GcmNetworkManager mGcmNetworkManager = GcmNetworkManager.getInstance(context);
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
                    details.setFirstName(prefsManager.getString(PREF_USER_NAME));
                    details.setPhoneNumber(user.getMobileNO());
                    details.setBirthday(user.getBirthday());
                    details.setEmail(prefsManager.getString(PREF_USER_EMAIL));
                    details.setAuthToken(prefsManager.getString(PREF_AUTH_TOKEN));
                    details.setGenderUser(user.getGender());
                    details.setSignUp(prefsManager.getBoolean(Constants.PREF_IS_SIGN_UP_USER));
                    details.setTeamId(LeaderBoardDataStore.getInstance().getMyTeamId());
                    details.setSocialThumb(user.getProfileImageUrl());

                    MainApplication.getInstance().setUserDetails(details);
                }
            }
        }
    }

    public static void syncCampaignData(Context context) {
        SyncTaskManger.startCampaign(context);
    }


    public static void fetchCampaign(Context context) {

        CampaignList.Campaign campaign = null;
        CampaignList.Campaign oldCampaign = SharedPrefsManager.getInstance().getObject(Constants.PREF_CAMPAIGN_DATA, CampaignList.Campaign.class);
        try {
            CampaignList campaignList = NetworkDataProvider.doGetCall(Urls.getCampaignUrl(), CampaignList.class);
            if (campaignList.getTotalCount() > 0) {
                SharedPrefsManager.getInstance().setObject(Constants.PREF_CAMPAIGN_DATA, campaignList.getCampaignList().get(0));
                campaign = campaignList.getCampaignList().get(0);
                Picasso.with(context).load(campaign.getImageUrl()).fetch();
                if (oldCampaign != null && oldCampaign.getId() != campaign.getId()) {
                    SharedPrefsManager.getInstance().setBoolean(Constants.PREF_CAMPAIGN_SHOWN_ONCE, false);
                }
            } else {
                SharedPrefsManager.getInstance().removeKey(Constants.PREF_CAMPAIGN_DATA);
            }

        } catch (NetworkException e) {
            e.printStackTrace();
            Logger.d(TAG, "NetworkException" + e.getMessageFromServer() + e.getMessage());
            campaign = SharedPrefsManager.getInstance().getObject(Constants.PREF_CAMPAIGN_DATA, CampaignList.Campaign.class);
        }
        if (campaign != null) {
            EventBus.getDefault().post(new DBEvent.CampaignDataUpdated(campaign));
        }
    }

}
