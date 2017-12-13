package com.sharesmile.share.googleapis;

import android.content.Context;

import com.google.android.gms.fitness.data.DataType;
import com.sharesmile.share.utils.Logger;

/**
 * Created by ankitmaheshwari on 12/8/17.
 */

public class GoogleFitDistanceTracker implements GoogleFitSensorTracker.Listener, GoogleTracker {

    private static final String TAG = "GoogleFitDistanceTracker";

    private Listener listener;
    private GoogleFitSensorTracker tracker;


    public GoogleFitDistanceTracker(Context context, Listener listener) {
        this.listener = listener;
        tracker = new GoogleFitSensorTracker(context, DataType.TYPE_DISTANCE_DELTA, this);
    }

    @Override
    public void start() {
        Logger.d(TAG, "start");
        tracker.start();
    }

    @Override
    public void stop() {
        Logger.d(TAG, "stop");
        tracker.stop();
    }

    @Override
    public void pause() {
        Logger.d(TAG, "pause");
        tracker.pause();
    }

    @Override
    public void resume() {
        Logger.d(TAG, "resume");
        tracker.resume();
    }

    @Override
    public void isTrackerReady() {
        listener.onDistanceTrackerReady();
    }

    @Override
    public void trackerNotAvailable() {
        listener.distanceTrackerNotAvailable();
    }

    @Override
    public void onDeltaCount(long beginTs, long endTs, float deltaIncrement) {
        // Do something with Delta distance
        Logger.d(TAG, "onDeltaCount, beginTs = " + beginTs + ", endTs = " + endTs
                + ", delta in meters = " + deltaIncrement);
        listener.onDistanceUpdate(deltaIncrement);
    }


    interface Listener {

        void distanceTrackerNotAvailable();
        void onDistanceTrackerReady();
        void onDistanceUpdate(float deltaDistance);

    }
}
