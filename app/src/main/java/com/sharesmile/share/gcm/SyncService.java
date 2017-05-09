package com.sharesmile.share.gcm;

import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.google.gson.Gson;
import com.sharesmile.share.Events.DBEvent;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.Workout;
import com.sharesmile.share.WorkoutDao;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.network.NetworkDataProvider;
import com.sharesmile.share.network.NetworkException;
import com.sharesmile.share.network.NetworkUtils;
import com.sharesmile.share.rfac.models.CauseList;
import com.sharesmile.share.rfac.models.Run;
import com.sharesmile.share.rfac.models.RunList;
import com.sharesmile.share.rfac.models.UserDetails;
import com.sharesmile.share.sync.SyncHelper;
import com.sharesmile.share.utils.DateUtil;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.SharedPrefsManager;
import com.sharesmile.share.utils.Urls;
import com.squareup.okhttp.Response;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

import static com.sharesmile.share.sync.SyncHelper.updateUserImpact;

/**
 * Created by Shine on 15/05/16.
 */
public class SyncService extends GcmTaskService {
    private static final String TAG = SyncService.class.getSimpleName();

    @Override
    public int onRunTask(TaskParams taskParams) {
        Logger.d(TAG, "runtask started: " + taskParams.getTag());
        if (!SharedPrefsManager.getInstance().getBoolean(Constants.PREF_IS_LOGIN, false)) {
            return GcmNetworkManager.RESULT_FAILURE;
        }
        if (taskParams.getTag().equalsIgnoreCase(TaskConstants.PUSH_WORKOUT_DATA)) {
            return uploadWorkoutData();
        } else if (taskParams.getTag().equalsIgnoreCase(TaskConstants.PULL_ENTIRE_WORKOUT_HISTORY)) {
            return pullEntireWorkoutHistory();
        } else if (taskParams.getTag().equalsIgnoreCase(TaskConstants.UPLOAD_USER_DATA)) {
            return uploadUserData();
        } else if (taskParams.getTag().equalsIgnoreCase(TaskConstants.SYNC_CAUSE_DATA)) {
            return updateCauseData();
        } else if (taskParams.getTag().equalsIgnoreCase(TaskConstants.SYNC_WORKOUT_DATA)) {
            return syncWorkoutData();
        }
        return GcmNetworkManager.RESULT_SUCCESS;
    }

    @Override
    public void onInitializeTasks() {
        super.onInitializeTasks();
        MainApplication.getInstance().startSyncTasks();
    }

    public static int updateCauseData() {
        Logger.d(TAG, "updateCauseData");
        try {
            CauseList causeList = NetworkDataProvider.doGetCall(Urls.getCauseListUrl(), CauseList.class);
            MainApplication.getInstance().updateCauseList(causeList);
            EventBus.getDefault().post(new DBEvent.CauseDataUpdated(causeList));
            return GcmNetworkManager.RESULT_SUCCESS;
        } catch (NetworkException e) {
            Logger.e(TAG, "Exception occurred while fetching updated cause list from network");
            e.printStackTrace();
            return GcmNetworkManager.RESULT_FAILURE;
        }
    }


    private int syncWorkoutData(){
        WorkoutDao mWorkoutDao = MainApplication.getInstance().getDbWrapper().getWorkoutDao();
        long clientVersion = SharedPrefsManager.getInstance().getLong(Constants.PREF_WORKOUT_DATA_SYNC_VERSION);
        if (clientVersion > 0){
            String syncUrl;
            syncUrl = Urls.getSyncRunUrl(clientVersion);
            Logger.d(TAG, "Starting sync with client_version: " + clientVersion);
            syncWorkoutTimeStamp = 0;
            int result = syncWorkoutData(syncUrl, mWorkoutDao);
            return result;
        }else {
            Logger.e(TAG, "Client Version not present, must fetch historical runs before");
            SyncHelper.pullEntireWorkoutHistory();
            return  GcmNetworkManager.RESULT_FAILURE;
        }
    }

    private long syncWorkoutTimeStamp;

