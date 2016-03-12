package com.sharesmile.share.gps;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.sharesmile.share.core.Config;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.gps.models.DistRecord;
import com.sharesmile.share.gps.models.WorkoutData;
import com.sharesmile.share.utils.Logger;


/**
 * Created by ankitmaheshwari1 on 20/02/16.
 */
public class LocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, RunTracker.UpdateListner, SensorEventListener {

    private static final String TAG = "LocationService";

    private boolean currentlyProcessingLocation = false;
    private boolean currentlyProcessingSteps = false;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    private Location currentLocation;
    private SensorManager sensorManager;
    private int stepsTillNow = -1;
    private DistRecord lastValidatedRecord;
    private float lastValidatedDistance;

    private RunTracker tracker;

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.i(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.d(TAG, "onStartCommand");
        //If location fetching is already in process then no need to setup again
        if (!currentlyProcessingLocation) {
            currentlyProcessingLocation = true;
            startLocationUpdatess();
        }
        return START_STICKY;
    }

    private void startTracking(){
        if (tracker == null){
            tracker = new RunTracker(this);
        }
        if (!tracker.isActive()){
            tracker.beginRun();
        }
        stepsTillNow = -1;
        lastValidatedRecord = null;
        lastValidatedDistance = 0;
        startTimer();
    }


    private void stopTracking(){
        if (tracker != null){
            WorkoutData result = tracker.endRun();
            tracker = null;
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.LOCATION_SERVICE_BROADCAST_CATEGORY,
                    Constants.BROADCAST_WORKOUT_RESULT_CODE);
            bundle.putParcelable(Constants.KEY_WORKOUT_RESULT,result);
            Intent intent = new Intent(Constants.LOCATION_SERVICE_BROADCAST_ACTION);
            intent.putExtras(bundle);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
        stopTimer();
    }


