package com.sharesmile.share.gps.activityrecognition;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.sharesmile.share.MainApplication;
import com.sharesmile.share.R;
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.core.Config;
import com.sharesmile.share.utils.CircularQueue;
import com.sharesmile.share.utils.Logger;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.sharesmile.share.core.Config.ACTIVITY_VALID_INTERVAL_ACTIVE;
import static com.sharesmile.share.core.Config.ACTIVITY_VALID_INTERVAL_IDLE;
import static com.sharesmile.share.core.Config.CONFIDENCE_LOWER_THRESHOLD_STILL;
import static com.sharesmile.share.core.Config.CONFIDENCE_THRESHOLD_ON_FOOT;
import static com.sharesmile.share.core.Config.CONFIDENCE_THRESHOLD_VEHICLE;
import static com.sharesmile.share.core.Config.CONFIDENCE_THRESHOLD_WALK_ENGAGEMENT;
import static com.sharesmile.share.core.Config.CONFIDENCE_UPPER_THRESHOLD_STILL;
import static com.sharesmile.share.core.Config.DETECTED_INTERVAL_ACTIVE;
import static com.sharesmile.share.core.Config.DETECTED_INTERVAL_IDLE;
import static com.sharesmile.share.core.Config.WALK_ENGAGEMENT_COUNTER_INTERVAL;
import static com.sharesmile.share.core.Config.WALK_ENGAGEMENT_NOTIFICATION_INTERVAL;
import static com.sharesmile.share.core.NotificationActionReceiver.WORKOUT_NOTIFICATION_STILL_ID;
import static com.sharesmile.share.core.NotificationActionReceiver.WORKOUT_NOTIFICATION_WALK_ENGAGEMENT;

/**
 * Created by ankitmaheshwari on 3/10/17.
 */