    private int syncWorkoutData(String syncUrl, WorkoutDao mWorkoutDao){
        try {
            Response response = NetworkDataProvider.getResponseForGetCall(syncUrl);
            if (syncWorkoutTimeStamp == 0){
                syncWorkoutTimeStamp = response.headers().getDate("Date").getTime();
                Logger.d(TAG, "syncWorkoutData, fetched SyncedTimeStampMillis as: " + syncWorkoutTimeStamp);
            }
            RunList runList = NetworkUtils.handleResponse(response, RunList.class);

            Gson gson = new Gson();
            Logger.d(TAG, "Syncing these runs in DB " + gson.toJson(runList));
            mWorkoutDao.insertOrReplaceInTx(runList);

            if (!TextUtils.isEmpty(runList.getNextUrl())) {
                // Recursive call to fetch the runs of next page
                return syncWorkoutData(runList.getNextUrl(), mWorkoutDao);
            } else {
                updateUserImpact();
                Logger.d(TAG, "syncWorkoutData, Setting SyncedTimeStampMillis as: " + syncWorkoutTimeStamp);
                SharedPrefsManager.getInstance().setLong(Constants.PREF_WORKOUT_DATA_SYNC_VERSION, (syncWorkoutTimeStamp / 1000));
                return GcmNetworkManager.RESULT_SUCCESS;
            }
        } catch (NetworkException e) {
            e.printStackTrace();
            Logger.d(TAG, "NetworkException while syncing runs with URL: "+syncUrl+"," +
                    " messageFromServer " + e.getMessageFromServer()
                    + " exceptionMessage: " + e.getMessage());
            return GcmNetworkManager.RESULT_RESCHEDULE;
        }
    }

    private int uploadUserData() {

        int user_id = MainApplication.getInstance().getUserID();
        Logger.d(TAG, "uploadUserData for userId: " + user_id );
        try {

            UserDetails prev = MainApplication.getInstance().getUserDetails();

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("first_name", prev.getFirstName());
            jsonObject.put("gender_user", prev.getGenderUser());
            jsonObject.put("phone_number", prev.getPhoneNumber());
            jsonObject.put("body_weight", prev.getBodyWeight());
            jsonObject.put("user_id", user_id);

            Logger.d(TAG, "Syncing user with data " + jsonObject.toString());

            Gson gson = new Gson();
            UserDetails response = NetworkDataProvider.doPutCall(Urls.getUserUrl(user_id), jsonObject, UserDetails.class);
            Logger.d(TAG, "Response for getUser:" + gson.toJson(response));

            //TODO: CalorieCalculation, remove this weight hack after the API returns weight of the user
            if (response.getBodyWeight() == 0f){
                response.setBodyWeight(prev.getBodyWeight());
            }

            MainApplication.getInstance().setUserDetails(response);

            return GcmNetworkManager.RESULT_SUCCESS;

        } catch (NetworkException e) {
            e.printStackTrace();
            Logger.d(TAG, "NetworkException" + e.getMessageFromServer());
            return GcmNetworkManager.RESULT_FAILURE;
        } catch (JSONException e) {
            e.printStackTrace();
            Logger.d(TAG, "NetworkException");
            return GcmNetworkManager.RESULT_FAILURE;
        }

    }

    private int uploadWorkoutData() {

        WorkoutDao mWorkoutDao = MainApplication.getInstance().getDbWrapper().getWorkoutDao();
        List<Workout> mWorkoutList = mWorkoutDao.queryBuilder().where(WorkoutDao.Properties.Is_sync.eq(false)).list();

        if (mWorkoutList != null && mWorkoutList.size() > 0) {

            boolean isSuccess = true;
            for (Workout workout : mWorkoutList) {
                isSuccess = isSuccess && uploadWorkoutData(workout);
            }
            return isSuccess ? GcmNetworkManager.RESULT_SUCCESS : GcmNetworkManager.RESULT_RESCHEDULE;
        } else {
            return GcmNetworkManager.RESULT_SUCCESS;
        }

    }