    private void startLocationUpdatess() {
        Logger.d(TAG, "startLocationUpdates");
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            if (!googleApiClient.isConnected() || !googleApiClient.isConnecting()) {
                googleApiClient.connect();
            }
        } else {
            Logger.e(TAG, "unable to connect to google play services.");
        }
        if (isKitkatWithStepSensor() && !currentlyProcessingSteps){
            Logger.d(TAG, "Step Detector present! Will register");
            registerStepDetector();
        }
    }

    /**
     * Returns true if this device is supported. It needs to be running Android KitKat (4.4) or
     * higher and has a step counter and step detector sensor.
     * This check is useful when an app provides an alternative implementation or different
     * functionality if the step sensors are not available or this code runs on a platform version
     * below Android KitKat. If this functionality is required, then the minSDK parameter should
     * be specified appropriately in the AndroidManifest.
     *
     * @return True iff the device can run this sample
     */
    public boolean isKitkatWithStepSensor() {
        // Require at least Android KitKat
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        // Check that the device supports the step counter and detector sensors
        PackageManager packageManager = getPackageManager();
        return currentApiVersion >= android.os.Build.VERSION_CODES.KITKAT
                && packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR);
    }

    @TargetApi(19)
    private void registerStepDetector(){
        // Get the default sensor for the sensor type from the SenorManager
        sensorManager = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);
        // sensorType is either Sensor.TYPE_STEP_COUNTER or Sensor.TYPE_STEP_DETECTOR
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        // Register the listener for this sensor in batch mode.
        // If the max delay is 0, events will be delivered in continuous mode without batching.
        final boolean batchMode = sensorManager.registerListener(this, sensor,
                SensorManager.SENSOR_DELAY_NORMAL, Config.STEP_THRESHOLD_INTERVAL);

        if (batchMode) {
            // batchMode was enabled successfully
            Logger.d(TAG, "Step Detector registered successfully");
            currentlyProcessingSteps = true;
        }
    }

    public synchronized void stopLocationUpdates() {
        Logger.d(TAG, "stopLocationUpdates");
        if (currentlyProcessingLocation){
            stopTracking();
            if (googleApiClient != null && googleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
                googleApiClient.disconnect();
            }
            locationRequest = null;
            googleApiClient = null;
            currentLocation = null;
            currentlyProcessingLocation = false;
            stepsTillNow = -1;
            lastValidatedRecord = null;
            lastValidatedDistance = 0;
            sensorManager.unregisterListener(this);
            currentlyProcessingSteps = false;
            unBindFromActivityAndStop();
        }
    }

    private void unBindFromActivityAndStop() {
        Logger.d(TAG, "unBindFromActivityAndStop");
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.LOCATION_SERVICE_BROADCAST_CATEGORY,
                Constants.BROADCAST_UNBIND_SERVICE_CODE);
        sendBroadcast(bundle);
        stopSelf();
    }

    private void initiateLocationFetching(){
        Logger.d(TAG, "initiateLocationFetching");
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        startTracking();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Logger.d(TAG, "onUnbind");
        super.onUnbind(intent);
        if (!RunTracker.isActive()){
            // Stop service only when workout session is not going on
            Logger.d(TAG, "onUnbind: Will stop service");
            stopSelf();
        }
        return false;
    }

    @Override
    public void onDestroy() {
        Logger.d(TAG, "onDestroy");
        super.onDestroy();
    }

    final IBinder mBinder = new MyBinder();

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (tracker != null && tracker.isActive()){
            tracker.feedSteps(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     Class used for the client Binder.  Because we know this service always
     runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class MyBinder extends Binder {

        public LocationService getService() {
            // Return this instance of LocalService so clients can call public methods
            return LocationService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Logger.d(TAG, "onBind");
        return mBinder;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            currentLocation = location;
            tracker.feedLocation(location);
        }
    }

    @Override
    public void updateWorkoutRecord(float totalDistance, float currentSpeed){
        Logger.d(TAG, "updateWorkoutRecord: totalDistance = " + totalDistance
                + " currentSpeed = " + currentSpeed);
        // Send an update broadcast to Activity
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.LOCATION_SERVICE_BROADCAST_CATEGORY,
                Constants.BROADCAST_WORKOUT_UPDATE_CODE);
        bundle.putFloat(Constants.KEY_WORKOUT_UPDATE_SPEED, currentSpeed);
        bundle.putFloat(Constants.KEY_WORKOUT_UPDATE_TOTAL_DISTANCE, totalDistance);
        sendBroadcast(bundle);
    }

    private void sendBroadcast(Bundle bundle){
        Intent intent = new Intent(Constants.LOCATION_SERVICE_BROADCAST_ACTION);
        intent.putExtras(bundle);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public void updateStepsRecord(long timeStampMillis, int numSteps) {
        Logger.d(TAG, "Time to show steps count, totalSteps = " + tracker.getTotalSteps());
        // Send an update broadcast to Activity
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.LOCATION_SERVICE_BROADCAST_CATEGORY,
                Constants.BROADCAST_STEPS_UPDATE_CODE);
        bundle.putInt(Constants.KEY_WORKOUT_UPDATE_STEPS, tracker.getTotalSteps());
        sendBroadcast(bundle);
    }

    private void sendStopWorkoutBroadcast(int problem){
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.KEY_WORKOUT_STOP_PROBLEM, problem);
        bundle.putInt(Constants.LOCATION_SERVICE_BROADCAST_CATEGORY,
                Constants.BROADCAST_STOP_WORKOUT_CODE);
        sendBroadcast(bundle);
        stopLocationUpdates();
    }

    /**
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle bundle) {
        Logger.d(TAG, "onConnected");

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(Config.LOCATION_UPDATE_INTERVAL); // milliseconds
        locationRequest.setSmallestDisplacement(Config.SMALLEST_DISPLACEMENT);
        locationRequest.setFastestInterval(Config.LOCATION_UPDATE_INTERVAL); // the fastest rate in milliseconds at which your app can handle location updates
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        checkForLocationSettings();

        fetchInitialLocation();
    }


    private void fetchInitialLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            currentLocation =
                    LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (currentLocation == null){
                Logger.i(TAG, "Last Known Location could'nt be fetched");
                Toast.makeText(this, "Couldn't fetch last location", Toast.LENGTH_LONG).show();
            }
        }else {
            //No need to worry about permission unavailability, as it was already granted before service started
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode){
            case Constants.CODE_LOCATION_SETTINGS_RESOLUTION:
                if (resultCode == Activity.RESULT_OK){
                    // Can start with location requests
                    initiateLocationFetching();
                }else{
                    // Can't do nothing, retry for enabling Location Settings
                    checkForLocationSettings();
                }
        }
    }

    private void checkForLocationSettings(){
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient,
                        builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        initiateLocationFetching();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        Bundle bundle = new Bundle();
                        bundle.putInt(Constants.LOCATION_SERVICE_BROADCAST_CATEGORY,
                                Constants.BROADCAST_FIX_LOCATION_SETTINGS_CODE);
                        Intent intent = new Intent(Constants.LOCATION_SERVICE_BROADCAST_ACTION);
                        bundle.putParcelable(Constants.KEY_LOCATION_SETTINGS_PARCELABLE,
                                status);
                        intent.putExtras(bundle);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        Toast.makeText(getApplicationContext(), "Sorry, can't Access GPS", Toast.LENGTH_SHORT);
                        break;
                }
            }
        });
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Logger.e(TAG, "onConnectionFailed");
        stopLocationUpdates();
        stopSelf();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Logger.e(TAG, "GoogleApiClient connection has been suspend");
    }

    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            onTimerTick();
            timerHandler.postDelayed(this, Config.VIGILANCE_TIMER_INTERVAL);
        }
    };

    private synchronized void startTimer(){
        timerHandler.removeCallbacks(timerRunnable);
        timerHandler.post(timerRunnable);
    }

    private synchronized void stopTimer(){
        timerHandler.removeCallbacks(timerRunnable);
    }

    private synchronized void onTimerTick(){

        if (tracker != null){
            if (currentlyProcessingLocation){
                //check for slow speed
                if (checkForTooSlow()){
                    Logger.d(TAG, "Workout too slow, not enough distance will stop");
                    // Not enough steps/distance since the beginning
                    sendStopWorkoutBroadcast(Constants.PROBELM_TOO_SLOW);
                    return;
                }

                //check for high speed
                if (checkForTooFast()){
                    sendStopWorkoutBroadcast(Constants.PROBELM_TOO_FAST);
                    return;
                }
            }
            if (currentlyProcessingSteps){
                // Check if user is actually running/moving
                if (checkForLackOfMovement()){
                    sendStopWorkoutBroadcast(Constants.PROBELM_NOT_MOVING);
                    return;
                }
            }
        }
    }

    private boolean checkForTooSlow(){
        long timeElapsedSinceBeginning = System.currentTimeMillis() - tracker.getBeginTimeStamp();
        float inSecs = (float)(timeElapsedSinceBeginning / 1000);
        Logger.d(TAG, "onTick, Lower speed limit check, till now steps = " + tracker.getTotalSteps()
                + ", timeElapsed in secs = " + inSecs
                + ", distanceCovered = " +  tracker.getDistanceCovered());
        if (timeElapsedSinceBeginning > Config.VIGILANCE_START_THRESHOLD
                && tracker.getDistanceCovered() < inSecs*Config.LOWER_SPEED_LIMIT
                ){
            return true;
        }
        return false;
    }

    private boolean checkForTooFast(){
        if (lastValidatedRecord == null){
            // Will wait for next tick
            lastValidatedRecord = tracker.getLastRecord();
            lastValidatedDistance = tracker.getDistanceCovered();
        }else{
            DistRecord latestRecord = tracker.getLastRecord();
            if (!lastValidatedRecord.equals(latestRecord)){
                // We have a new record!
                float distanceInSession = tracker.getDistanceCovered() - lastValidatedDistance;
                float timeElapsedInSecs = (float)
                        ((latestRecord.getLocation().getTime() - lastValidatedRecord.getLocation().getTime()) / 1000);
                Logger.d(TAG, "onTick Upper speed limit check, Distance in last session = " + distanceInSession
                        + ", timeElapsedInSecs = " + timeElapsedInSecs
                        + ", distanceCovered = " +  tracker.getDistanceCovered());
                if (distanceInSession > Config.MIN_DISTANCE_FOR_VIGILANCE){
                    // Distance is above the threshold minimum to apply Usain Bolt Filter
                    float speedInSession = distanceInSession / timeElapsedInSecs;
                    if (speedInSession > Config.UPPER_SPEED_LIMIT){
                        // Running faster than Usain Bolt
                        Logger.d(TAG, "Speed " + speedInSession + " m/s is too fast, will show Usain Bolt");
                        return true;
                    }else{
                        lastValidatedRecord = latestRecord;
                        lastValidatedDistance = tracker.getDistanceCovered();
                    }
                }
            }
        }
        return false;
    }

    private boolean checkForLackOfMovement(){
        if (stepsTillNow == -1){
            //Will wait for the next tick
            stepsTillNow = tracker.getTotalSteps();
        }else{
            int totalSteps = tracker.getTotalSteps();
            int stepsThisSession = totalSteps - stepsTillNow;
            if (stepsThisSession < (Config.VIGILANCE_TIMER_INTERVAL / 1000)*Config.STEPS_PER_SECOND_FACTOR){
                //time to pause workout
                // Not enough steps since the beginning
                Logger.d(TAG, "Only " + stepsThisSession + " this session. Not enough! Will pause workout");
                return true;
            }else{
                stepsTillNow = totalSteps;
            }
        }
        return false;
    }

}