public class ActivityDetector implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "ActivityDetector";

    private boolean isInVehicle = false;
    private boolean isOnFoot = false;
    private boolean isStill = false;
    private int stillNotificationOccurredCounter = 0;

    private float runningConfidenceRecentAvg;
    private float walkingConfidenceRecentAvg;
    private float onFootConfidenceRecentAvg;

    private boolean isWorkoutActive;

    private CircularQueue<ActivityRecognitionResult> historyQueue;


    private static ActivityDetector uniqueInstance;
    private Context appContext;
    private GoogleApiClient googleApiClient;
    private Handler handler;


    private ActivityDetector(Context appContext){
        this.appContext = appContext;
        this.handler = new Handler();
        this.historyQueue = new CircularQueue<>(3);
        connectGoogleApiClient();
    }

    /**
     Throws IllegalStateException if this class is not initialized

     @return unique ActivityDetector instance
     */
    public static ActivityDetector getInstance() {
        if (uniqueInstance == null) {
            throw new IllegalStateException(
                    "ActivityDetector is not initialized, call initialize(applicationContext) " +
                            "static method first");
        }
        return uniqueInstance;
    }

    /**
     Initialize this class using application Context,
     should be called once in the beginning by any application Component

     @param appContext application context
     */
    public static void initialize(Context appContext) {
        if (appContext == null) {
            throw new NullPointerException("Provided application context is null");
        }
        if (uniqueInstance == null) {
            synchronized (ActivityDetector.class) {
                if (uniqueInstance == null) {
                    uniqueInstance = new ActivityDetector(appContext);
                }
            }
        }
    }

    private void connectGoogleApiClient(){

        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(appContext) == ConnectionResult.SUCCESS) {
            googleApiClient = new GoogleApiClient.Builder(appContext)
                    .addApi(ActivityRecognition.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            if (!googleApiClient.isConnected() || !googleApiClient.isConnecting()) {
                googleApiClient.connect();
            }
        } else {
            Logger.e(TAG, "unable to connect to google play services.");
        }

    }

    private void registerForActivityUpdates(){
        Intent intent = new Intent(appContext, ActivityRecognizedService.class);
        PendingIntent pendingIntent = PendingIntent.getService( appContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        long detectionIntervalMillis = isWorkoutActive() ? DETECTED_INTERVAL_ACTIVE : DETECTED_INTERVAL_IDLE;
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( googleApiClient, detectionIntervalMillis, pendingIntent );
        if (!isWorkoutActive()){
            startWalkEngagementDetectionCounter();
        }
    }

    public void workoutActive(){
        isWorkoutActive = true;
        if (googleApiClient != null && googleApiClient.isConnected()){
            registerForActivityUpdates();
        }else {
            connectGoogleApiClient();
        }
        handler.removeCallbacks(handleStillNotificationRunnable);
        stopWalkEngagementDetectionCounter();
    }

    public void workoutIdle(){
        isWorkoutActive = false;
        if (googleApiClient != null && googleApiClient.isConnected()){
            registerForActivityUpdates();
        }else {
            connectGoogleApiClient();
        }
        handler.removeCallbacks(handleStillNotificationRunnable);
        startWalkEngagementDetectionCounter();
    }

    private void stopWalkEngagementDetectionCounter(){
        if (isWalkEngagementNotificationOnDisplay){
            cancelWalkEngagementNotif();
        }
        handler.removeCallbacks(handleEngagementNotificationRunnable);
        timeOnFootContinuously = 0;
    }

    public void cancelWalkEngagementNotif(){
        NotificationManager manager = (NotificationManager) appContext.getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(WORKOUT_NOTIFICATION_WALK_ENGAGEMENT);
        isWalkEngagementNotificationOnDisplay = false;
    }

    private void startWalkEngagementDetectionCounter(){
        handler.removeCallbacks(handleEngagementNotificationRunnable);
        timeOnFootContinuously = 0;
        handler.postDelayed(handleEngagementNotificationRunnable, WALK_ENGAGEMENT_COUNTER_INTERVAL);
    }

    public boolean isWorkoutActive(){
        return isWorkoutActive;
    }

    public void stopActivityDetection(){
        Logger.d(TAG, "stopActivityDetection");
        unregisterForActivityUpdates();
        if (googleApiClient != null && googleApiClient.isConnected()){
            googleApiClient.disconnect();
        }
        googleApiClient = null;
    }

    private void unregisterForActivityUpdates(){
        reset();
        Intent intent = new Intent(appContext, ActivityRecognizedService.class);
        PendingIntent pendingIntent = PendingIntent.getService(appContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (googleApiClient != null && googleApiClient.isConnected()) {
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates( googleApiClient, pendingIntent );
        }
    }

    final Runnable handleStillNotificationRunnable = new Runnable() {
        @Override
        public void run() {
            // Show notification and increment still occurred counter
            MainApplication.showRunNotification(WORKOUT_NOTIFICATION_STILL_ID,
                    appContext.getString(R.string.notification_standing_still),
                    appContext.getString(R.string.notification_action_pause),
                    appContext.getString(R.string.notification_action_stop)
            );
            AnalyticsEvent.create(Event.DISP_YOU_ARE_STILL_NOTIF)
                    .buildAndDispatch();
            handler.removeCallbacks(handleStillNotificationRunnable);
            stillNotificationOccurredCounter = 1;
        }
    };

    private long timeOnFootContinuously = 0;
    private boolean isWalkEngagementNotificationOnDisplay = false;

    final Runnable handleEngagementNotificationRunnable = new Runnable() {
        @Override
        public void run() {
            Logger.d(TAG, "handleEngagementNotificationRunnable: run called");
            MainApplication.showToast("OnFoot: " + onFootConfidenceRecentAvg);
            if (onFootConfidenceRecentAvg > CONFIDENCE_THRESHOLD_WALK_ENGAGEMENT){
                // If on foot currently increase timeOnFootContinuously
                timeOnFootContinuously += WALK_ENGAGEMENT_COUNTER_INTERVAL;
            }else {
                // If not on foot then reset timeOnFootContinuously to 0
                timeOnFootContinuously = 0;
                if (isWalkEngagementNotificationOnDisplay){
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            cancelWalkEngagementNotif();
                        }
                    }, WALK_ENGAGEMENT_COUNTER_INTERVAL);
                    cancelWalkEngagementNotif();
                }
            }
            if (timeOnFootContinuously > WALK_ENGAGEMENT_NOTIFICATION_INTERVAL){
                // User has been walking continuously for the past 4 mins without switching on ImpactRun
                // Lets notify him/her to start the app
                if (!isWalkEngagementNotificationOnDisplay){
                    MainApplication.showRunNotification("Start ImpactRun",
                            WORKOUT_NOTIFICATION_WALK_ENGAGEMENT,
                            appContext.getString(R.string.notification_walk_engagement),
                            appContext.getString(R.string.notification_action_start),
                            appContext.getString(R.string.notification_action_cancel)
                    );
                    isWalkEngagementNotificationOnDisplay = true;
                }
                timeOnFootContinuously = 0;
            }
            handler.postDelayed(handleEngagementNotificationRunnable, WALK_ENGAGEMENT_COUNTER_INTERVAL);
        }
    };

    final Runnable resetConfidenceValuesRunnable = new Runnable() {
        @Override
        public void run() {
            runningConfidenceRecentAvg = 0;
            walkingConfidenceRecentAvg = 0;
            onFootConfidenceRecentAvg = 0;
            handler.removeCallbacks(resetConfidenceValuesRunnable);
        }
    };

    public void handleActivityRecognitionResult(ActivityRecognitionResult result){
        int inVehicleConfidence = result.getActivityConfidence(DetectedActivity.IN_VEHICLE);
        float stillConfidence = result.getActivityConfidence(DetectedActivity.STILL);
        float onFootConfidence = result.getActivityConfidence(DetectedActivity.ON_FOOT);
        float runningConfidence = result.getActivityConfidence(DetectedActivity.RUNNING);
        float walkingConfidence = result.getActivityConfidence(DetectedActivity.WALKING);

        Logger.d(TAG, "handleActivityRecognitionResult: inVehicleConfidence = " + inVehicleConfidence
                    + ", stillConfidence = " + stillConfidence + ", onFootConfidence = " + onFootConfidence
                    + ", stillNotificationOccurredCounter " + stillNotificationOccurredCounter);

        // Add the fresh result in HistoryQueue
        long currentTime = result.getTime();
        historyQueue.add(result);
        if (historyQueue.isFull()){
            int i = historyQueue.getMaxSize() - 1;
            int count = 0;
            float cumulativeVehicleConfidence = 0;
            float cumulativeOnFootConfidence = 0;
            float cumulativeStillConfidence = 0;
            float cumulativeRunningConfidence = 0;
            float cumulativeWalkingConfidence = 0;
            long validInterval = isWorkoutActive() ? ACTIVITY_VALID_INTERVAL_ACTIVE : ACTIVITY_VALID_INTERVAL_IDLE;
            while (i >= 0){
                ActivityRecognitionResult elem = historyQueue.getElemAtPosition(i);
                if (currentTime - elem.getTime() > validInterval){
                    // This is elem is way too old to be considered
                    break;
                }
                cumulativeVehicleConfidence += elem.getActivityConfidence(DetectedActivity.IN_VEHICLE);
                cumulativeOnFootConfidence += elem.getActivityConfidence(DetectedActivity.ON_FOOT);
                cumulativeStillConfidence += elem.getActivityConfidence(DetectedActivity.STILL);
                cumulativeRunningConfidence += elem.getActivityConfidence(DetectedActivity.RUNNING);
                cumulativeWalkingConfidence += elem.getActivityConfidence(DetectedActivity.WALKING);
                count++;
                i--;
            }
            float avgVehilceConfidence = cumulativeVehicleConfidence / count;
            float avgOnFootConfidence = cumulativeOnFootConfidence / count;
            float avgStillConfidence = cumulativeStillConfidence / count;
            runningConfidenceRecentAvg = cumulativeRunningConfidence / count;
            walkingConfidenceRecentAvg = cumulativeWalkingConfidence / count;
            onFootConfidenceRecentAvg = cumulativeOnFootConfidence / count;
            handler.removeCallbacks(resetConfidenceValuesRunnable);
            // Resetting these confidence values back to 0, if we don't receive activity recognition updates for sufficiently long period
            handler.postDelayed(resetConfidenceValuesRunnable, 25000);

            Logger.d(TAG, "handleActivityRecognitionResult, calculated avg confidence values, Vehicle: "
                    + avgVehilceConfidence + ", Foot: " + avgOnFootConfidence + ", Still: " + avgStillConfidence);

            if (avgStillConfidence > CONFIDENCE_UPPER_THRESHOLD_STILL){
                isStill = true;
            }else if (avgStillConfidence < CONFIDENCE_LOWER_THRESHOLD_STILL){
                isStill = false;
                stillNotificationOccurredCounter = 0;
            }

            if (avgVehilceConfidence > CONFIDENCE_THRESHOLD_VEHICLE){
                isInVehicle = true;
                isStill = false; // isStill is forced set as false if the user is supposedly in vehicle
                stillNotificationOccurredCounter = 0;
                if (isWorkoutActive()){
                    AnalyticsEvent.create(Event.DISP_YOU_ARE_DRIVING_NOTIF)
                            .buildAndDispatch();
                }
            }else {
                isInVehicle = false;
            }

            if (avgOnFootConfidence > CONFIDENCE_THRESHOLD_ON_FOOT){
                isOnFoot = true;
                isStill = false; // isStill is forced set as false if the user is supposedly on foot
                stillNotificationOccurredCounter = 0;
            }else {
                isOnFoot = false;
            }
        }

        if (isWorkoutActive()){
            if (isStill){
                if (stillNotificationOccurredCounter == 0){
                    handler.postDelayed(handleStillNotificationRunnable, Config.STILL_NOTIFICATION_DISPLAY_INTERVAL);
                    Logger.d(TAG, "Scheduling Still notification.");
                }
            }else {
                handler.removeCallbacks(handleStillNotificationRunnable);
                Logger.d(TAG, "Removing Still notification.");
            }
        }

    }

    public void persistentMovementDetectedFromOutside(){
        if (isStill){
            isStill = false;
            stillNotificationOccurredCounter = 0;
        }
    }

    public float getRunningConfidence(){
        return runningConfidenceRecentAvg;
    }

    public float getWalkingConfidence(){
        return walkingConfidenceRecentAvg;
    }

    public float getOnFootConfidence(){
        return onFootConfidenceRecentAvg;
    }

    public boolean isIsInVehicle(){
        return isInVehicle;
    }

    public boolean isOnFoot(){
        return isOnFoot;
    }

    public boolean isStill(){
        return isStill;
    }

    private void reset(){
        isInVehicle = false;
        isOnFoot = false;
        historyQueue.clear();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Logger.d(TAG, "onConnected");
        reset();
        registerForActivityUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Connection temporarily suspended, onConnected will be called once connection is back
        Logger.e(TAG, "GoogleApiClient connection has been suspend");
    }

    int retryAttempt = 0;

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Logger.e(TAG, "onConnectionFailed");
        if (retryAttempt < 3){
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    connectGoogleApiClient();
                }
            }, 500);
            retryAttempt++;
        }
        else {
            // Connection Failure, notify listeners

        }
    }
}
