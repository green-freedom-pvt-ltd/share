package com.sharesmile.share.core.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.onesignal.OSNotificationAction;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.core.MainActivity;

import org.json.JSONObject;

/**
 * Created by Shine on 27/01/17.
 */

public class OneSignalNotificationHandler implements OneSignal.NotificationOpenedHandler {
    public static final String TAG = OneSignalNotificationHandler.class.getSimpleName();

    @Override
    public void notificationOpened(OSNotificationOpenResult result) {

        OSNotificationAction.ActionType actionType = result.action.type;
        JSONObject data = result.notification.payload.additionalData;
        String key;
        Bundle bundle = new Bundle();
        if (data != null) {
            key = data.optString(NotificationConsts.KEY_SCREEN, null);
            if (!TextUtils.isEmpty(key)) {
                bundle.putString(NotificationConsts.KEY_SCREEN, key);
                Log.i(TAG, "key set with value: " + key);
            }
        }

        Intent intent = new Intent(MainApplication.getContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtras(bundle);
        MainApplication.getContext().startActivity(intent);

    }
}
