package com.sharesmile.share.gps;

import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by ankitmaheshwari on 12/1/17.
 */

public abstract class AbstractGoogleFitStepCounter implements StepCounter,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "AbstractGoogleFitStepCounter";

    private Context context;
    private GoogleApiClient mApiClient;
    boolean isPaused;




}
