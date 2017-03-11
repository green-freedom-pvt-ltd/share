package com.sharesmile.share.gps.activityrecognition;

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
import com.sharesmile.share.analytics.events.AnalyticsEvent;
import com.sharesmile.share.analytics.events.Event;
import com.sharesmile.share.utils.CircularQueue;
import com.sharesmile.share.utils.Logger;

/**
 * Created by ankitmaheshwari on 3/10/17.
 */

public class ActivityDetector implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "ActivityDetector";

    private boolean isInVehicle = false;
    private boolean isOnFoot = false;
    private int stillOccurredCounter = 0;

    private boolean isWorkoutActive;

    private CircularQueue<ActivityRecognitionResult> historyQueue;

    public static final int CONFIDENCE_THRESHOLD = 80;
    public static final int CONFIDENCE_THRESHOLD_EVENT = 30;
    public static final int ACTIVITY_VALID_INTERVAL = 21000; // in millisecs
    public static final long DETECTED_INTERVAL_IDLE = 6000; // in millisecs
    public static final long DETECTED_INTERVAL_ACTIVE = 2000; // in millisecs


    private static ActivityDetector uniqueInstance;
    private Context appContext;
    private GoogleApiClient googleApiClient;
    private Handler handler;


    private ActivityDetector(Context appContext){
        this.appContext = appContext;
        this.handler = new Handler();
        this.historyQueue = new CircularQueue<>(3);
        connectToLocationServices();
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

    private void connectToLocationServices(){

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
        long detectionIntervalMillis = isWorkoutActive ? DETECTED_INTERVAL_ACTIVE : DETECTED_INTERVAL_IDLE;
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( googleApiClient, detectionIntervalMillis, pendingIntent );
    }

    public void workoutActive(){
        isWorkoutActive = true;
        if (googleApiClient != null && googleApiClient.isConnected()){
            registerForActivityUpdates();
        }else {
            connectToLocationServices();
        }
    }

    public void workoutIdle(){
        isWorkoutActive = false;
        if (googleApiClient != null && googleApiClient.isConnected()){
            registerForActivityUpdates();
        }else {
            connectToLocationServices();
        }
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

    public void handleActivityRecognitionResult(ActivityRecognitionResult result){
        Logger.d( TAG, "IN_VEHICLE, confidence " +  result.getActivityConfidence(DetectedActivity.IN_VEHICLE));

        int inVehicleConfidence = result.getActivityConfidence(DetectedActivity.IN_VEHICLE);
        if (inVehicleConfidence > CONFIDENCE_THRESHOLD_EVENT){
            AnalyticsEvent.create(Event.ACTIVITY_RCOGNIZED_IN_VEHICLE)
                    .put("confidence_value", inVehicleConfidence)
                    .buildAndDispatch();
        }

        // Add the fresh result in HistoryQueue
        long currentTime = result.getTime();
        historyQueue.add(result);
        if (historyQueue.isFull()){
            int i = historyQueue.getMaxSize() - 1;
            int count = 0;
            float cumulativeVehicleConfidence = 0;
            float cumulativeStillConfidence = 0;
            float cumulativeOnFootConfidence = 0;
            while (i >= 0){
                ActivityRecognitionResult elem = historyQueue.getElemAtPosition(i);
                if (currentTime - elem.getTime() > ACTIVITY_VALID_INTERVAL){
                    // This is elem is way too old to be considered
                    break;
                }
                cumulativeVehicleConfidence += elem.getActivityConfidence(DetectedActivity.IN_VEHICLE);
                cumulativeStillConfidence += elem.getActivityConfidence(DetectedActivity.STILL);
                cumulativeOnFootConfidence += elem.getActivityConfidence(DetectedActivity.ON_FOOT);
                count++;
            }
            float avgVehilceConfidence = cumulativeVehicleConfidence / count;
            float avgStillConfidence = cumulativeStillConfidence / count;
            float avgOnFootConfidence = cumulativeOnFootConfidence / count;

            Logger.d( TAG, "IN_VEHICLE, AverageConfidence " +  avgVehilceConfidence);
            if (avgVehilceConfidence > CONFIDENCE_THRESHOLD){
                isInVehicle = true;
                MainApplication.showRunNotification("We have detected that you are driving.");
                AnalyticsEvent.create(Event.DISP_YOU_ARE_DRIVING_NOTIF)
                        .buildAndDispatch();
            }else {
                isInVehicle = false;
            }

            Logger.d( TAG, "STILL, AverageConfidence " +  avgStillConfidence);
            if (avgStillConfidence > CONFIDENCE_THRESHOLD){
                if (stillOccurredCounter == 0){
                    // Show notification and increment still occurred counter
                    MainApplication.showRunNotification("It seems like you are still!");
                    AnalyticsEvent.create(Event.DISP_YOU_ARE_STILL_NOTIF)
                            .buildAndDispatch();
                    stillOccurredCounter = 1;
                }
            }

            Logger.d( TAG, "ON_FOOT, AverageConfidence " +  avgOnFootConfidence);
            if (avgOnFootConfidence > CONFIDENCE_THRESHOLD){
                isOnFoot = true;
                stillOccurredCounter = 0;
            }else {
                isOnFoot = false;
            }
        }
    }

    public boolean isIsInVehicle(){
        return isInVehicle;
    }

    public boolean isOnFoot(){
        return isOnFoot;
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
                    connectToLocationServices();
                }
            }, 500);
            retryAttempt++;
        }
        else {
            // Connection Failure, notify listeners

        }
    }
}
