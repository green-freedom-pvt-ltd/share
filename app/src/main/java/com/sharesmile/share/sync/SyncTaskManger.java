package com.sharesmile.share.sync;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class SyncTaskManger extends IntentService {
    private static final String ACTION_UPDATE_RUN = "com.sharesmile.share.sync.action.updaterundata";
    private static final String ACTION_FETCH_MESSAGES = "com.sharesmile.share.sync.action.fetchmessages";
    private static final String ACTION_FETCH_COMPAIGN = "com.sharesmile.share.sync.action.fetchCompaign";


    public SyncTaskManger() {
        super("SyncTaskManger");
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

    public static void startCampaign(Context context) {
        Intent intent = new Intent(context, SyncTaskManger.class);
        intent.setAction(ACTION_FETCH_COMPAIGN);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATE_RUN.equals(action)) {
                SyncHelper.pullRunData();
            } else if (ACTION_FETCH_MESSAGES.equals(action)) {
                SyncHelper.fetchMessage();
            } else if (ACTION_FETCH_COMPAIGN.equals(action)) {
                SyncHelper.fetchCampaign(this);
            }
        }
    }

}
