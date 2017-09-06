package com.sharesmile.share.core;

import com.clevertap.android.sdk.FcmTokenListenerService;
import com.google.firebase.iid.FirebaseInstanceId;
import com.sharesmile.share.utils.Logger;
import com.sharesmile.share.utils.SharedPrefsManager;

import static com.sharesmile.share.core.Constants.PREFS_LAST_SAVED_FCM_TOKEN;

/**
 * Created by ankitmaheshwari on 9/6/17.
 */

public class CustomFirebaseInstanceIdService extends FcmTokenListenerService {

    private static final String TAG = "CustomFirebaseInstanceIdService";

    @Override
    public void onTokenRefresh() {
        Logger.d(TAG, "CustomFirebaseInstanceIdService: onTokenRefresh");

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        // Store the refreshedToken in sharedPreferences
        SharedPrefsManager.getInstance().setString(PREFS_LAST_SAVED_FCM_TOKEN, refreshedToken);

//        Smooch.setFirebaseCloudMessagingToken(refreshedToken);

        super.onTokenRefresh();
    }

}
