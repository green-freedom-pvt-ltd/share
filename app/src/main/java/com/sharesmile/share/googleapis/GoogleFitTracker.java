package com.sharesmile.share.googleapis;

import android.content.Context;

import com.sharesmile.share.gps.StepCounter;
import com.sharesmile.share.utils.Logger;

/**
 * Created by ankitmaheshwari on 12/12/17.
 */

public class GoogleFitTracker implements GoogleTracker, StepCounter.Listener,
        GoogleFitDistanceTracker.Listener {

    private static final String TAG = "GoogleFitTracker";

    GoogleFitStepCounter stepCounter;
    GoogleFitDistanceTracker distanceTracker;
    GoogleFitnessSessionRecorder sessionRecorder;

    public GoogleFitTracker(Context context) {
        stepCounter = new GoogleFitStepCounter(context, this);
        distanceTracker = new GoogleFitDistanceTracker(context, this);
        sessionRecorder = new GoogleFitnessSessionRecorder(context);
    }

    @Override
    public void start() {
        stepCounter.start();
        distanceTracker.start();
        sessionRecorder.start();
    }

    @Override
    public void stop() {
        stepCounter.stop();
        distanceTracker.stop();
        sessionRecorder.stop();
    }

    @Override
    public void pause() {
        stepCounter.pause();
        distanceTracker.pause();
        sessionRecorder.pause();
    }

    @Override
    public void resume() {
        stepCounter.resume();
        distanceTracker.resume();
        sessionRecorder.resume();
    }

    @Override
    public void stepCounterNotAvailable(int reasonCode) {
        Logger.d(TAG, "stepCounterNotAvailable: reasonCode = " + reasonCode );
    }

    @Override
    public void stepCounterReady() {
        Logger.d(TAG, "stepCounterReady");
    }

    @Override
    public void onStepCount(int deltaSteps) {
    }

    @Override
    public void distanceTrackerNotAvailable() {
        Logger.d(TAG, "distanceTrackerNotAvailable");
    }

    @Override
    public void onDistanceTrackerReady() {
        Logger.d(TAG, "onDistanceTrackerReady");
    }

    @Override
    public void onDistanceUpdate(float deltaDistance) {

    }
}
