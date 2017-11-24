package com.sharesmile.share.sync;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class SyncTaskManger extends IntentService {
    private static final String ACTION_FETCH_MESSAGES = "com.sharesmile.share.sync.action.fetchmessages";
    private static final String ACTION_FETCH_COMPAIGN = "com.sharesmile.share.sync.action.fetchCompaign";

    private static final String TAG = "SyncTaskManger";


    public SyncTaskManger() {
        super("SyncTaskManger");
    }

    public static void fetchMessageData(Context context) {
        // This try catch block is to avoid SecurityException crash in Oppo phones
        try {
            Intent intent = new Intent(context, SyncTaskManger.class);
            intent.setAction(ACTION_FETCH_MESSAGES);
            context.startService(intent);
        }catch (SecurityException se){
            se.printStackTrace();
            Log.e(TAG, "SecurityException while starting SyncTaskManager");
        }
    }

    public static void startCampaign(Context context) {
        try {
            Intent intent = new Intent(context, SyncTaskManger.class);
            intent.setAction(ACTION_FETCH_COMPAIGN);
            context.startService(intent);
        }catch (SecurityException se){
            se.printStackTrace();
            Log.e(TAG, "SecurityException while starting SyncTaskManager");
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FETCH_MESSAGES.equals(action)) {
                SyncHelper.fetchMessage();
            } else if (ACTION_FETCH_COMPAIGN.equals(action)) {
                SyncHelper.fetchCampaign(this);
            }
        }
    }

}
