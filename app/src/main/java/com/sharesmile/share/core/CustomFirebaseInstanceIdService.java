package com.sharesmile.share.core;

import com.clevertap.android.sdk.FcmTokenListenerService;
import com.google.firebase.iid.FirebaseInstanceId;
import com.sharesmile.share.utils.Logger;

import io.smooch.core.Smooch;

/**
 * Created by ankitmaheshwari on 9/6/17.
 */

public class CustomFirebaseInstanceIdService extends FcmTokenListenerService {

    private static final String TAG = "CustomFirebaseInstanceIdService";

    @Override
    public void onTokenRefresh() {
        Logger.d(TAG, "CustomFirebaseInstanceIdService: onTokenRefresh");

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        Smooch.setFirebaseCloudMessagingToken(refreshedToken);

        super.onTokenRefresh();
    }

}
