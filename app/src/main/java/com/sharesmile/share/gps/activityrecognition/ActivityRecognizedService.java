package com.sharesmile.share.gps.activityrecognition;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.utils.Logger;

import static com.sharesmile.share.gps.activityrecognition.ActivityDetector.CONFIDENCE_THRESHOLD_EVENT;

/**
 * Created by piyush on 10/1/16.
 */

public class ActivityRecognizedService extends IntentService {

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
            ActivityDetector.getInstance().handleActivityRecognitionResult(result);
        }
    }

}
