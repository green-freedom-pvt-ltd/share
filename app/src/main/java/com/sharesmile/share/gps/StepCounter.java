package com.sharesmile.share.gps;

import android.content.Intent;

/**
 * Created by ankitm on 09/05/16.
 */
public interface StepCounter {

    int PERMISSION_NOT_GRANTED_BY_USER = 101;
    int SENSOR_API_NOT_ADDED = 102;
    int STEP_DETECTOR_NOT_SUPPORTED = 103;
    int GOOGLE_FIT_STEP_COUNTER_DEPRECATED = 104;

    int NUM_ELEMS_IN_HISTORY_QUEUE = 8;
    long STEP_COUNT_READING_VALID_INTERVAL = 25; // in secs

    void startCounting();
    void stopCounting();
    void pauseCounting();
    void resumeCounting();
    void onActivityResult(int requestCode, int resultCode, Intent data);

    /**
     * Returns current step speed of the user, which is based on the last few step readings from sensor
     * @return steps per sec as float value, returns -1 if step counting has not started yet.
     */
    float getMovingAverageOfStepsPerSec();

    interface Listener {

        void notAvailable(int reasonCode);
        void isReady();
        void onStepCount(int deltaSteps);

    }
}