    private boolean uploadWorkoutData(Workout workout) {

        Logger.d(TAG, "uploadWorkoutData called for client_run_id: " + workout.getWorkoutId());
        int user_id = SharedPrefsManager.getInstance().getInt(Constants.PREF_USER_ID);
        try {

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("user_id", user_id);
            jsonObject.put("cause_run_title", workout.getCauseBrief());
            jsonObject.put("distance", workout.getDistance());

            if (workout.getBeginTimeStamp() != null){
                jsonObject.put("start_time", DateUtil.getDefaultFormattedDate(new Date(workout.getBeginTimeStamp())));
                jsonObject.put("start_time_epoch", workout.getBeginTimeStamp());
            }else if (workout.getDate() != null){
                jsonObject.put("start_time", DateUtil.getDefaultFormattedDate(workout.getDate()));
                jsonObject.put("start_time_epoch", workout.getDate().getTime());
            }

            if (workout.getEndTimeStamp() != null){
                jsonObject.put("end_time", DateUtil.getDefaultFormattedDate(new Date(workout.getEndTimeStamp())));
                jsonObject.put("end_time_epoch", workout.getEndTimeStamp());
            }
            jsonObject.put("run_amount", workout.getRunAmount());
            jsonObject.put("run_duration", workout.getElapsedTime());
            jsonObject.put("no_of_steps", workout.getSteps());
            jsonObject.put("avg_speed", workout.getAvgSpeed());
            jsonObject.put("client_run_id", workout.getWorkoutId());
            jsonObject.put("start_location_lat", workout.getStartPointLatitude());
            jsonObject.put("start_location_long", workout.getStartPointLongitude());
            jsonObject.put("end_location_lat", workout.getEndPointLatitude());
            jsonObject.put("end_location_long", workout.getEndPointLongitude());
            jsonObject.put("version", workout.getVersion());

            Logger.d(TAG, jsonObject.toString());

            Run response = NetworkDataProvider.doPostCall(Urls.getRunUrl(), jsonObject, Run.class);

            WorkoutDao mWorkoutDao = MainApplication.getInstance().getDbWrapper().getWorkoutDao();
            //delete row
            mWorkoutDao.delete(workout);
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
            Logger.d(TAG, "NetworkException: " + e.getMessageFromServer());
            Crashlytics.log("Run sync networkException, messageFromServer: " + e.getMessageFromServer());
            Crashlytics.logException(e);
            AnalyticsEvent.create(Event.ON_RUN_SYNC)
                    .put("upload_result", "failure")
                    .put("client_run_id", workout.getWorkoutId())
                    .put("exception_message", e.getMessage())
                    .put("message_from_server", e.getMessageFromServer())
                    .put("http_status", e.getHttpStatusCode())
                    .buildAndDispatch();
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            Logger.d(TAG, "JSONException");
            Crashlytics.log("Run sync JSONException");
            Crashlytics.logException(e);
            AnalyticsEvent.create(Event.ON_RUN_SYNC)
                    .put("upload_result", "JSONException ")
                    .buildAndDispatch();
            return false;
        }

    }

    public int pullEntireWorkoutHistory() {
        Logger.d(TAG, "pullEntireWorkoutHistory");
        WorkoutDao mWorkoutDao = MainApplication.getInstance().getDbWrapper().getWorkoutDao();
        long syncedCount;
        String runUrl;
        syncedCount = mWorkoutDao.queryBuilder().where(WorkoutDao.Properties.Is_sync.eq(true)).count();
        runUrl = Urls.getFlaggedRunUrl(false);
        fetchAllTimeStamp = 0;
        int result = fetchAllWorkoutData(runUrl, syncedCount);
        return result;
    }

    private long fetchAllTimeStamp;

    private int fetchAllWorkoutData(String runUrl, final long syncedCount) {

        try {
            Response response = NetworkDataProvider.getResponseForGetCall(runUrl);
            if (fetchAllTimeStamp == 0){
                fetchAllTimeStamp = response.headers().getDate("Date").getTime();
                Logger.d(TAG, "fetchAllWorkoutData, fetched SyncedTimeStampMillis as: " + fetchAllTimeStamp);
            }
            RunList runList = NetworkUtils.handleResponse(response, RunList.class);
            // If num of synced run on client is same as total run count on server then no need to update
            if (syncedCount >= runList.getTotalRunCount()) {
                Logger.d(TAG, "fetchAllWorkoutData, update success " + syncedCount + " : " + runList.getTotalRunCount());
                EventBus.getDefault().post(new DBEvent.RunDataUpdated());
                updateUserImpact();
                return GcmNetworkManager.RESULT_SUCCESS;
            } else {
                Gson gson = new Gson();
                Logger.d(TAG, "Updating these runs in DB " + gson.toJson(runList));
                WorkoutDao mWorkoutDao = MainApplication.getInstance().getDbWrapper().getWorkoutDao();
                mWorkoutDao.insertOrReplaceInTx(runList);
                SharedPrefsManager.getInstance().setBoolean(Constants.PREF_HAS_RUN, true);
                if (!TextUtils.isEmpty(runList.getNextUrl())) {
                    // Recursive call to fetch the runs of next page
                    Logger.d(TAG, "fetchAllWorkoutData, Setting SyncedTimeStampMillis as: " + fetchAllTimeStamp);
                    SharedPrefsManager.getInstance().setLong(Constants.PREF_WORKOUT_DATA_SYNC_VERSION, (fetchAllTimeStamp / 1000));
                    return fetchAllWorkoutData(runList.getNextUrl(), syncedCount);
                } else {
                    updateUserImpact();
                }
                EventBus.getDefault().post(new DBEvent.RunDataUpdated());
                return GcmNetworkManager.RESULT_SUCCESS;
            }
        } catch (NetworkException e) {
            e.printStackTrace();
            Logger.d(TAG, "update NetworkException, messageFromServer " + e.getMessageFromServer()
                    + " exceptionMessage: " + e.getMessage());
            return GcmNetworkManager.RESULT_RESCHEDULE;
        }

    }
}
