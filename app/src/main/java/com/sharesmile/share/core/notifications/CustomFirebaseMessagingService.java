package com.sharesmile.share.core.notifications;

import android.os.Bundle;

import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.NotificationInfo;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sharesmile.share.utils.Logger;

import java.util.Map;

import io.smooch.core.FcmService;

/**
 * Created by ankitmaheshwari on 9/1/17.
 */

public class CustomFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "CustomFirebaseMessagingService";

    @Override
    public void onMessageReceived(RemoteMessage message){
        try {
            Logger.d(TAG, "onMessageReceived: Received message: " + message.getMessageId());
            Map data = message.getData();

            if (data.size() > 0) {
                Bundle extras = new Bundle();
                for (Map.Entry<String, String> entry : message.getData().entrySet()) {
                    extras.putString(entry.getKey(), entry.getValue());
                }

                // Handle for Clevertap:
                NotificationInfo info = CleverTapAPI.getNotificationInfo(extras);
                Logger.d(TAG, "Clevertap notification info: " + info.toString());

                if (info.fromCleverTap) {
                    CleverTapAPI.createNotification(getApplicationContext(), extras);
                }
            }
            Logger.d(TAG, "onMessageReceived: Will pass received message to Smooch");
            // Handle for Smooch
            FcmService.triggerSmoochNotification(data, this);

        } catch (Throwable t) {
            Logger.e(TAG, "Error parsing FCM message", t);
        }
    }

}
