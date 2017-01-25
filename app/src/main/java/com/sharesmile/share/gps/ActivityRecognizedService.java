package com.sharesmile.share.gps;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.utils.Logger;

/**
 * Created by piyush on 10/1/16.
 */

public class ActivityRecognizedService extends IntentService {

    private static boolean isInVehicle = false;
    private static int stillOccurredCounter = 0;

    public static final int CONFIDENCE_THRESHOLD = 85;

    private static final String TAG = "ActivityRecognizedService";

    public ActivityRecognizedService() {
        super(TAG);
    }

    public ActivityRecognizedService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        Logger.d(TAG, "onCreate");
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Logger.d(TAG, "onHandleIntent");
        if(ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            Logger.d( TAG, "IN_VEHICLE, confidence " +  result.getActivityConfidence(DetectedActivity.IN_VEHICLE));
            if (result.getActivityConfidence(DetectedActivity.IN_VEHICLE) > CONFIDENCE_THRESHOLD){
                isInVehicle = true;
                MainApplication.showRunNotification("We have detected that you are driving.");
            }else {
                isInVehicle = false;
            }
            Logger.d( TAG, "STILL, confidence " +  result.getActivityConfidence(DetectedActivity.STILL));
            if (result.getActivityConfidence(DetectedActivity.STILL) > CONFIDENCE_THRESHOLD){
                if (stillOccurredCounter == 0){
                    // Show notification and increment still occurred counter
                    MainApplication.showRunNotification("It seems like you are still!");
                    stillOccurredCounter = 1;
                }
            }
            Logger.d( TAG, "ON_FOOT, confidence " +  result.getActivityConfidence(DetectedActivity.STILL));
            if (result.getActivityConfidence(DetectedActivity.ON_FOOT) > CONFIDENCE_THRESHOLD){
                stillOccurredCounter = 0;
            }
        }
    }

    public static boolean isIsInVehicle(){
        return isInVehicle;
    }


}
