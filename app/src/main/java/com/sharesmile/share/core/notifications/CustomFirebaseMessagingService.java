package com.sharesmile.share.core.notifications;

import android.os.Bundle;

import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.NotificationInfo;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.event.UpdateEvent;
import com.sharesmile.share.refer_program.model.ReferrerDetails;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;

import io.smooch.core.FcmService;

/**
 * Created by ankitmaheshwari on 9/1/17.
 */

public class CustomFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "CustomFirebaseMessagingService";

    @Override
    public void onMessageReceived(RemoteMessage message) {
        try {
            Logger.d(TAG, "onMessageReceived: Received message: " + message.getMessageId());
            Map data = message.getData();
            if (data.size() > 0) {
                Bundle extras = new Bundle();
                for (Map.Entry<String, String> entry : message.getData().entrySet()) {
                    extras.putString(entry.getKey(), entry.getValue());
                }
                if (extras.containsKey(Constants.SMC_NOTI_INVITEE_USER_ID)) {
                    ReferrerDetails referrerDetails = new ReferrerDetails();
                    referrerDetails.setReferalId(extras.getInt(Constants.SMC_NOTI_INVITEE_USER_ID));
                    referrerDetails.setReferalName(extras.getString(Constants.SMC_NOTI_INVITEE_NAME));
                    referrerDetails.setReferrerSocialThumb(extras.getString(Constants.SMC_NOTI_INVITEE_SOCIAL_THUMB));
                    referrerDetails.setReferrerProfilePicture(extras.getString(Constants.SMC_NOTI_INVITEE_PROFILE_PICTURE));
                    EventBus.getDefault().post(new UpdateEvent.OnReferrerSuccessful(referrerDetails));
                } else {
                    // Handle for Clevertap:
                    NotificationInfo info = CleverTapAPI.getNotificationInfo(extras);
                    Logger.d(TAG, "Clevertap notification info: " + info.toString());

                    if (info.fromCleverTap) {
                        CleverTapAPI.createNotification(getApplicationContext(), extras);
                    }
                }
            }
            Logger.d(TAG, "onMessageReceived: Will pass received message to Smooch");
            // Handle for Smooch
            FcmService.triggerSmoochNotification(data, this);

        } catch (Throwable t) {
            Logger.e(TAG, "Error parsing FCM message", t);
        }
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

    }
}
