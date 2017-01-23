package com.sharesmile.share.gps;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.utils.Logger;

import java.util.List;

/**
 * Created by piyush on 10/1/16.
 */

public class ActivityRecognizedService extends IntentService {

    public static DetectedActivity detectedActivity = null;
    public static String detectedActivityText = "Running";

    private static final String TAG = "ActivityRecognizedService";

    public ActivityRecognizedService() {
        super("ActivityRecognizedService");
    }

    public ActivityRecognizedService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivities(result.getProbableActivities());
        }
    }

    private void handleDetectedActivities(List<DetectedActivity> probableActivities) {
        for( DetectedActivity activity : probableActivities ) {
            switch( activity.getType() ) {
                case DetectedActivity.IN_VEHICLE: {
                    Logger.d( TAG, "In Vehicle: " + activity.getConfidence() );
                    detectedActivity = activity;
                    if(activity.getConfidence() > 85) {
                        detectedActivityText = "Driving";
                        MainApplication.showRunNotification("We have detected that you are driving.");
                    }
                    break;
                }
                case DetectedActivity.ON_BICYCLE: {
                    Logger.d( TAG, "On Bicycle: " + activity.getConfidence() );
                    detectedActivity = activity;
                    detectedActivityText = "Cycling";
                    NotificationManagerCompat.from(this).cancel(0);
                    break;
                }
                case DetectedActivity.ON_FOOT: {
                    Logger.d( TAG, "On Foot: " + activity.getConfidence() );
                    NotificationManagerCompat.from(this).cancel(0);
                    break;
                }
                case DetectedActivity.RUNNING: {
                    Logger.d( TAG, "Running: " + activity.getConfidence() );
                    detectedActivity = activity;
                    NotificationManagerCompat.from(this).cancel(0);
                    detectedActivityText = "Running";

                    break;
                }
                case DetectedActivity.STILL: {
                    Logger.d( TAG, "Still: " + activity.getConfidence() );
                    detectedActivityText = "Still";
                    detectedActivity = activity;
                    if(activity.getConfidence() > 85) {
                        MainApplication.showRunNotification("It seems like you are still!");
                    }
                    break;
                }
                case DetectedActivity.TILTING: {
                    Logger.d( TAG, "Tilting: " + activity.getConfidence() );
                    break;
                }
                case DetectedActivity.WALKING: {
                    Logger.d( TAG, "Walking: " + activity.getConfidence() );
                    detectedActivity = activity;
                    detectedActivityText = "Walking";
                    NotificationManagerCompat.from(this).cancel(0);
                    break;
                }
                case DetectedActivity.UNKNOWN: {
                    Logger.d( TAG, "Unknown: " + activity.getConfidence() );
                    break;
                }
            }
        }
    }

    public static DetectedActivity getDetectedActivity(){
        return detectedActivity;
    }





}
