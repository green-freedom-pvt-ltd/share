package com.sharesmile.share.googleapis;

import android.content.Context;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.sharesmile.share.utils.Logger;

/**
 * Created by ankitmaheshwari on 12/8/17.
 */

public class GoogleFitDistanceTracker implements GoogleTracker, GoogleApiHelper.Listener {

    private static final String TAG = "GoogleFitDistanceTracker";

    private Listener listener;
    GoogleApiHelper helper;


    public GoogleFitDistanceTracker(Context context, Listener listener) {
        this.listener = listener;
        this.helper = new GoogleApiHelper(GoogleApiHelper.API_LIVE_DISTANCE_TRACKING, context);
        start();
    }

    @Override
    public void start() {
        Logger.d(TAG, "start");
        helper.setListener(this);
        helper.connect();
    }

    @Override
    public void stop() {
        Logger.d(TAG, "stop");
        if (helper.isConnected()){
            Fitness.SensorsApi.remove( helper.getGoogleApiClient(), this )
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess() && helper.isConnected()) {
                                helper.disconnect();
                            }
                        }
                    });
        }
    }

    @Override
    public void pause() {
        Logger.d(TAG, "pause");
    }

    @Override
    public void resume() {
        Logger.d(TAG, "resume");
    }

    @Override
    public void onConnected() {

    }

    @Override
    public void connectionFailed() {

    }

    interface Listener {

        void notAvailable(int reasonCode);
        void isReady();
        void onDistanceUpdate(int deltaDistance);

    }
}
