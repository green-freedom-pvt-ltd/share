package com.sharesmile.share.gcm;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.clevertap.android.sdk.CleverTapAPI;
import com.onesignal.GcmBroadcastReceiver;

/**
 * Created by ankitmaheshwari on 3/15/17.
 */

public class CustomGcmBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        CleverTapAPI.createNotification(context, intent.getExtras());
        new GcmBroadcastReceiver().onReceive(context, intent);
    }

}
