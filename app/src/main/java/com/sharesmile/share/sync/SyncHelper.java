package com.sharesmile.share.sync;

import android.text.TextUtils;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.Task;
import com.sharesmile.share.Events.DBEvent;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.WorkoutDao;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.gcm.SyncService;
import com.sharesmile.share.gcm.TaskConstants;
import com.sharesmile.share.network.NetworkDataProvider;
import com.sharesmile.share.network.NetworkException;
import com.sharesmile.share.rfac.models.RunList;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.SharedPrefsManager;
import com.sharesmile.share.utils.Urls;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Shine on 20/07/16.
 */
public class SyncHelper {

    private static final String TAG = SyncHelper.class.getSimpleName();

    public static void syncRunData() {
        OneoffTask task = new OneoffTask.Builder()
                .setService(SyncService.class)
                .setTag(TaskConstants.UPDATE_WORKOUT_DATA)
                .setExecutionWindow(0L, 1L)
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED).setPersisted(true)
                .build();

        GcmNetworkManager mGcmNetworkManager = GcmNetworkManager.getInstance(MainApplication.getContext().getApplicationContext());
        mGcmNetworkManager.schedule(task);
    }

    public static int updateWorkoutData() {
        WorkoutDao mWorkoutDao = MainApplication.getInstance().getDbWrapper().getWorkoutDao();
        long workoutCount = mWorkoutDao.queryBuilder().where(WorkoutDao.Properties.Is_sync.eq(true)).count();
        String runUrl = Urls.getRunUrl();
        return updateWorkoutData(runUrl, workoutCount);
    }

    private static int updateWorkoutData(String runUrl, long workoutCount) {

        try {
            RunList runList = NetworkDataProvider.doGetCall(runUrl, RunList.class);
            if (workoutCount >= runList.getTotalRunCount()) {
                Logger.d(TAG, "update success" + workoutCount + " : " + runList.getTotalRunCount());
                EventBus.getDefault().post(new DBEvent.RunDataUpdated());
                return GcmNetworkManager.RESULT_SUCCESS;
            } else {
                WorkoutDao mWorkoutDao = MainApplication.getInstance().getDbWrapper().getWorkoutDao();
                mWorkoutDao.insertOrReplaceInTx(runList);
                SharedPrefsManager.getInstance().setBoolean(Constants.PREF_HAS_RUN, true);
                Logger.d(TAG, "update success" + runList.toString());
                if (!TextUtils.isEmpty(runList.getNextUrl())) {
                    updateWorkoutData(runList.getNextUrl(), workoutCount);
                }
            }
        } catch (NetworkException e) {
            e.printStackTrace();
            Logger.d(TAG, "update NetworkException" + e.getMessageFromServer() + e.getMessage());
        }
        EventBus.getDefault().post(new DBEvent.RunDataUpdated());

        return 0;
    }

}
