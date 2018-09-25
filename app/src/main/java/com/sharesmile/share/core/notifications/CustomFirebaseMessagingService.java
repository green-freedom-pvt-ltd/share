package com.sharesmile.share.core.notifications;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.NotificationInfo;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.sharesmile.share.R;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.Logger;
import com.sharesmile.share.core.SharedPrefsManager;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.core.event.UpdateEvent;
import com.sharesmile.share.login.UserDetails;
import com.sharesmile.share.refer_program.model.ReferrerDetails;
import com.sharesmile.share.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.Map;

import io.smooch.core.FcmService;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;
import static com.sharesmile.share.core.application.MainApplication.getContext;
import static com.sharesmile.share.core.notifications.NotificationActionReceiver.SMC_FCM_NOTIFICATION_ID;

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
                    referrerDetails.setReferalId(Integer.parseInt(extras.getString(Constants.SMC_NOTI_INVITEE_USER_ID)));
                    referrerDetails.setReferalName(extras.getString(Constants.SMC_NOTI_INVITEE_NAME));
                    referrerDetails.setReferrerSocialThumb(extras.getString(Constants.SMC_NOTI_INVITEE_SOCIAL_THUMB));
                    referrerDetails.setReferrerProfilePicture(extras.getString(Constants.SMC_NOTI_INVITEE_PROFILE_PICTURE));
                    ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
                    ActivityManager.getMyMemoryState(appProcessInfo);
                    if (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE) {
                        EventBus.getDefault().post(new UpdateEvent.OnReferrerSuccessful(referrerDetails));
                    } else {
                        sendNotification(referrerDetails);
                        JSONObject jsonObject = new JSONObject(SharedPrefsManager.getInstance().getString(Constants.SMC_NOTI_INVITEES_JSON, "{}"));
                        if (!jsonObject.has(referrerDetails.getReferalId() + "")) {
                            jsonObject.put(referrerDetails.getReferalId() + "", new Gson().toJson(referrerDetails));
                        }
                        SharedPrefsManager.getInstance().setString(Constants.SMC_NOTI_INVITEES_JSON, jsonObject.toString());
                    }
                    Logger.d(TAG, referrerDetails.getReferalId() + "," + referrerDetails.getReferalName());
                    UserDetails userDetails = MainApplication.getInstance().getUserDetails();
                    userDetails.setMealsShared(userDetails.getMealsShared() + 1);
                    MainApplication.getInstance().setUserDetails(userDetails);
                    EventBus.getDefault().post(new UpdateEvent.OnMealAdded());
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

    private void sendNotification(ReferrerDetails referrerDetails) {
        NotificationManager mNotificationManager =
                (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        // This try catch bug is to avoid crash due to runtimeexception
        try {


            NotificationCompat.Builder mBuilder = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mBuilder = Utils.createChannelForNotification(getContext(), getContext().getString(R.string.channel_description_general),
                        getContext().getString(R.string.channel_smc_fcm_general), true);
            } else {
                mBuilder = new NotificationCompat.Builder(getContext());
            }

            mBuilder
                    .setContentTitle("Invite Successful")
                    .setContentText("You friend," + referrerDetails.getReferalName() + " has joined Impact")
                    .setSmallIcon(getNotificationIcon())
                    .setColor(ContextCompat.getColor(getContext(), R.color.bright_sky_blue))
                    .setLargeIcon(getLargeIcon(getContext()))
                    .setTicker(getContext().getResources().getString(R.string.app_name))
                    .setOngoing(false)
                    .setVisibility(1)
                    .setSound(Uri.parse("android.resource://"
                            + getContext().getPackageName() + "/" + R.raw.slow_spring_board))
                    .setVibrate(new long[]{0, 100, 200, 300});


//            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
//                mBuilder.addAction(pauseResumeDrawable, pauseResumeLabel, MainApplication.getInstance().createNotificationActionIntent(pauseResumeIntent, pauseResumeAction))
//                        .addAction(R.drawable.ic_stop_black_24px, "Stop", MainApplication.getInstance().createNotificationActionIntent(MainActivity.INTENT_STOP_RUN, getString(R.string.notification_action_stop)));
//            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
            }

            mBuilder.setContentIntent(MainApplication.getInstance().createAppIntent());

            mNotificationManager.notify(SMC_FCM_NOTIFICATION_ID, mBuilder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap getLargeIcon(Context context) {
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        return largeIcon;
    }

    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.mipmap.ic_notification_small : R.mipmap.ic_launcher;
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

    }
}
