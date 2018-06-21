package com.sharesmile.share.core.sync;

import android.content.AsyncTaskLoader;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.sharesmile.share.AchievedBadge;
import com.sharesmile.share.AchievedBadgeDao;
import com.sharesmile.share.BadgeDao;
import com.sharesmile.share.core.cause.CauseDataStore;
import com.sharesmile.share.core.event.UpdateEvent;
import com.sharesmile.share.leaderboard.global.GlobalLeaderBoardDataUpdated;
import com.sharesmile.share.leaderboard.impactleague.event.LeagueBoardDataUpdated;
import com.sharesmile.share.leaderboard.impactleague.event.TeamLeaderBoardDataFetched;
import com.sharesmile.share.leaderboard.LeaderBoardDataStore;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.MessageDao;
import com.sharesmile.share.Workout;
import com.sharesmile.share.WorkoutDao;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.config.ClientConfig;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.base.ExpoBackoffTask;
import com.sharesmile.share.profile.badges.model.Badge;
import com.sharesmile.share.profile.badges.model.BadgeParent;
import com.sharesmile.share.profile.model.CharityOverview;
import com.sharesmile.share.tracking.models.WorkoutBatch;
import com.sharesmile.share.tracking.models.WorkoutBatchLocationData;
import com.sharesmile.share.tracking.models.WorkoutBatchLocationDataResponse;
import com.sharesmile.share.tracking.models.WorkoutData;
import com.sharesmile.share.tracking.models.WorkoutDataImpl;
import com.sharesmile.share.network.NetworkDataProvider;
import com.sharesmile.share.network.NetworkException;
import com.sharesmile.share.network.NetworkUtils;
import com.sharesmile.share.core.cause.model.CauseList;
import com.sharesmile.share.tracking.workout.data.model.FraudData;
import com.sharesmile.share.home.howitworks.model.HowItWorksResponse;
import com.sharesmile.share.leaderboard.global.model.LeaderBoardList;
import com.sharesmile.share.tracking.workout.data.model.Run;
import com.sharesmile.share.tracking.workout.data.model.RunList;
import com.sharesmile.share.login.UserDetails;
import com.sharesmile.share.helpcenter.model.UserFeedback;
import com.sharesmile.share.utils.DateUtil;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.timekeeping.ServerTimeKeeper;
import com.sharesmile.share.core.SharedPrefsManager;
import com.sharesmile.share.core.config.Urls;
import com.sharesmile.share.utils.Utils;
import com.squareup.okhttp.Response;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import Models.CampaignList;
import Models.FaqList;
import Models.FeedLatestArticleResponse;
import Models.LeagueBoard;
import Models.MessageList;
import Models.TeamLeaderBoard;

import static com.sharesmile.share.core.sync.TaskConstants.SYNC_BADGE_DATA;
import static com.sharesmile.share.core.sync.TaskConstants.SYNC_CHARITY_OVERVIEW;
import static com.sharesmile.share.core.sync.TaskConstants.UPLOAD_ACHIEVEMENT;
import static com.sharesmile.share.core.sync.TaskConstants.UPLOAD_STREAK;
import static com.sharesmile.share.core.sync.TaskConstants.UPLOAD_PENDING_WORKOUT;
import static com.sharesmile.share.leaderboard.LeaderBoardDataStore.ALL_INTERVALS;
import static com.sharesmile.share.core.Constants.PREF_LAST_TIME_FEED_WAS_SEEN;
import static com.sharesmile.share.core.sync.TaskConstants.PUSH_FRAUD_DATA;
import static com.sharesmile.share.core.sync.TaskConstants.PUSH_USER_FEEDBACK;
import static com.sharesmile.share.core.sync.TaskConstants.SYNC_DATA;
import static com.sharesmile.share.core.sync.TaskConstants.UPLOAD_USER_DATA;

/**
 * Created by Shine on 15/05/16.
 */
public class SyncService extends GcmTaskService {
    private static final String TAG = SyncService.class.getSimpleName();

    @Override
    public int onRunTask(TaskParams taskParams) {
        Logger.d(TAG, "runtask started: " + taskParams.getTag());
        try {
            switch (taskParams.getTag()) {
                case SYNC_CHARITY_OVERVIEW:
                    return getCharityOverviewData();
                case UPLOAD_USER_DATA:
                    return uploadUserData();
                case UPLOAD_STREAK:
                    return uploadStreak();
                case UPLOAD_ACHIEVEMENT:
                    return uploadAchievement();
                case SYNC_DATA:
                    return syncData(getAssets());
                case PUSH_FRAUD_DATA:
                    Bundle fraudExtras = taskParams.getExtras();
                    String fraudDataString = fraudExtras.getString(TaskConstants.FRAUD_DATA_JSON);
                    return pushFraudData(fraudDataString);
                case PUSH_USER_FEEDBACK:
                    Bundle feedbackExtras = taskParams.getExtras();
                    String feedbackString = feedbackExtras.getString(TaskConstants.FEEDBACK_DATA_JSON);
                    return pushUserFeedback(feedbackString);
                case UPLOAD_PENDING_WORKOUT:
                    return uploadPendingWorkoutsData();

                case SYNC_BADGE_DATA:
                    return syncBadgeData(getAssets());
                default:
                    return GcmNetworkManager.RESULT_SUCCESS;
            }
        } catch (Throwable th) {
            String message = "Exception while performing sync task: " + taskParams.getTag()
                    + ", exception message: " + th.getMessage();
            th.printStackTrace();
            Logger.e(TAG, message);
            Crashlytics.log(message);
            Crashlytics.logException(th);
            AnalyticsEvent.create(Event.ON_EXCEPTION_IN_SYNC_TASK)
                    .put("task", taskParams.getTag())
                    .put("exception_message", th.getMessage())
                    .buildAndDispatch();
            return GcmNetworkManager.RESULT_FAILURE;
        }
    }

