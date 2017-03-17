package com.sharesmile.share.sync;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.sharesmile.share.BuildConfig;
import com.sharesmile.share.CauseDao;
import com.sharesmile.share.Events.DBEvent;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.network.NetworkDataProvider;
import com.sharesmile.share.network.NetworkException;
import com.sharesmile.share.rfac.models.CauseData;
import com.sharesmile.share.rfac.models.CauseList;
import com.sharesmile.share.utils.SharedPrefsManager;
import com.sharesmile.share.utils.Urls;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class SyncTaskManger extends IntentService {
    private static final String ACTION_CAUSE = "com.sharesmile.share.sync.action.cause";
    private static final String ACTION_UPDATE_RUN = "com.sharesmile.share.sync.action.updaterundata";
    private static final String ACTION_FETCH_MESSAGES = "com.sharesmile.share.sync.action.fetchmessages";
    private static final String ACTION_FETCH_LEADERBOARD = "com.sharesmile.share.sync.action.fetchleaderboard";
    private static final String ACTION_FETCH_COMPAIGN = "com.sharesmile.share.sync.action.fetchCompaign";


    public SyncTaskManger() {
        super("SyncTaskManger");
    }

    public static void startCauseSync(Context context) {
        Intent intent = new Intent(context, SyncTaskManger.class);
        intent.setAction(ACTION_CAUSE);
        context.startService(intent);
    }

    public static void startRunDataUpdate(Context context) {
        Intent intent = new Intent(context, SyncTaskManger.class);
        intent.setAction(ACTION_UPDATE_RUN);
        context.startService(intent);
    }

    public static void fetchMessageData(Context context) {
        Intent intent = new Intent(context, SyncTaskManger.class);
        intent.setAction(ACTION_FETCH_MESSAGES);
        context.startService(intent);
    }

    public static void fetchLeaderBoardData(Context context) {
        Intent intent = new Intent(context, SyncTaskManger.class);
        intent.setAction(ACTION_FETCH_LEADERBOARD);
        context.startService(intent);
    }

    public static void startCampaign(Context context) {
        Intent intent = new Intent(context, SyncTaskManger.class);
        intent.setAction(ACTION_FETCH_COMPAIGN);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CAUSE.equals(action)) {
                updateCauses();
            } else if (ACTION_UPDATE_RUN.equals(action)) {
                SyncHelper.pullRunData();
            } else if (ACTION_FETCH_MESSAGES.equals(action)) {
                SyncHelper.fetchMessage();
            } else if (ACTION_FETCH_LEADERBOARD.equals(action)) {
                SyncHelper.fetchLeaderBoard();
            } else if (ACTION_FETCH_COMPAIGN.equals(action)) {
                SyncHelper.fetchCampaign(this);
            }
        }
    }

    private void updateCauses() {

        try {
            CauseList causeList = NetworkDataProvider.doGetCall(Urls.getCauseListUrl(), CauseList.class);
            CauseDao mCauseDao = MainApplication.getInstance().getDbWrapper().getCauseDao();

            CauseList activeCauseList = new CauseList();
            activeCauseList.setCauses(new ArrayList<CauseData>());
            for (CauseData data : causeList.getCauses()) {
                if (data.isActive()) {
                    Picasso.with(this).load(data.getCauseThankYouImage()).fetch();
                    Picasso.with(this).load(data.getSponsor().getLogoUrl()).fetch();
                    Picasso.with(this).load(data.getImageUrl()).fetch();
                    activeCauseList.getCauses().add(data);
                }

                if (data.getApplicationUpdate() != null) {
                    int latestVersion = SharedPrefsManager.getInstance().getInt(Constants.PREF_LATEST_APP_VERSION, 0);
                    if (latestVersion < data.getApplicationUpdate().app_version && data.getApplicationUpdate().app_version > BuildConfig.VERSION_CODE) {
                        SharedPrefsManager.getInstance().setBoolean(Constants.PREF_SHOW_APP_UPDATE_DIALOG, true);
                        SharedPrefsManager.getInstance().setBoolean(Constants.PREF_FORCE_UPDATE, data.getApplicationUpdate().force_update);
                        SharedPrefsManager.getInstance().setInt(Constants.PREF_LATEST_APP_VERSION, data.getApplicationUpdate().app_version);
                        SharedPrefsManager.getInstance().setString(Constants.PREF_APP_UPDATE_MESSAGE, data.getApplicationUpdate().message);
                    }
                }
                mCauseDao.insertOrReplace(data.getCauseDbObject());
            }

            EventBus.getDefault().post(new DBEvent.CauseDataUpdated(activeCauseList));

        } catch (NetworkException e) {
            e.printStackTrace();
            return;
        }
    }

}
