package com.sharesmile.share.gps;

import android.content.Intent;

/**
 * Created by ankitm on 09/05/16.
 */
public interface StepCounter {

    int PERMISSION_NOT_GRANTED_BY_USER = 101;
    int SENSOR_API_NOT_ADDED = 102;

    void startCounting();
    void stopCounting();
    void onActivityResult(int requestCode, int resultCode, Intent data);

    interface Listener {

        void notAvailable(int reasonCode);
        void isReady();
        void onStepCount(int deltaSteps);

    }
}
