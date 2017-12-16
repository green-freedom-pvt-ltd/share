package com.sharesmile.share.googleapis;

import android.content.Context;

import com.sharesmile.share.gps.StepCounter;
import com.sharesmile.share.gps.models.WorkoutData;
import com.sharesmile.share.utils.Logger;

/**
 * Created by ankitmaheshwari on 12/12/17.
 */

public class GoogleFitTracker implements StepCounter.Listener,
        GoogleFitDistanceTracker.Listener {

    private static final String TAG = "GoogleFitTracker";

    GoogleFitStepCounter stepCounter;
    GoogleFitDistanceTracker distanceTracker;
    GoogleFitnessSessionRecorder sessionRecorder;
    GoogleFitListener googleFitListener;

    public GoogleFitTracker(Context context, GoogleFitListener listener) {
        stepCounter = new GoogleFitStepCounter(context, this);
        distanceTracker = new GoogleFitDistanceTracker(context, this);
        sessionRecorder = new GoogleFitnessSessionRecorder(context);
        googleFitListener = listener;
    }

    public void start() {
        Logger.d(TAG, "start");
        stepCounter.start();
        distanceTracker.start();
        sessionRecorder.start();
    }

    /**
     * Synchronously reads and updates the tracked data in result object and then stops tracking.
     * Should not be called on the main thread.
     * @param result
     */
    public void readAndStop(WorkoutData result) {
        Logger.d(TAG, "readAndStop");
        stepCounter.stop();
        distanceTracker.stop();
        sessionRecorder.readAndStop(result);
    }

    public void pause() {
        Logger.d(TAG, "pause");
        stepCounter.pause();
        distanceTracker.pause();
        sessionRecorder.pause();
    }

    public void resume() {
        Logger.d(TAG, "resume");
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
        googleFitListener.onGoogleFitStepCount(deltaSteps);
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
        googleFitListener.onGoogleFitDistanceUpdate(deltaDistance);
    }

    public interface GoogleFitListener{
        void onGoogleFitStepCount(int deltaSteps);
        void onGoogleFitDistanceUpdate(float deltaDistance);
    }
}