    private static int syncBadgeData(AssetManager assets) {
        try {
            StringBuilder buf = new StringBuilder();
            InputStream json = assets.open("badges.txt");
            BufferedReader in =
                    new BufferedReader(new InputStreamReader(json, "UTF-8"));
            String str;

            while ((str = in.readLine()) != null) {
                buf.append(str);
            }

            in.close();
            JSONArray jsonArray = new JSONArray(buf.toString());
            Gson gson = new Gson();
            Type listType = new TypeToken<List<BadgeParent>>() {
            }.getType();
            List<BadgeParent> badgeParents = gson.fromJson(buf.toString(), listType);
            BadgeDao badgeDao = MainApplication.getInstance().getDbWrapper().getBadgeDao();
            for (BadgeParent badgeParent : badgeParents) {
                for (Badge badge : badgeParent.getBadges()) {
                    com.sharesmile.share.Badge badgeDb = new com.sharesmile.share.Badge();
                    badgeDb.setBadgeId(badge.getBadgeId());
                    badgeDb.setName(badge.getName());
                    badgeDb.setType(badge.getType());
                    badgeDb.setCategory(badge.getCategory());
                    badgeDb.setNoOfStars(badge.getNoOfStars());
                    badgeDb.setImageUrl(badge.getImageUrl());
                    badgeDb.setDescription1(badge.getDescription1());
                    badgeDb.setDescription2(badge.getDescription2());
                    badgeDb.setDescription3(badge.getDescription3());
                    badgeDb.setBadgeParameter(badge.getBadgeParameter());
                    badgeDb.setBadgeParameterCheck(badge.getBadgeParameterCheck());
                    List<com.sharesmile.share.Badge> badges = badgeDao.queryBuilder().where(BadgeDao.Properties.BadgeId.eq(badge.getBadgeId())).list();
                    if (badges.size() > 0) {
                        badgeDb.setId(badges.get(0).getId());
                        badgeDao.update(badgeDb);
                    } else {
                        badgeDao.insertOrReplace(badgeDb);
                    }
                }
            }
            EventBus.getDefault().post(new UpdateEvent.BadgeUpdated());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return GcmNetworkManager.RESULT_SUCCESS;
    }

    @Override
    public void onInitializeTasks() {
        Logger.d(TAG, "onInitializeTasks");
        super.onInitializeTasks();
        MainApplication.getInstance().startSyncTasks();
    }

    public static int syncData(AssetManager assets) {
        Logger.d(TAG, "syncData");
        ClientConfig.sync();
        syncServerTime();
        uploadUserData();
        syncGlobalLeaderBoardData();
        syncLeagueBoardData();
        //TODO : temp getting data from assets file
        syncBadgeData(assets);
        updateCauseData();
        updateFaqs();
        uploadPendingWorkoutsData();
        syncWorkoutData();
        // Rolling back to old feed
//        syncFeed();
        syncHowItWorksContent();
        fetchMessage();
        fetchCampaign();
        getCharityOverviewData();
        uploadStreak();
        uploadAchievement();

        // Returning success as result does not matter
        return GcmNetworkManager.RESULT_SUCCESS;
    }

    public static void fetchCampaign() {

        CampaignList.Campaign campaign = null;
        CampaignList.Campaign oldCampaign = SharedPrefsManager.getInstance().getObject(Constants.PREF_CAMPAIGN_DATA, CampaignList.Campaign.class);
        try {
            CampaignList campaignList = NetworkDataProvider.doGetCall(Urls.getCampaignUrl(), CampaignList.class);
            if (campaignList.getTotalCount() > 0) {
                SharedPrefsManager.getInstance().setObject(Constants.PREF_CAMPAIGN_DATA, campaignList.getCampaignList().get(0));
                campaign = campaignList.getCampaignList().get(0);
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
            EventBus.getDefault().post(new UpdateEvent.CampaignDataUpdated(campaign));
        }
    }

    public static boolean fetchMessage() {
        Logger.d(TAG, "fetchMessage");
        MessageDao messageDao = MainApplication.getInstance().getDbWrapper().getDaoSession().getMessageDao();
        long messageCount = messageDao.queryBuilder().count();
        String url = Urls.getMessageUrl();
        return fetchMessages(url, messageCount);
    }

    private static boolean fetchMessages(String url, long prevMessagesCount) {

        try {
            MessageList messageList = NetworkDataProvider.doGetCall(url, MessageList.class);
            if (messageList == null) {
                return false;
            }
            MessageDao messageDao = MainApplication.getInstance().getDbWrapper().getDaoSession().getMessageDao();
            messageDao.insertOrReplaceInTx(messageList);
            if (prevMessagesCount < messageList.getTotalMessageCount()) {
                SharedPrefsManager.getInstance().setBoolean(Constants.PREF_UNREAD_MESSAGE, true);
            }
            Logger.d(TAG, "Feed Messages fetched successfully");
            if (!TextUtils.isEmpty(messageList.getNextUrl())) {
                return fetchMessages(messageList.getNextUrl(), prevMessagesCount);
            } else {
                EventBus.getDefault().post(new UpdateEvent.MessageDataUpdated());
                return true;
            }
        } catch (NetworkException e) {
            e.printStackTrace();
            Logger.d(TAG, "NetworkException" + e.getMessageFromServer() + e.getMessage());
            return false;
        }
    }

    public static boolean syncHowItWorksContent() {
        Logger.d(TAG, "syncHowItWorksContent");
        try {
            HowItWorksResponse response
                    = NetworkDataProvider.doGetCall(Urls.getHowItWorksContentUrl(), HowItWorksResponse.class);
            if (response == null) {
                return false;
            }

            SharedPrefsManager.getInstance().setObject(Constants.PREF_HOW_IT_WORKS_CONTENT, response);

            return true;
        } catch (NetworkException e) {
            e.printStackTrace();
            Logger.d(TAG, "NetworkException while fetching content for how_it_works"
                    + e.getMessageFromServer() + e.getMessage());
            return false;
        }
    }

    public static boolean syncFeed() {
        Logger.d(TAG, "syncFeed");
        try {
            FeedLatestArticleResponse response
                    = NetworkDataProvider.doGetCall(Urls.getFeedLatestArticleUrl(), FeedLatestArticleResponse.class);
            if (response == null) {
                return false;
            }
            long latestArtileCreationTsMillis = response.getCreationEpochSecs() * 1000;

            long feedLastSeenTs = SharedPrefsManager.getInstance().getLong(PREF_LAST_TIME_FEED_WAS_SEEN);

            if (latestArtileCreationTsMillis > feedLastSeenTs) {
                Logger.d(TAG, "New feed article available");
                SharedPrefsManager.getInstance().setBoolean(Constants.PREF_NEW_FEED_ARTICLE_AVAILABLE, true);
            }
            return true;
        } catch (NetworkException e) {
            e.printStackTrace();
            Logger.d(TAG, "NetworkException while fetching latest article's creation time"
                    + e.getMessageFromServer() + e.getMessage());
            return false;
        }
    }

    public static int syncServerTime() {
        // Force sync servertime but do not retry on failure
        if (NetworkUtils.isNetworkConnected(MainApplication.getContext())) {
            ServerTimeKeeper.getInstance().forceSyncTimerWithServerTime(false);
        }
        return GcmNetworkManager.RESULT_SUCCESS;
    }

    public static int syncGlobalLeaderBoardData() {

        Logger.d(TAG, "syncGlobalLeaderBoardData");
        if (!MainApplication.isLogin()) {
            return ExpoBackoffTask.RESULT_FAILURE;
        }

        int result = ExpoBackoffTask.RESULT_SUCCESS;

        for (String interval : ALL_INTERVALS) {
            try {
                Logger.d(TAG, "Will sync GlobalLeaderBoard, interval: " + interval);
                LeaderBoardList list =
                        NetworkDataProvider.doGetCall(Urls.getLeaderboardUrl(interval), LeaderBoardList.class);
                // Store this in LeaderBoardDataStore
                LeaderBoardDataStore.getInstance().setGlobalLeaderBoard(interval, list);
                // Notify LeaderBoardFragment about it
                EventBus.getDefault().post(new GlobalLeaderBoardDataUpdated(true, interval));
            } catch (NetworkException e) {
                Logger.e(TAG, "Exception occurred while syncing GlobalLeaderBoardData data ("
                        + interval + ") from network: " + e);
                e.printStackTrace();
                result = ExpoBackoffTask.RESULT_RESCHEDULE;
            }
        }
        return result;
    }

    public static int syncLeagueBoardData() {
        Logger.d(TAG, "syncLeagueBoardData");
        if (!MainApplication.isLogin()) {
            return ExpoBackoffTask.RESULT_FAILURE;
        }

        int result = ExpoBackoffTask.RESULT_SUCCESS;

        if (LeaderBoardDataStore.getInstance().toSyncLeaugeData()) {
            // Go for sync only when an active league is present and is still visible to team members
            try {
                Logger.d(TAG, "Will sync LeagueBoard");
                LeagueBoard leagueBoard = NetworkDataProvider.doGetCall(Urls.getLeagueBoardUrl(), LeagueBoard.class);
                // Store this in LeaderBoardDataStore
                LeaderBoardDataStore.getInstance().setLeagueBoardData(leagueBoard);
                // Notify LeaderBoardFragment about it
                EventBus.getDefault().post(new LeagueBoardDataUpdated(true));
            } catch (NetworkException ne) {
                String log = "NetworkException while fetching my (user_id = "
                        + MainApplication.getInstance().getUserDetails().getUserId()
                        + ", and team_id = " + LeaderBoardDataStore.getInstance().getMyTeamId()
                        + ") LeaugeBoardData: " + ne;
                Logger.e(TAG, log);
                ne.printStackTrace();
                Crashlytics.log(log);
                Crashlytics.logException(ne);
                return ExpoBackoffTask.RESULT_RESCHEDULE;
            }

            try {
                Logger.d(TAG, "Will sync MyTeamLeaderBoard");
                int leagueTeamId = LeaderBoardDataStore.getInstance().getMyTeamId();
                if (leagueTeamId > 0) {
                    Map<String, String> queryParams = new HashMap<>();
                    queryParams.put("team_id", String.valueOf(leagueTeamId));
                    TeamLeaderBoard myTeamLeaderBoard = NetworkDataProvider
                            .doGetCall(Urls.getTeamLeaderBoardUrl(), queryParams, TeamLeaderBoard.class);
                    // Store this in LeaderBoardDataStore
                    LeaderBoardDataStore.getInstance().setMyTeamLeaderBoardData(myTeamLeaderBoard);
                    // Notify LeaderBoardFragment about it
                    EventBus.getDefault().post(new TeamLeaderBoardDataFetched(leagueTeamId, true, myTeamLeaderBoard));
                }
            } catch (NetworkException ne) {
                String log = "NetworkException while fetching my (user_id = "
                        + MainApplication.getInstance().getUserDetails().getUserId()
                        + ", and team_id = " + LeaderBoardDataStore.getInstance().getMyTeamId()
                        + ") TeamLeaderBoardData: " + ne;
                Logger.e(TAG, log);
                ne.printStackTrace();
                Crashlytics.log(log);
                Crashlytics.logException(ne);
                return ExpoBackoffTask.RESULT_RESCHEDULE;
            }

        } else {
            Logger.d(TAG, "Will NOT sync LeagueBoard");
        }

        return result;

    }

    public static int updateCauseData() {
        Logger.d(TAG, "updateCauseData");
        try {
            CauseList causeList = NetworkDataProvider.doGetCall(Urls.getCauseListUrl(), CauseList.class);
            CauseDataStore.getInstance().updateCauseList(causeList);
            return GcmNetworkManager.RESULT_SUCCESS;
        } catch (NetworkException e) {
            Logger.e(TAG, "Exception occurred while fetching updated cause list from network");
            e.printStackTrace();
            return GcmNetworkManager.RESULT_RESCHEDULE;
        }
    }

    public static int updateFaqs() {
        Logger.d(TAG, "updateFaqs");
        try {
            FaqList faqList = NetworkDataProvider.doGetCall(Urls.getFaqUrl(), FaqList.class);
            MainApplication.getInstance().updateFaqList(faqList);
            EventBus.getDefault().post(new UpdateEvent.FaqsUpdated(true));
            return GcmNetworkManager.RESULT_SUCCESS;
        } catch (NetworkException e) {
            Logger.e(TAG, "Exception occurred while fetching updated Faqs list from network: " + e.getMessage());
            e.printStackTrace();
            EventBus.getDefault().post(new UpdateEvent.FaqsUpdated(false));
            return GcmNetworkManager.RESULT_RESCHEDULE;
        } catch (Exception ex) {
            Logger.e(TAG, "Exception occurred while fetching updated Faqs list: " + ex.getMessage());
            ex.printStackTrace();
            EventBus.getDefault().post(new UpdateEvent.FaqsUpdated(false));
            return GcmNetworkManager.RESULT_FAILURE;
        }
    }


    /**
     * Constructs the sync URL using clientVersion and then fetches all runs with version above clientVersion
     * and then insert or update all of those runs in DB
     *
     * @return
     */
    private static int syncWorkoutData() {
        synchronized (SyncService.class) {
            if (!MainApplication.isLogin()) {
                Logger.d(TAG, "User not logged in, cannot sync data to server");
                return GcmNetworkManager.RESULT_FAILURE;
            }
            WorkoutDao mWorkoutDao = MainApplication.getInstance().getDbWrapper().getWorkoutDao();
            long clientVersion = SharedPrefsManager.getInstance()
                    .getLong(Constants.PREF_WORKOUT_DATA_SYNC_VERSION);
            boolean isWorkoutDataUpToDate = SharedPrefsManager.getInstance()
                    .getBoolean(Constants.PREF_IS_WORKOUT_DATA_UP_TO_DATE_IN_DB, false);
            if (isWorkoutDataUpToDate && clientVersion > 0) {
                String syncUrl;
                syncUrl = Urls.getSyncRunUrl(clientVersion);
                Logger.d(TAG, "Starting sync with client_version: " + clientVersion);
                syncWorkoutTimeStamp = 0;
                return syncWorkoutData(syncUrl, mWorkoutDao);
            } else {
                // Need to force refresh Workout Data
                Logger.e(TAG, "Must fetch historical runs before");
                SyncHelper.forceRefreshEntireWorkoutHistory();
                return GcmNetworkManager.RESULT_FAILURE;
            }
        }
    }

    private static long syncWorkoutTimeStamp;

    private static int syncWorkoutData(String syncUrl, WorkoutDao mWorkoutDao) {
        if (!NetworkUtils.isNetworkConnected(MainApplication.getContext())) {
            // If internet not available then silently exit
            Logger.d(TAG, "Internet not available while syncing runs with URL: " + syncUrl);
            return GcmNetworkManager.RESULT_RESCHEDULE;
        }
        try {
            Response response = NetworkDataProvider.getResponseForGetCall(syncUrl);
            if (syncWorkoutTimeStamp == 0) {
                if (response.headers().getDate("Date") != null) {
                    syncWorkoutTimeStamp = response.headers().getDate("Date").getTime();
                } else {
                    syncWorkoutTimeStamp = DateUtil.getServerTimeInMillis();
                }
            }
            RunList runList = NetworkUtils.handleResponse(response, RunList.class);
            Gson gson = new Gson();
            Logger.d(TAG, "Syncing these runs in DB " + gson.toJson(runList));

            Iterator<Workout> iterator = runList.iterator();
            while (iterator.hasNext()) {
                Workout workout = iterator.next();
                if (TextUtils.isEmpty(workout.getWorkoutId())) {
                    // This run was created on backend, Generating new client_run_id and scheduling the run for sync
                    workout.setWorkoutId(UUID.randomUUID().toString());
                    workout.setIs_sync(false);
                } else {
                    // Existing run, lets take the appropriate value for setShouldSyncLocationData
                    // from the storedWorkout
                    Workout storedWorkout = mWorkoutDao.queryBuilder()
                            .where(WorkoutDao.Properties.WorkoutId.eq(workout.getWorkoutId()))
                            .unique();
                    if (storedWorkout != null) {
                        workout.setShouldSyncLocationData(storedWorkout.getShouldSyncLocationData());
                    }
                }
                mWorkoutDao.insertOrReplace(workout);
            }

            // Update User's track record
            Utils.updateTrackRecordFromDb();

            if (!TextUtils.isEmpty(runList.getNextUrl())) {
                // Recursive call to fetch the runs of next page
                return syncWorkoutData(runList.getNextUrl(), mWorkoutDao);
            } else {
                Logger.d(TAG, "syncWorkoutData, Setting SyncedTimeStampMillis as: " + syncWorkoutTimeStamp);
                SharedPrefsManager.getInstance().setLong(Constants.PREF_WORKOUT_DATA_SYNC_VERSION,
                        (syncWorkoutTimeStamp / 1000));
                return GcmNetworkManager.RESULT_SUCCESS;
            }
        } catch (NetworkException e) {
            e.printStackTrace();
            Logger.d(TAG, "NetworkException while syncing runs with URL: " + syncUrl + ", Exception: " + e);
            return GcmNetworkManager.RESULT_RESCHEDULE;
        }
    }

    public static int pushUserFeedback(String feedbackString) {
        Logger.d(TAG, "pushUserFeedback with: " + feedbackString);
        if (TextUtils.isEmpty(feedbackString)) {
            Logger.d(TAG, "Can't push FeedbackString in TaskParams is empty");
            return GcmNetworkManager.RESULT_FAILURE;
        }
        try {
            NetworkDataProvider.doPostCall(Urls.getFeedBackUrl(), feedbackString, UserFeedback.class);
            Logger.d(TAG, "Successfully pushed feedback");
            return GcmNetworkManager.RESULT_SUCCESS;
        } catch (NetworkException ne) {
            ne.printStackTrace();
            Logger.d(TAG, "NetworkException: " + ne);
            String log = "Couldn't post user feedback to URL: " + Urls.getFeedBackUrl()
                    + ", Feedback: " + feedbackString;
            Logger.e(TAG, log);
            Crashlytics.log(log);
            Crashlytics.log("Push user feedback (id=" + MainApplication.getInstance().getUserDetails().getUserId()
                    + ") networkException, messageFromServer: " + ne);
            Crashlytics.logException(ne);
            return GcmNetworkManager.RESULT_RESCHEDULE;
        } catch (Exception e) {
            e.printStackTrace();
            String log = "Couldn't post user feedback to URL: " + Urls.getFeedBackUrl()
                    + ", Feedback: " + feedbackString;
            Logger.e(TAG, log);
            Logger.e(TAG, "Exception: " + e.getMessage());
            Crashlytics.log(log);
            Crashlytics.logException(e);
            return GcmNetworkManager.RESULT_FAILURE;
        }
    }

    private int pushFraudData(String fraudDataString) {
        if (!MainApplication.isLogin()) {
            return GcmNetworkManager.RESULT_FAILURE;
        }
        if (TextUtils.isEmpty(fraudDataString)) {
            Logger.d(TAG, "Can't push FraudDtaString in TaskParams is empty");
            return GcmNetworkManager.RESULT_FAILURE;
        }
        Logger.d(TAG, "Will pushFraudData: " + fraudDataString);
        try {
            Gson gson = new Gson();
            FraudData fraudData = gson.fromJson(fraudDataString, FraudData.class);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("user_id", fraudData.getUserId());
            jsonObject.put("client_run_id", fraudData.getClientRunId());
            jsonObject.put("cause_id", fraudData.getCauseId());
            jsonObject.put("usain_bolt_count", fraudData.getUsainBoltCount());
            // Send team_id only when it is greater than 0
            if (fraudData.getTeamId() > 0) {
                jsonObject.put("team_id", fraudData.getTeamId());
            }
            jsonObject.put("timestamp", fraudData.getTimeStamp());
            jsonObject.put("mock_location_used", fraudData.isMockLocationUsed());

            NetworkDataProvider.doPostCall(Urls.getFraudstersUrl(), jsonObject, FraudData.class);

            return GcmNetworkManager.RESULT_SUCCESS;

        } catch (JSONException e) {
            e.printStackTrace();
            Logger.d(TAG, "JSONException: " + e.getMessage());
            Crashlytics.logException(e);
            return GcmNetworkManager.RESULT_RESCHEDULE;
        } catch (NetworkException ne) {
            ne.printStackTrace();
            Logger.d(TAG, "NetworkException: " + ne);
            String log = "Couldn't post fraudData to URL: " + Urls.getFraudstersUrl() + ", FraudData: " + fraudDataString;
            Logger.e(TAG, log);
            Crashlytics.log(log);
            Crashlytics.log("Push fraud data networkException, messageFromServer: " + ne
                    + "\n and FraudData: " + fraudDataString);
            Crashlytics.logException(ne);
            return GcmNetworkManager.RESULT_RESCHEDULE;
        }
    }


    private static int uploadUserData() {
        if (!MainApplication.isLogin()) {
            // Can't sync a non logged in User
            return GcmNetworkManager.RESULT_FAILURE;
        }
        int user_id = MainApplication.getInstance().getUserID();
        Logger.d(TAG, "uploadUserData for userId: " + user_id);
        try {

            UserDetails prev = MainApplication.getInstance().getUserDetails();
            if (prev == null) {
                // Ideally this condition should never happen
                Logger.d(TAG, "Can't UPLOAD, MemberDetails not present");
                return GcmNetworkManager.RESULT_FAILURE;
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("first_name", prev.getFirstName());
            jsonObject.put("last_name", prev.getLastName());
            jsonObject.put("gender_user", prev.getGenderUser());
            jsonObject.put("phone_number", prev.getPhoneNumber());
            jsonObject.put("body_weight", Utils.formatWithOneDecimal(prev.getBodyWeight()));
            jsonObject.put("body_height", prev.getBodyHeight());
            jsonObject.put("body_height_unit", prev.getBodyHeightUnit());
            jsonObject.put("birthday", prev.getBirthday());
            jsonObject.put("profile_picture", prev.getProfilePicture());
            jsonObject.put("user_id", user_id);
            jsonObject.put("goal", prev.getStreakGoalID());
            jsonObject.put("reminder_time", Utils.getReminderTime().getTimeInMillis());

            Logger.d(TAG, "Syncing user with data " + jsonObject.toString());

            Gson gson = new Gson();
            UserDetails response = NetworkDataProvider.doPutCall(Urls.getUserUrl(user_id), jsonObject,
                    UserDetails.class);
            Logger.d(TAG, "Response for getUser:" + gson.toJson(response));

            MainApplication.getInstance().setUserDetails(response);

            return GcmNetworkManager.RESULT_SUCCESS;

        } catch (NetworkException e) {
            e.printStackTrace();
            Logger.d(TAG, "NetworkException" + e);
            return GcmNetworkManager.RESULT_RESCHEDULE;
        } catch (JSONException e) {
            e.printStackTrace();
            Logger.d(TAG, "NetworkException");
            return GcmNetworkManager.RESULT_FAILURE;
        }
    }

    private static int uploadStreak() {
        if (!MainApplication.isLogin()) {
            // Can't sync a non logged in User
            return GcmNetworkManager.RESULT_FAILURE;
        }
        int user_id = MainApplication.getInstance().getUserID();
        Logger.d(TAG, "uploadUserData for userId: " + user_id);
        try {

            UserDetails prev = MainApplication.getInstance().getUserDetails();
            if (prev == null) {
                // Ideally this condition should never happen
                Logger.d(TAG, "Can't UPLOAD, MemberDetails not present");
                return GcmNetworkManager.RESULT_FAILURE;
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("max_streak", prev.getStreakMaxCount());
            jsonObject.put("current_streak_date", prev.getStreakCurrentDate());
            jsonObject.put("streak_added", prev.isStreakAdded());
            jsonObject.put("streak_count", prev.getStreakCount());


            Logger.d(TAG, "Syncing user with data " + jsonObject.toString());

            Gson gson = new Gson();
            JSONObject response = NetworkDataProvider.doPutCall(Urls.getStreakUrl(), jsonObject,
                    JSONObject.class);
            Logger.d(TAG, "Response for put Streak:" + response);


            return GcmNetworkManager.RESULT_SUCCESS;

        } catch (NetworkException e) {
            e.printStackTrace();
            Logger.d(TAG, "NetworkException" + e);
            return GcmNetworkManager.RESULT_RESCHEDULE;
        } catch (JSONException e) {
            e.printStackTrace();
            Logger.d(TAG, "NetworkException");
            return GcmNetworkManager.RESULT_FAILURE;
        }
    }

    private static int uploadAchievement() {
        if (!MainApplication.isLogin()) {
            // Can't sync a non logged in User
            return GcmNetworkManager.RESULT_FAILURE;
        }
        int user_id = MainApplication.getInstance().getUserID();
        Logger.d(TAG, "uploadUserData for userId: " + user_id);
        try {

            AchievedBadgeDao achievedBadgeDao = MainApplication.getInstance().getDbWrapper().getAchievedBadgeDao();
            List<AchievedBadge> achievedBadges = achievedBadgeDao.queryBuilder().where(AchievedBadgeDao.Properties.IsSync.eq(false)).list();

            if (achievedBadges.size() > 0) {
                JSONArray jsonArray = new JSONArray();
                int size = achievedBadges.size();
                for (int i = 0; i < size; i++) {
                    AchievedBadge achievedBadge = achievedBadges.get(i);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("user_id", user_id);
                    jsonObject.put("cause_id", achievedBadge.getCauseId());
                    jsonObject.put("badge_id_in_progress", achievedBadge.getBadgeIdInProgress());
                    jsonObject.put("badge_id_achieved", achievedBadge.getBadgeIdAchieved());
                    jsonObject.put("category_id", achievedBadge.getCategory());
                    jsonObject.put("parameter_completed", achievedBadge.getParamDone());
                    jsonArray.put(jsonObject);
                }
                Gson gson = new Gson();
                JSONObject response = NetworkDataProvider.doPostCall(Urls.getAchievementUrl(), jsonArray.toString(),
                        JSONObject.class);

                Logger.d(TAG, "Response for getUser:" + response);
                if (response.getInt("code") == 200) {
                    for (int i = 0; i < size; i++) {
                        AchievedBadge achievedBadge = achievedBadges.get(i);
                        achievedBadge.setIsSync(true);
                        achievedBadgeDao.update(achievedBadge);
                    }
                }
            }
            return GcmNetworkManager.RESULT_SUCCESS;

        } catch (NetworkException e) {
            e.printStackTrace();
            Logger.d(TAG, "NetworkException" + e);
            return GcmNetworkManager.RESULT_RESCHEDULE;
        } catch (JSONException e) {
            e.printStackTrace();
            Logger.d(TAG, "NetworkException");
            return GcmNetworkManager.RESULT_FAILURE;
        }
    }


    public static void pushWorkoutDataWithBackoff() {
        Logger.d(TAG, "pushWorkoutDataWithBackoff");
        ExpoBackoffTask task = new ExpoBackoffTask(2000) {
            @Override
            public int performtask() {
                return uploadPendingWorkoutsData();
            }
        };
        task.run();
    }

    private static int uploadPendingWorkoutsData() {
        synchronized (SyncService.class) {
            if (!MainApplication.isLogin()) {
                Logger.d(TAG, "uploadPendingWorkoutData: User not logged in, did not sync data to server.");
                return ExpoBackoffTask.RESULT_FAILURE;
            }
            // Step: Upload all the Pending Workouts
            Logger.d(TAG, "uploadPendingWorkoutData");
            boolean isSuccess = true;
            WorkoutDao mWorkoutDao = MainApplication.getInstance().getDbWrapper().getWorkoutDao();
            List<Workout> mWorkoutList = mWorkoutDao.queryBuilder()
                    .where(WorkoutDao.Properties.Is_sync.eq(false))
                    .list();

            if (mWorkoutList != null && mWorkoutList.size() > 0) {
                for (Workout workout : mWorkoutList) {
                    boolean resultW = uploadWorkoutData(workout);
                    isSuccess = isSuccess && resultW;
                }
            }

            // Step: Upload all pending WorkoutLocationData
            Logger.d(TAG, "uploadPendingWorkoutData, Will upload locationData");
            List<Workout> pendingLocationWorkoutsList = mWorkoutDao.queryBuilder()
                    .where(
                            WorkoutDao.Properties.Is_sync.eq(true),
                            WorkoutDao.Properties.ShouldSyncLocationData.eq(true)
                    ).list();

            if (pendingLocationWorkoutsList == null || pendingLocationWorkoutsList.isEmpty()) {
                Logger.d(TAG, "uploadPendingWorkoutsData: Didn't find any pending WorkoutLocationData to be uploaded");
            } else {
                for (Workout workout : pendingLocationWorkoutsList) {
                    boolean resultL = uploadWorkoutLocationData(workout);
                    if (resultL) {
                        // If all batches of this workout are uploaded successfully, we update the boolean flag in DB
                        workout.setShouldSyncLocationData(false);
                        mWorkoutDao.insertOrReplace(workout);
                    }
                    isSuccess = isSuccess && resultL;
                }
            }

            EventBus.getDefault().post(new UpdateEvent.PendingWorkoutUploaded());
            SharedPrefsManager.getInstance().setBoolean(Constants.PREF_CHARITY_OVERVIEW_DATA_LOAD,true);
            getCharityOverviewData();
            return isSuccess ? ExpoBackoffTask.RESULT_SUCCESS : ExpoBackoffTask.RESULT_RESCHEDULE;
        }

    }

    private static boolean uploadWorkoutLocationData(Workout workout) {
        if (!NetworkUtils.isNetworkConnected(MainApplication.getContext())) {
            // If internet not available then silently exit
            return false;
        }

        Logger.d(TAG, "uploadWorkoutLocationData called for client_run_id: " + workout.getWorkoutId()
                + ", distance: " + workout.getDistance() + ", date: " + workout.getDate());
        long runId = workout.getId();
        String workoutId = workout.getWorkoutId();
        String prefKey = Utils.getWorkoutLocationDataPendingQueuePrefKey(workoutId);
        WorkoutData workoutData = SharedPrefsManager.getInstance().getObject(prefKey, WorkoutDataImpl.class);

        if (workoutData == null) {
            String failureMessage = "Can't find WorkoutData for " + workoutId;
            Logger.d(TAG, "uploadWorkoutLocationData, " + failureMessage);
            AnalyticsEvent.create(Event.ON_LOCATION_DATA_SYNC)
                    .put("upload_result", "failure")
                    .put("run_id", runId)
                    .put("client_run_id", workoutId)
                    .put("batch_num", -1)
                    .put("exception_message", failureMessage)
                    .buildAndDispatch();
            // Returning true so that the service doesn't retry upload of location data for this workout
            return true;
        }

        Gson gson = new Gson();
        Logger.d(TAG, "uploadWorkoutLocationData will upload locationData from: " + gson.toJson(workoutData));

        for (int i = 0; i < workoutData.getBatches().size(); i++) {
            WorkoutBatch batch = workoutData.getBatches().get(i);

            // Step: Construct WorkoutBatchLocationData object for this batch
            WorkoutBatchLocationData locationData = new WorkoutBatchLocationData();
            locationData.setBatchNum(i);
            locationData.setClientRunId(workoutId);
            locationData.setRunId(runId);
            locationData.setStartTimeEpoch(batch.getStartTimeStamp());
            locationData.setWasInVehicle(batch.wasInVehicle());
            locationData.setEndTimeEpoch(batch.getEndTimeStamp());
            locationData.setLocationArray(batch.getPoints());

            String locationDataString = "";
            try {
                // Step: POST WorkoutBatchLocationData on server
                locationDataString = gson.toJson(locationData);
                Logger.d(TAG, "Will POST data: " + locationDataString);
                WorkoutBatchLocationDataResponse response = NetworkDataProvider.doPostCall(Urls.getRunLocationsUrl(),
                        locationDataString, WorkoutBatchLocationDataResponse.class);
                // Successfully uploaded location data
                AnalyticsEvent.create(Event.ON_LOCATION_DATA_SYNC)
                        .put("upload_result", "success")
                        .put("run_id", runId)
                        .put("batch_num", i)
                        .put("client_run_id", workoutId)
                        .buildAndDispatch();

            } catch (NetworkException e) {
                e.printStackTrace();
                String message = "NetworkException while uploading WorkoutLocationData: " + e.getMessage()
                        + "\n WorkoutLocationData: " + locationDataString;
                Logger.d(TAG, message);
                Crashlytics.log(message);
                Crashlytics.logException(e);
                AnalyticsEvent.create(Event.ON_LOCATION_DATA_SYNC)
                        .put("upload_result", "failure")
                        .put("run_id", runId)
                        .put("batch_num", i)
                        .put("client_run_id", workoutId)
                        .put("exception_message", e.getMessage())
                        .put("message_from_server", e.getMessageFromServer())
                        .put("http_status", e.getHttpStatusCode())
                        .put("failure_type", e.getFailureType())
                        .buildAndDispatch();
                return false;
            } catch (Throwable ex) {
                String message = "Exception while uploading WorkoutLocationData: " + ex.getMessage()
                        + "\n WorkoutLocationData: " + locationDataString;
                Logger.e(TAG, message);
                ex.printStackTrace();
                Crashlytics.log(message);
                Crashlytics.logException(ex);
                AnalyticsEvent.create(Event.ON_LOCATION_DATA_SYNC)
                        .put("upload_result", "failure")
                        .put("run_id", runId)
                        .put("client_run_id", workoutId)
                        .put("batch_num", i)
                        .put("exception_message", ex.getMessage())
                        .buildAndDispatch();
                return false;
            }
        }

        // Delete the files in which location data of all the batches of this workout was stored
        for (int i = 0; i < workoutData.getBatches().size(); i++) {
            WorkoutBatch batch = workoutData.getBatches().get(i);
            String fileName = batch.getLocationDataFileName();
            if (!TextUtils.isEmpty(fileName) && MainApplication.getContext().deleteFile(fileName)) {
                Logger.d(TAG, batch.getLocationDataFileName() + " was successfully deleted");
            }
        }

        // Reaching here means all batches were uploaded successfully,
        // will remove the pref key on which locationData was stored
        SharedPrefsManager.getInstance().removeKey(prefKey);
        return true;
    }

    /**
     * Uploads WorkoutData and stores the run_id received in workoutDao object
     *
     * @param workout
     * @return true on success and false on failure
     */
    private static boolean uploadWorkoutData(Workout workout) {

        if (!NetworkUtils.isNetworkConnected(MainApplication.getContext()) || !MainApplication.isLogin()) {
            // If internet not available then silently exit || user not logged in
            return false;
        }
        Logger.d(TAG, "uploadWorkoutData called for client_run_id: " + workout.getWorkoutId()
                + ", distance: " + workout.getDistance() + ", date: " + workout.getDate());
        int user_id = SharedPrefsManager.getInstance().getInt(Constants.PREF_USER_ID);
        JSONObject jsonObject = new JSONObject();
        try {

            boolean isUpdateRequest = false;
            if (workout.getVersion() != null && workout.getVersion() > 0) {
                // Need to make a PUT request to update this already existing run on server
                jsonObject.put("run_id", workout.getId());
                isUpdateRequest = true;
            } else {
                // Version is not set, means this is a newly created run on client and needs to be POSTed on server
                isUpdateRequest = false;
            }

            Run response;
            if (isUpdateRequest) {
                // Need to make PUT request with just client_run_id in post data
                jsonObject.put("client_run_id", workout.getWorkoutId());
                String updateUrl = Urls.getUpdateRunUrl() + workout.getId() + "/";
                response = NetworkDataProvider.doPutCall(updateUrl, jsonObject, Run.class);
            } else {
                // Need to make POST request to create a new Run
                jsonObject.put("user_id", user_id);
                jsonObject.put("cause_run_title", workout.getCauseBrief());
                if (workout.getCauseId() != null && workout.getCauseId() > 0) {
                    jsonObject.put("cause_id", workout.getCauseId());
                }
                jsonObject.put("distance", workout.getDistance());

                if (workout.getBeginTimeStamp() != null) {
                    jsonObject.put("start_time", DateUtil.getDefaultFormattedDate(new Date(workout.getBeginTimeStamp())));
                    jsonObject.put("start_time_epoch", workout.getBeginTimeStamp());
                } else if (workout.getDate() != null) {
                    jsonObject.put("start_time", DateUtil.getDefaultFormattedDate(workout.getDate()));
                    jsonObject.put("start_time_epoch", workout.getDate().getTime());
                }

                if (workout.getEndTimeStamp() != null) {
                    jsonObject.put("end_time", DateUtil.getDefaultFormattedDate(new Date(workout.getEndTimeStamp())));
                    jsonObject.put("end_time_epoch", workout.getEndTimeStamp());
                }
                jsonObject.put("run_amount", workout.getRunAmount());
                jsonObject.put("run_duration", workout.getElapsedTime());
                jsonObject.put("run_duration_epoch", Utils.hhmmssToSecs(workout.getElapsedTime()));
                jsonObject.put("no_of_steps", workout.getSteps());
                jsonObject.put("avg_speed", workout.getAvgSpeed());
                jsonObject.put("client_run_id", workout.getWorkoutId());
                jsonObject.put("start_location_lat", workout.getStartPointLatitude());
                jsonObject.put("start_location_long", workout.getStartPointLongitude());
                jsonObject.put("end_location_lat", workout.getEndPointLatitude());
                jsonObject.put("end_location_long", workout.getEndPointLongitude());
                jsonObject.put("version", workout.getVersion());
                jsonObject.put("calories_burnt", workout.getCalories() == null ? 0 : workout.getCalories());
                if (workout.getTeamId() != null && workout.getTeamId() > 0) {
                    jsonObject.put("team_id", workout.getTeamId());
                }
                jsonObject.put("num_spikes", workout.getNumSpikes());
                jsonObject.put("num_updates", workout.getNumUpdates());
                jsonObject.put("app_version", workout.getAppVersion());
                jsonObject.put("os_version", workout.getOsVersion());
                jsonObject.put("device_id", workout.getDeviceId());
                jsonObject.put("device_name", workout.getDeviceName());
                jsonObject.put("estimated_steps", workout.getEstimatedSteps());
                jsonObject.put("estimated_distance", workout.getEstimatedDistance());
                jsonObject.put("estimated_calories", workout.getEstimatedCalories());
                jsonObject.put("google_fit_distance", workout.getGoogleFitDistance());
                jsonObject.put("google_fit_steps", workout.getGoogleFitStepCount());
                jsonObject.put("step_counter",
                        SharedPrefsManager.getInstance().getString(Constants.PREF_TYPE_STEP_COUNTER));
                jsonObject.put("usain_bolt_count", workout.getUsainBoltCount());
                jsonObject.put("is_flag", !workout.getIsValidRun());


                Logger.d(TAG, "Will upload run: " + jsonObject.toString());
                response = NetworkDataProvider.doPostCall(Urls.getRunUrl(), jsonObject, Run.class);
            }

            Logger.d(TAG, "POST run api call, Response: is_flag = " + response.isFlag());
            WorkoutDao mWorkoutDao = MainApplication.getInstance().getDbWrapper().getWorkoutDao();
            workout.setId(response.getId());
            workout.setIs_sync(true);
            workout.setIsValidRun(!response.isFlag());
            workout.setVersion(response.getVersion());
            mWorkoutDao.insertOrReplace(workout);
            AnalyticsEvent.create(Event.ON_RUN_SYNC)
                    .put("upload_result", "success")
                    .put("client_run_id", workout.getWorkoutId())
                    .buildAndDispatch();

            return true;

        } catch (NetworkException e) {
            e.printStackTrace();
            String message = "Run sync networkException: " + e + "\n Run: "
                    + jsonObject.toString();
            Logger.d(TAG, message);
            Crashlytics.log(message);
            Crashlytics.logException(e);
            AnalyticsEvent.create(Event.ON_RUN_SYNC)
                    .put("upload_result", "failure")
                    .put("client_run_id", workout.getWorkoutId())
                    .put("exception_message", e.getMessage())
                    .put("message_from_server", e.getMessageFromServer())
                    .put("http_status", e.getHttpStatusCode())
                    .put("failure_type", e.getFailureType())
                    .buildAndDispatch();
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            String message = "JSONException while syncing run: " + jsonObject.toString();
            Logger.d(TAG, message);
            Crashlytics.log(message);
            Crashlytics.logException(e);
            AnalyticsEvent.create(Event.ON_RUN_SYNC)
                    .put("upload_result", "failure")
                    .put("exception_message", e.getMessage())
                    .buildAndDispatch();
            return false;
        } catch (Exception ex) {
            String message = "Run sync Exception: " + ex.getMessage() + "\n Run: " + jsonObject.toString();
            Logger.e(TAG, message);
            ex.printStackTrace();
            Crashlytics.log(message);
            Crashlytics.logException(ex);
            AnalyticsEvent.create(Event.ON_RUN_SYNC)
                    .put("upload_result", "failure")
                    .put("exception_message", ex.getMessage())
                    .buildAndDispatch();
            return false;
        }
    }

    public static void forceRefreshEntireWorkoutHistoryWithBackoff() {
        ExpoBackoffTask task = new ExpoBackoffTask() {
            @Override
            public int performtask() {
                return forceRefreshEntireWorkoutHistory();
            }
        };
        task.run();
    }

    private static int forceRefreshEntireWorkoutHistory() {
        Logger.d(TAG, "forceRefreshEntireWorkoutHistory");
        String runUrl;
        runUrl = Urls.getFlaggedRunUrl(false);
        refreshAllTimeStamp = 0;
        return forceRefreshAllWorkoutData(runUrl);
    }

    private static long refreshAllTimeStamp;

    private static int forceRefreshAllWorkoutData(String runUrl) {

        RunList runList = null;
        Gson gson = new Gson();
        try {
            Response response = NetworkDataProvider.getResponseForGetCall(runUrl);
            if (refreshAllTimeStamp == 0) {
                if (response.headers().getDate("Date") != null) {
                    refreshAllTimeStamp = response.headers().getDate("Date").getTime();
                } else {
                    refreshAllTimeStamp = DateUtil.getServerTimeInMillis();
                }
            }
            Logger.d(TAG, "forceRefreshAllWorkoutData, fetched SyncedTimeStampMillis as: " + refreshAllTimeStamp);
            runList = NetworkUtils.handleResponse(response, RunList.class);

            Logger.d(TAG, "Updating these runs in DB " + gson.toJson(runList));
            WorkoutDao mWorkoutDao = MainApplication.getInstance().getDbWrapper().getWorkoutDao();

            Iterator<Workout> iterator = runList.iterator();
            while (iterator.hasNext()) {
                Workout workout = iterator.next();
                if (TextUtils.isEmpty(workout.getWorkoutId())) {
                    // Generating new client_run_id and scheduling the run for sync
                    workout.setWorkoutId(UUID.randomUUID().toString());
                    workout.setIs_sync(false);
                    if (workout.getVersion() == 0) {
                        // Setting dummy version just make sure that this record is updated using PUT request
                        workout.setVersion(1L);
                    }
                }
                mWorkoutDao.insertOrReplace(workout);
            }

            if (!TextUtils.isEmpty(runList.getNextUrl())) {
                // Recursive call to fetch the runs of next page
                return forceRefreshAllWorkoutData(runList.getNextUrl());
            } else {
                // All the runs are pulled from server and written into DB
                Logger.d(TAG, "forceRefreshAllWorkoutData, Setting SyncedTimeStampMillis as: "
                        + refreshAllTimeStamp);
                SharedPrefsManager.getInstance().setLong(Constants.PREF_WORKOUT_DATA_SYNC_VERSION,
                        (refreshAllTimeStamp / 1000));
                SharedPrefsManager.getInstance().setBoolean(Constants.PREF_IS_WORKOUT_DATA_UP_TO_DATE_IN_DB, true);
                Utils.updateTrackRecordFromDb();
                EventBus.getDefault().post(new UpdateEvent.RunDataUpdated());
                return ExpoBackoffTask.RESULT_SUCCESS;
            }
        } catch (NetworkException e) {
            Logger.d(TAG, "NetworkException in forceRefreshAllWorkoutData: " + e);
            e.printStackTrace();
            Crashlytics.log("forceRefreshAllWorkoutData networkException for user_id ("
                    + MainApplication.getInstance().getUserID() + "), messageFromServer: " + e
                    + ", while syncing runs at URL: " + runUrl);
            Crashlytics.logException(e);
            AnalyticsEvent.create(Event.ON_FORCE_REFRESH_FAILURE)
                    .put("exception_message", e.getMessage())
                    .put("message_from_server", e.getMessageFromServer())
                    .put("http_status", e.getHttpStatusCode())
                    .put("failure_type", e.getFailureType())
                    .buildAndDispatch();

            return ExpoBackoffTask.RESULT_RESCHEDULE;
        } catch (Exception e) {
            Logger.d(TAG, "Exception in forceRefreshAllWorkoutData: " + e.getMessage());
            e.printStackTrace();
            Crashlytics.log("forceRefreshAllWorkoutData Exception for user_id ("
                    + MainApplication.getInstance().getUserID() + "), while syncing runs at URL: " + runUrl);
            Crashlytics.logException(e);
            AnalyticsEvent.create(Event.ON_FORCE_REFRESH_FAILURE)
                    .put("exception_message", e.getMessage())
                    .buildAndDispatch();

            return ExpoBackoffTask.RESULT_RESCHEDULE;
        }

    }

    private static int getCharityOverviewData() {

        try {
            JsonObject response = NetworkDataProvider.doGetCall(Urls.getImpactOverviewUrl(), JsonObject.class);
            String responseString = response.getAsJsonObject("result").toString();
            Logger.d(TAG,"getCharityOverviewData response : "+responseString);
            SharedPrefsManager.getInstance().setString(Constants.PREF_CHARITY_OVERVIEW,responseString);
            SharedPrefsManager.getInstance().setBoolean(Constants.PREF_CHARITY_OVERVIEW_DATA_LOAD,false);

            return ExpoBackoffTask.RESULT_SUCCESS;

        } catch (NetworkException e) {
            Logger.d(TAG, "NetworkException in getCharityOverviewData: " + e);
            e.printStackTrace();
            Crashlytics.log("getCharityOverviewData networkException for user_id ("
                    + MainApplication.getInstance().getUserID() + "), messageFromServer: " + e
                    + ", while syncing runs at URL: " + Urls.getImpactOverviewUrl());
            Crashlytics.logException(e);
            AnalyticsEvent.create(Event.ON_GET_CHARITY_OVERVIEW_FAILURE)
                    .put("exception_message", e.getMessage())
                    .put("message_from_server", e.getMessageFromServer())
                    .put("http_status", e.getHttpStatusCode())
                    .put("failure_type", e.getFailureType())
                    .buildAndDispatch();

            return ExpoBackoffTask.RESULT_RESCHEDULE;
        } catch (Exception e) {

            Logger.d(TAG, "Exception in getCharityOverviewData: " + e.getMessage());
            e.printStackTrace();
            Crashlytics.log("getCharityOverviewData Exception for user_id (" + MainApplication.getInstance().getUserID() + ") ," +
                    "while syncing charity overview data at URL : " + Urls.getImpactOverviewUrl());
            Crashlytics.logException(e);
            AnalyticsEvent.create(Event.ON_GET_CHARITY_OVERVIEW_FAILURE)
                    .put("exception_message", e.getMessage())
                    .buildAndDispatch();

            return ExpoBackoffTask.RESULT_RESCHEDULE;
        }

    }

    private static int getAchievedBadgeData() {

        try {
            JsonObject response = NetworkDataProvider.doGetCall(Urls.getAchievementUrl(), JsonObject.class);
            String responseString = response.getAsJsonObject("result").toString();
            JSONArray jsonArray = new JSONArray(responseString);
            AchievedBadgeDao achievedBadgeDao = MainApplication.getInstance().getDbWrapper().getAchievedBadgeDao();

            for(int i=0;i<jsonArray.length();i++)
            {

            }
            Logger.d(TAG,"getCharityOverviewData response : "+responseString);
            SharedPrefsManager.getInstance().setString(Constants.PREF_CHARITY_OVERVIEW,responseString);
            SharedPrefsManager.getInstance().setBoolean(Constants.PREF_CHARITY_OVERVIEW_DATA_LOAD,false);

            return ExpoBackoffTask.RESULT_SUCCESS;

        } catch (NetworkException e) {
            Logger.d(TAG, "NetworkException in getAchievedBadgeData: " + e);
            e.printStackTrace();
            Crashlytics.log("getAchievedBadgeData networkException for user_id ("
                    + MainApplication.getInstance().getUserID() + "), messageFromServer: " + e
                    + ", while syncing runs at URL: " + Urls.getImpactOverviewUrl());
            Crashlytics.logException(e);
            AnalyticsEvent.create(Event.ON_GET_CHARITY_OVERVIEW_FAILURE)
                    .put("exception_message", e.getMessage())
                    .put("message_from_server", e.getMessageFromServer())
                    .put("http_status", e.getHttpStatusCode())
                    .put("failure_type", e.getFailureType())
                    .buildAndDispatch();

            return ExpoBackoffTask.RESULT_RESCHEDULE;
        } catch (Exception e) {

            Logger.d(TAG, "Exception in getAchievedBadgeData: " + e.getMessage());
            e.printStackTrace();
            Crashlytics.log("getAchievedBadgeData Exception for user_id (" + MainApplication.getInstance().getUserID() + ") ," +
                    "while syncing charity overview data at URL : " + Urls.getImpactOverviewUrl());
            Crashlytics.logException(e);
            AnalyticsEvent.create(Event.ON_GET_CHARITY_OVERVIEW_FAILURE)
                    .put("exception_message", e.getMessage())
                    .buildAndDispatch();

            return ExpoBackoffTask.RESULT_RESCHEDULE;
        }

    }


}
