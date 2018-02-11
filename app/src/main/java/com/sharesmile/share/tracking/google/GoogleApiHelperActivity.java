package com.sharesmile.share.tracking.google;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.sharesmile.share.tracking.google.event.GoogleApiResultEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by ankitmaheshwari on 12/2/17.
 */

public class GoogleApiHelperActivity extends AppCompatActivity {

    int apiCode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiCode = getIntent().getIntExtra(GoogleApiHelper.KEY_API_CODE, 0);

        ConnectionResult connectionResult
                = getIntent().getParcelableExtra(GoogleApiHelper.KEY_RESOLUTION_PARCELABLE);
        try {
            // Show the dialog by calling startResolutionForResult(),
            // and check the result in onActivityResult().
            connectionResult.startResolutionForResult(this, apiCode);
        } catch (IntentSender.SendIntentException e) {
            // Ignore the error.
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        finish();
        EventBus.getDefault().post(new GoogleApiResultEvent(resultCode, requestCode, data));
    }


}
